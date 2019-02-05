#!/usr/bin/env python3

import connexion
from flask import Flask

from playground_application import encoder
from elasticapm.contrib.flask import ElasticAPM


def main():
    app = connexion.App(__name__, specification_dir='./swagger/')
    app.add_api('swagger.yaml', arguments={'title': 'Playground Application'})

    flask = app.app
    flask.json_encoder = encoder.JSONEncoder
    flask.config['ELASTIC_APM'] = {
        'SERVICE_NAME': 'PG',
        'SECRET_TOKEN': 'vLVGtdBxiqrwadhotd',
        'SERVER_URL': 'https://a30fe51d3dcb486c99b519a7836d5b84.apm.eu-central-1.aws.cloud.es.io:443',
        'DEBUG': True,
    }

    apm = ElasticAPM(flask, logging=True)
    app.run(host='localhost', port=8080)


if __name__ == '__main__':
    main()
