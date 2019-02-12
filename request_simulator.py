import requests
import random
import time
import logging


logging.basicConfig(level=logging.DEBUG)

logger = logging.getLogger(__name__)

BASE_PATH = 'http://localhost:8080/norbert6/playground-application/0.0.1/'


def main():
    logger.info('Starting requests simulator')

    while True:
        i = random.randint(1, 3)
        endpoint = ''

        if i == 1:
            endpoint = 'one'

        if i == 2:
            endpoint = 'two'

        if i == 3:
            endpoint = 'three'

        endpoint = BASE_PATH + endpoint

        logger.info(f'Sending requests to {endpoint}')

        requests.get(endpoint)

        time.sleep(1)

if __name__ == '__main__':
    main()
