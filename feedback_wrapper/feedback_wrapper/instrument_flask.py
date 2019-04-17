import argparse
import subprocess
import logging
import os
import signal
import sys
import pathlib

from typing import List
from datetime import datetime

from metric_backend_client.models.pyflame_profile import PyflameProfile

from feedback_wrapper.configuration import FeedbackConfiguration

app = None
feedback_config = None
PYFLAME_ARGS = {
    'abi': 36, # Without this, error code 1 is returned
    'seconds': 9999,
    'rate': 0.001, # Default is 0.01
}

def main():
    parser = argparse.ArgumentParser(
        description="Feedback Driven Development wrapper: Flask",
    )

    parser.add_argument('command', nargs=argparse.REMAINDER)
    args = parser.parse_args()
    wrap_flask(args.command)


def instrument_flask(flask_app, feedback_config_filename):
    global app, feedback_config
    app = flask_app
    feedback_config = FeedbackConfiguration(flask_app, feedback_config_filename)

    @flask_app.before_request
    def before():
        from flask import request, after_this_request

        process, start_time = pyflame_profile_start(request)

        @after_this_request
        def after(response):
            return pyflame_profile_end(process, response, start_time)


def pyflame_profile_start(request):
    pid = os.getpid()

    # TODO: Run forever instead of 10 seconds
    # TODO: Don't hardcode ABI
    # TODO: Use pyspy
    # TODO: Look into the effects of "idle" time
    # TODO: Look into the effects of rate
    command = f"pyflame-bleeding " \
        f"--threads " \
        f"--exclude-idle " \
        f"--abi {PYFLAME_ARGS['abi']} " \
        f"--rate {PYFLAME_ARGS['rate']} " \
        f"--seconds {PYFLAME_ARGS['seconds']} " \
        f"-p {pid} "
        # f"--flamechart "

    start_time = datetime.now()

    app.logger.info(f'Running {command}')
    process = subprocess.Popen(command.split(' '), stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    app.logger.info(f'Ran')

    return process, start_time


def pyflame_profile_end(process, response, start_time):

    def kill_process():
        process.send_signal(signal.SIGINT)

        try:
            stdout, stderr = process.communicate(timeout=0.1)
            return stdout, stderr
        except subprocess.TimeoutExpired as e:
            return kill_process()

    stdout, stderr = kill_process()
    return_code = process.poll()
    app.logger.info(f'Finished')

    if return_code != 0:
        app.logger.error(f'pyflame returned status code {return_code}: \nstdout: {stdout}\nstderr: {stderr}')

        if return_code == -2:
            app.logger.error(f'Request possibly ran for too short')

        return response

    end_time = datetime.now()

    pyflame_profile = PyflameProfile(
        application_name=feedback_config.application_name,
        version=feedback_config.current_version,
        start_timestamp=start_time,
        end_timestamp=end_time,
        pyflame_output=stdout,
        base_path=str(feedback_config.source_base_path),
        instrument_directories=[str(d) for d in feedback_config.instrument_directories]
    )

    added_profile_response = feedback_config.metric_handling_api.add_pyflame_profile(pyflame_profile)
    generate_flamegraph(feedback_config.git_base_path, stdout, added_profile_response.id)

    return response


def generate_flamegraph(directory, pyflame_output, profile_id):
    command = 'flamegraph.pl'
    process = subprocess.Popen(command.split(' '), stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    stdout, stderr = process.communicate(input=pyflame_output)

    filename = os.path.join(directory, 'flamegraphs', f'{profile_id}.svg')
    with open(filename, 'w') as file:
        file.write(stdout)

    filename = os.path.join(directory, 'flamegraphs', f'{profile_id}.txt')
    with open(filename, 'w') as file:
        file.write(pyflame_output)

    app.logger.info(f'Wrote flamegraph to: {filename}')

if __name__ == "__main__":
    main()