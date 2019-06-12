import os
import signal
import subprocess
import tempfile

from flask import current_app, _app_ctx_stack

def start_pyflame(pyflame_args):
    _app_ctx_stack.top.instrumentation_metadata.pyflame_output_file_stdout = stdout = tempfile.NamedTemporaryFile("w+")

    pid = os.getpid()
    # TODO: Run forever instead of 10 seconds
    # TODO: Don't hardcode ABI
    # TODO: Use pyspy
    # TODO: Look into the effects of "idle" time
    # TODO: Look into the effects of rate
    command = f"pyflame-bleeding " \
        f"--threads " \
        f"--exclude-idle " \
        f"--abi {pyflame_args['abi']} " \
        f"--rate {pyflame_args['rate']} " \
        f"--seconds {pyflame_args['seconds']} " \
        f"--o {stdout.name} "  \
        f"-p {pid} "
    # f"--flamechart "
    current_app.logger.info(f'Running {command}')

    process = subprocess.Popen(command.split(' '), stderr=subprocess.PIPE, universal_newlines=True)
    return process


def stop_pyflame():
    current_app.logger.info("Sending SIGINT to PyFlame")
    _app_ctx_stack.top.instrumentation_metadata.pyflame_process.send_signal(signal.SIGINT)
    try:
        proc = _app_ctx_stack.top.instrumentation_metadata.pyflame_process
        _, stderr = _app_ctx_stack.top.instrumentation_metadata.pyflame_process.communicate(timeout=0.5)

        # We read PyFlame output from a temporary file because subprocess cannot handle large outputs through pipes without
        # deadlocking: https://thraxil.org/users/anders/posts/2008/03/13/Subprocess-Hanging-PIPE-is-your-enemy/
        stdout = _app_ctx_stack.top.instrumentation_metadata.pyflame_output_file_stdout.read()
        return_code = _app_ctx_stack.top.instrumentation_metadata.pyflame_process.poll()

        return stdout, stderr, return_code
    except subprocess.TimeoutExpired as e:
        return stop_pyflame()

