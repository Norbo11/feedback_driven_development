import connexion
import six
import logging

logger = logging.getLogger(__name__)

from playground_application.models.number import Number  # noqa: E501
from playground_application import util


def one_get():  # noqa: E501
    """One.

    The one. # noqa: E501


    :rtype: Number
    """
    raise Exception('oh damn')
    return Number(1)


def three_get():  # noqa: E501
    """Three.

    The three. # noqa: E501


    :rtype: Number
    """
    logger.info('Executed three')
    return Number(3)


def two_get():  # noqa: E501
    """Two.

    The two. # noqa: E501


    :rtype: Number
    """
    return Number(2)
