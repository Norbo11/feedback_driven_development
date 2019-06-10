import logging

from flask import request, _app_ctx_stack


def instrument_logs():
    logging.getLogger().addHandler(FeedbackLoggingHandler())


class FeedbackLoggingHandler(logging.Handler):

    def __init__(self):
        super().__init__()
        # self.list = []
        # self.previous_request = None

    def emit(self, record):

        if request and _app_ctx_stack.top.instrumentation_metadata:
            # New request? Reset list of logging records
            # if self.previous_request != request:
            #     _app_ctx_stack.top.instrumentation_metadata.logging_lines = []

            _app_ctx_stack.top.instrumentation_metadata.logging_lines.append(record)
            # self.previous_request = request
