# coding: utf-8

from __future__ import absolute_import

from flask import json
from six import BytesIO

from playground_application.models.number import Number  # noqa: E501
from playground_application.test import BaseTestCase


class TestDefaultController(BaseTestCase):
    """DefaultController integration test stubs"""

    def test_one_get(self):
        """Test case for one_get

        One.
        """
        response = self.client.open(
            '/norbert6/playground-application/0.0.1/one',
            method='GET')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_three_get(self):
        """Test case for three_get

        Three.
        """
        response = self.client.open(
            '/norbert6/playground-application/0.0.1/three',
            method='GET')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_two_get(self):
        """Test case for two_get

        Two.
        """
        response = self.client.open(
            '/norbert6/playground-application/0.0.1/two',
            method='GET')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))


if __name__ == '__main__':
    import unittest
    unittest.main()
