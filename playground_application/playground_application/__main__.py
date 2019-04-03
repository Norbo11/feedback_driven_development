#!/usr/bin/env python3

import connexion
from flask import Flask, after_this_request, request

from metric_backend_client import DefaultApi, ApiClient, Configuration, PyflameProfile
from playground_application import encoder
from datetime import datetime

import subprocess
import asyncio
import os
import time
import signal

app = connexion.App(__name__, specification_dir='./swagger/')
app.add_api('swagger.yaml', arguments={'title': 'Playground Application'})

flask_app = app.app
flask_app.json_encoder = encoder.JSONEncoder
flask_app.config.update(
    ENV='development',
    DEBUG=True,
)

config = Configuration()
config.host = "http://host.docker.internal:8080/api"
metric_handling_api = DefaultApi(ApiClient(config))

@flask_app.before_request
def pyflame_profile():
    pid = os.getpid()

    name = request.url[-3:]
    #command = f"pyflame-bleeding --threads --abi 36 -x -s 10 --flamechart -o outputs/profile_{name}_{the_time} -p {pid} "
    command = f"pyflame-bleeding --threads --abi 36 -x -s 10 -p {pid} "
    start_time = datetime.now()

    flask_app.logger.error(f'Running {command}')
    process = subprocess.Popen(command.split(' '), stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    flask_app.logger.error(f'Ran')

    @after_this_request
    def pyflame_profile_end(response):

        def kill_process():
            #flask_app.logger.error('Terminating')
            process.send_signal(signal.SIGINT)
            #flask_app.logger.error('Sent')

            try:
                stdout, stderr = process.communicate(timeout=0.1)
                return stdout, stderr
            except subprocess.TimeoutExpired as e:
                return kill_process()

        stdout, stderr = kill_process()
        return_code = process.poll()
        flask_app.logger.error(f'Finished')

        if return_code != 0:
            flask_app.logger.error(f'pyflame returned status code {return_code}: \nstdout: {stdout}\nstderr: {stderr}')

            if return_code == -2:
                flask_app.logger.error(f'Request possibly ran for too short')
            return response

        end_time = datetime.now()
        pyflame_profile = PyflameProfile(start_timestamp=start_time, end_timestamp=end_time, pyflame_output=stdout)
        flask_app.logger.error(f'Stdout: ')
        flask_app.logger.error(stdout)
        metric_handling_api.add_pyflame_profile(pyflame_profile)

        return response

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8081, debug=True)
