import argparse
import subprocess
import logging
import os
import signal
import sys
from typing import List
from datetime import datetime

from metric_backend_client.configuration import Configuration
from metric_backend_client.api_client import ApiClient
from metric_backend_client.api.default_api import DefaultApi
from metric_backend_client.models.pyflame_profile import PyflameProfile

from feedback_wrapper.version_provider import get_current_version

config = Configuration()
config.host = "http://host.docker.internal:8080/api"
metric_handling_api = DefaultApi(ApiClient(config))
app = None
current_version = None

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


def instrument_flask(flask_app, base_path):
    global app
    app = flask_app

    app.logger.info("Base path: " + base_path)
    current_version = get_current_version(base_path)
    app.logger.info(f'Current version: {current_version}')

    @flask_app.before_request
    def before():
        from flask import request, after_this_request

        process, start_time = pyflame_profile_start(request)

        @after_this_request
        def after(response):
            return pyflame_profile_end(process, response, start_time, base_path, current_version)


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


def pyflame_profile_end(process, response, start_time, base_path, current_version):

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
        start_timestamp=start_time,
        end_timestamp=end_time,
        pyflame_output=stdout,
        base_path=base_path,
        version=current_version
    )

    added_profile_response = metric_handling_api.add_pyflame_profile(pyflame_profile)
    generate_flamegraph(base_path, stdout, added_profile_response.id)

    return response


def generate_flamegraph(base_path, pyflame_output, profile_id):
    command = 'flamegraph.pl'
    process = subprocess.Popen(command.split(' '), stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    stdout, stderr = process.communicate(input=pyflame_output)

    filename = os.path.join(base_path, 'flamegraphs', f'{profile_id}.svg')
    with open(filename, 'w') as file:
        file.write(stdout)

    filename = os.path.join(base_path, 'flamegraphs', f'{profile_id}.txt')
    with open(filename, 'w') as file:
        file.write(pyflame_output)

    app.logger.info(f'Wrote flamegraph to: {filename}')

if __name__ == "__main__":
    main()