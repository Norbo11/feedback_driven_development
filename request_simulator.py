import requests
import random
import time
import logging


logging.basicConfig(level=logging.DEBUG)

logger = logging.getLogger(__name__)

BASE_PATH = 'http://localhost:8080/norbert6/playground-application/0.0.1/'


def main():
    logger.info('Starting requests simulator')
    time.sleep(2)

    reqs = 0
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

        try:
            requests.get(endpoint)
        except:
            break

        reqs += 1 
        time.sleep(1)

    logger.info(f'Sent {reqs} requests')
    
if __name__ == '__main__':
    main()
