import sys
import traceback

from flask import _app_ctx_stack


def instrument_exceptions(flask_app):
    flask_handle_exception = flask_app.handle_exception

    def instrumented_handle_exception(e):
        _, _, tb = sys.exc_info()
        stack_summary = traceback.extract_tb(tb)

        _app_ctx_stack.top.instrumentation_metadata.exception = e
        _app_ctx_stack.top.instrumentation_metadata.stack_summary = stack_summary

        return flask_handle_exception(e)

    flask_app.handle_exception = instrumented_handle_exception
