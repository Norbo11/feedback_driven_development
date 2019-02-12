#!/usr/bin/env python3

import connexion
from flask import Flask, after_this_request

from playground_application import encoder
#from elasticapm.contrib.flask import ElasticAPM

app = connexion.App(__name__, specification_dir='./swagger/')
app.add_api('swagger.yaml', arguments={'title': 'Playground Application'})

flask_app = app.app
flask_app.json_encoder = encoder.JSONEncoder
#flask.config['ELASTIC_APM'] = {
#    'SERVICE_NAME': 'PG',
#    'SECRET_TOKEN': 'vLVGtdBxiqrwadhotd',
#    'SERVER_URL': 'https://a30fe51d3dcb486c99b519a7836d5b84.apm.eu-central-1.aws.cloud.es.io:443',
#    'DEBUG': True,
#}

flask_app.config.update(
    ENV='development',
    )

#apm = ElasticAPM(flask, logging=True)

import subprocess
import asyncio
import os
import time

@flask_app.before_request
def pyflame_profile():
    the_time = time.time()
    pid = os.getpid()

    command = f"pyflame --threads --abi 36 -x -s 10 -o outputs/profile_{the_time} -p {pid} "

    process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True, encoding='utf8')

    @after_this_request
    def pyflame_profile_end(response):
        flask_app.logger.error('Terminating')
        process.kill()
        return response



if __name__ == '__main__':
    app.run(host='localhost', port=8080)
