import connexion
import six
import logging
import time

from flask import current_app

from playground_application.models.number import Number  # noqa: E501
from playground_application import util


def one_get():  # noqa: E501
    """One.

    The one. # noqa: E501


    :rtype: Number
    """
    #raise Exception('oh damn')
    current_app.logger.info('Executed one')
    time.sleep(1)
    return Number(1)


def three_get():  # noqa: E501
    """Three.

    The three. # noqa: E501


    :rtype: Number
    """
    current_app.logger.info('Executed three ')
    time.sleep(3)
    return Number(3)


def two_get():  # noqa: E501
    """Two.

    The two. # noqa: E501


    :rtype: Number
    """
    current_app.logger.info('Executed two')
    time.sleep(2)
    return Number(2)



