import os
import signal
import subprocess

from flask import current_app, _app_ctx_stack

def start_pyflame(pyflame_args):
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
        f"-p {pid} "
    # f"--flamechart "
    current_app.logger.info(f'Running {command}')
    process = subprocess.Popen(command.split(' '), stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    return process


def stop_pyflame():
    current_app.logger.info("Sending SIGINT to PyFlame")
    _app_ctx_stack.top.instrumentation_metadata.pyflame_process.send_signal(signal.SIGINT)

    try:
        stdout, stderr = _app_ctx_stack.top.instrumentation_metadata.pyflame_process.communicate(timeout=0.1)
        return_code = _app_ctx_stack.top.instrumentation_metadata.pyflame_process.poll()
        return stdout, stderr, return_code
    except subprocess.TimeoutExpired as e:
        return stop_pyflame()

