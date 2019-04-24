import argparse
import subprocess
import logging
import os
import signal
import sys
import pathlib
import traceback

from typing import List
from datetime import datetime

from metric_backend_client.models.pyflame_profile import PyflameProfile
from metric_backend_client.models.new_exception import NewException
from metric_backend_client.models.new_exception_frames import NewExceptionFrames

from feedback_wrapper.configuration import FeedbackConfiguration

app = None
feedback_config = None
instrumentation_metadata = None
PYFLAME_ARGS = {
    'abi': 36, # Without this, error code 1 is returned
    'seconds': 9999,
    'rate': 0.001, # Default is 0.01
}


class InstrumentationMetadata(object):

    def __init__(self, request=None, pyflame_process=None, start_time=None):
        self.request = request
        self.pyflame_process = pyflame_process
        self.start_time = start_time
        self.response = None
        self.stack_summary = None
        self.exception = None


def instrument_flask(flask_app, feedback_config_filename):
    global app, feedback_config
    app = flask_app
    feedback_config = FeedbackConfiguration(flask_app, feedback_config_filename)

    @flask_app.before_request
    def before():
        from flask import request, after_this_request
        global instrumentation_metadata

        instrumentation_metadata = pyflame_profile_start(request)
        flask_app.logger.info(type(instrumentation_metadata))
        flask_app.logger.info("dasdsdsasd")

        @after_this_request
        def after(response):
            instrumentation_metadata.response = response
            return pyflame_profile_end()

    flask_handle_exception = flask_app.handle_exception

    def instrumented_handle_exception(e):
        _, _, tb = sys.exc_info()
        stack_summary = traceback.extract_tb(tb)

        for entry in stack_summary:
            flask_app.logger.info(f'{entry.filename}: {entry.lineno} - {entry.name} - {entry.line}')

        instrumentation_metadata.exception = e
        instrumentation_metadata.stack_summary = stack_summary

        pyflame_profile_end()

        return flask_handle_exception(e)

    flask_app.handle_exception = instrumented_handle_exception



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

    return InstrumentationMetadata(request=request, pyflame_process=process, start_time=start_time)


def pyflame_profile_end():

    def kill_process():
        instrumentation_metadata.pyflame_process.send_signal(signal.SIGINT)

        try:
            stdout, stderr = instrumentation_metadata.pyflame_process.communicate(timeout=0.1)
            return stdout, stderr
        except subprocess.TimeoutExpired as e:
            return kill_process()

    stdout, stderr = kill_process()
    return_code = instrumentation_metadata.pyflame_process.poll()
    app.logger.info(f'Finished')

    if return_code != 0:
        app.logger.error(f'pyflame returned status code {return_code}: \nstdout: {stdout}\nstderr: {stderr}')

        if return_code == -2:
            app.logger.error(f'Request possibly ran for too short')

        return instrumentation_metadata.response

    instrumentation_metadata.end_time = datetime.now()

    exception = None
    if instrumentation_metadata.exception is not None:
        frames = [NewExceptionFrames(filename=f.filename, line_number=f.lineno, function_name=f.name)
                  for f in instrumentation_metadata.stack_summary]

        exception = NewException(
            exception_type=type(instrumentation_metadata.exception).__name__,
            exception_message=str(instrumentation_metadata.exception),
            frames=frames
        )

    pyflame_profile = PyflameProfile(
        application_name=feedback_config.application_name,
        version=feedback_config.current_version,
        start_timestamp=instrumentation_metadata.start_time,
        end_timestamp=instrumentation_metadata.end_time,
        pyflame_output=stdout,
        base_path=str(feedback_config.source_base_path),
        instrument_directories=[str(d) for d in feedback_config.instrument_directories],
        exception=exception
    )

    added_profile_response = feedback_config.metric_handling_api.add_pyflame_profile(pyflame_profile)
    app.logger.info("Recorded profile")


    generate_flamegraph(feedback_config.git_base_path, stdout, added_profile_response.id)

    return instrumentation_metadata.response


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