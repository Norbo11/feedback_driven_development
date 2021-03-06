from datetime import datetime

from flask import current_app

from metric_backend_client import NewLineExceptionFrames, NewLineException, NewLogRecord, LogRecord, PyflameProfile, NewRequestParam
from feedback_wrapper.utils import generate_flamegraph


def send_feedback(feedback_config, instrumentation_metadata, pyflame_output):
    exception = None
    if instrumentation_metadata.exception is not None:
        frames = [NewLineExceptionFrames(filename=f.filename, line_number=f.lineno, function_name=f.name)
                  for f in instrumentation_metadata.stack_summary]

        exception = NewLineException(exception_type=type(instrumentation_metadata.exception).__name__,
                                     exception_message=str(instrumentation_metadata.exception),
                                     frames=frames)

    request_params = [NewRequestParam(name=name, value=str(value), type=type(value).__name__) for name, value in instrumentation_metadata.request.args.items()]

    logging_lines = [NewLogRecord(log_record=LogRecord(log_timestamp=datetime.fromtimestamp(record.created),
                                                       logger=record.name,
                                                       level=record.levelname,
                                                       message=record.msg.format(record.args)),
                                  line_number=record.lineno,
                                  filename=record.pathname) for record in instrumentation_metadata.logging_lines]

    pyflame_profile = PyflameProfile(application_name=feedback_config.application_name,
                                     version=feedback_config.current_version,
                                     start_timestamp=instrumentation_metadata.start_time,
                                     end_timestamp=instrumentation_metadata.end_time,
                                     pyflame_output=pyflame_output,
                                     base_path=str(feedback_config.source_base_path),
                                     instrument_directories=[str(d) for d in feedback_config.instrument_directories],
                                     exception=exception,
                                     logging_lines=logging_lines,
                                     request_params=request_params,
                                     url_rule=str(instrumentation_metadata.request.url_rule)
                                     )

    feedback_config.metric_handling_api.add_pyflame_profile(pyflame_profile)
    current_app.logger.info("Feedback sent")
