# coding: utf-8

from __future__ import absolute_import
from datetime import date, datetime  # noqa: F401

from typing import List, Dict  # noqa: F401

from playground_application.models.base_model_ import Model
from playground_application import util


class Number(Model):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    """

    def __init__(self, i: int=None):  # noqa: E501
        """Number - a model defined in Swagger

        :param i: The i of this Number.  # noqa: E501
        :type i: int
        """
        self.swagger_types = {
            'i': int
        }

        self.attribute_map = {
            'i': 'i'
        }

        self._i = i

    @classmethod
    def from_dict(cls, dikt) -> 'Number':
        """Returns the dict as a model

        :param dikt: A dict.
        :type: dict
        :return: The Number of this Number.  # noqa: E501
        :rtype: Number
        """
        return util.deserialize_model(dikt, cls)

    @property
    def i(self) -> int:
        """Gets the i of this Number.


        :return: The i of this Number.
        :rtype: int
        """
        return self._i

    @i.setter
    def i(self, i: int):
        """Sets the i of this Number.


        :param i: The i of this Number.
        :type i: int
        """
        if i is None:
            raise ValueError("Invalid value for `i`, must not be `None`")  # noqa: E501

        self._i = i