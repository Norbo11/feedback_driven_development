import threading
import traceback
import json
from multiprocessing import Process

from datetime import datetime

from feedback_wrapper.configuration import FeedbackConfiguration
from feedback_wrapper.instrument_exceptions import instrument_exceptions
from feedback_wrapper.instrument_logs import instrument_logs
from feedback_wrapper.utils import generate_flamegraph
from feedback_wrapper.send_feedback import send_feedback
from feedback_wrapper.instrument_performance import start_pyflame, stop_pyflame
from playground_application.models.number import Number

from flask import request, after_this_request, _app_ctx_stack

feedback_config = None


class InstrumentationMetadata(object):

    def __init__(self):
        self.request = None
        self.pyflame_process = None
        self.pyflame_output_file_stdout = None
        self.start_time = None
        self.end_time = None
        self.start_time_with_instrumentation = None
        self.end_time_with_instrumentation = None
        self.stack_summary = None
        self.exception = None
        self.logging_lines = []
        self.instrumentation_stopped = False


def instrument_flask(flask_app, feedback_config_filename):
    global feedback_config
    feedback_config = FeedbackConfiguration(flask_app, feedback_config_filename)

    if not feedback_config.enable:
        return

    if flask_app.config['DEBUG']:
        flask_app.logger.warning("You are currently running in debug mode. We force PRESERVE_CONTEXT_ON_EXCEPTION = False, "
                                 "so your debugger may not work.")

    # This ensures that the request is always popped and the instrumentation_end function is always called
    flask_app.config['PRESERVE_CONTEXT_ON_EXCEPTION'] = False

    instrument_requests(flask_app)
    flask_app.logger.info("Requests instrumented")

    if feedback_config.instrument_exceptions:
        instrument_exceptions(flask_app)
        flask_app.logger.info("Exceptions instrumented")

    if feedback_config.instrument_logs:
        instrument_logs()
        flask_app.logger.info("Logs instrumented")


def instrument_requests(flask_app):
    @flask_app.before_request
    def before():
        instrumentation_metadata = InstrumentationMetadata()
        _app_ctx_stack.top.instrumentation_metadata = instrumentation_metadata
        instrument_start(flask_app, request)
        _app_ctx_stack.top.instrumentation_metadata.start_time = datetime.now()

    @flask_app.after_request
    def after_request(response):
        _app_ctx_stack.top.instrumentation_metadata.end_time = datetime.now()
        instrument_end(flask_app)
        return response

    @flask_app.teardown_request
    def after_teardoown(exception):
        print(_app_ctx_stack.top.instrumentation_metadata.instrumentation_stopped)
        if not _app_ctx_stack.top.instrumentation_metadata.instrumentation_stopped:
            _app_ctx_stack.top.instrumentation_metadata.end_time = datetime.now()
            instrument_end(flask_app)


def instrument_start(flask_app, request):
    _app_ctx_stack.top.instrumentation_metadata.start_time_with_instrumentation = datetime.now()
    process = None
    try:
        if feedback_config.instrument_performance:
            process = start_pyflame(feedback_config.pyflame_args)
    except Exception as ex:
        flask_app.logger.error(f'Error while starting instrumentation: ' + str(ex))

    _app_ctx_stack.top.instrumentation_metadata.request = request
    _app_ctx_stack.top.instrumentation_metadata.pyflame_process = process
    _app_ctx_stack.top.instrumentation_metadata.instrumentation_stopped = False


def instrument_end(flask_app):
    try:
        if feedback_config.instrument_performance:
            stdout, stderr, return_code = stop_pyflame()
            flask_app.logger.info(f'PyFlame stopped')

            if return_code != 0:
                flask_app.logger.error(f'pyflame returned non-zero status code {return_code}: \nstdout: {stdout}\nstderr: {stderr}')

                if return_code == -2:
                    flask_app.logger.error(f'Request possibly ran for too short')

                if return_code == 1:
                    flask_app.logger.error(f'Check that PyFlame has ptrace permissions')
                return

            if feedback_config.save_flamegraph:
                filename = str(_app_ctx_stack.top.instrumentation_metadata.start_time)
                generate_flamegraph(feedback_config.git_base_path, stdout, filename)

        if feedback_config.send_to_backend:
            send_feedback_process = Process(target=send_feedback, args=(feedback_config, _app_ctx_stack.top.instrumentation_metadata, stdout))
            send_feedback_process.start()
            flask_app.logger.info("Queued feedback sending process")
    except Exception as ex:
        flask_app.logger.error("Error while terminating instrumentation: " + str(ex))
        traceback.print_exc()
    finally:
        _app_ctx_stack.top.instrumentation_metadata.pyflame_output_file_stdout.close()

    _app_ctx_stack.top.instrumentation_metadata.end_time_with_instrumentation = datetime.now()
    _app_ctx_stack.top.instrumentation_metadata.instrumentation_stopped = True
