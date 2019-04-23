import yaml
import pathlib
from yaml import CLoader

from metric_backend_client.configuration import Configuration
from metric_backend_client.api_client import ApiClient
from metric_backend_client.api.default_api import DefaultApi
from feedback_wrapper.version_provider import get_current_version

class FeedbackConfiguration():

    def __init__(self, flask_app, feedback_config_filepath):
        root_path = pathlib.Path(flask_app.root_path)
        flask_app.logger.info(f'Flask base path: {root_path}')

        with (root_path / feedback_config_filepath).open('r') as config_file:
            config = yaml.load(config_file, Loader=CLoader)

        # TODO: Validate config
        backend_client_config = Configuration()
        backend_client_config.host = config['metric_backend_url']
        self.metric_handling_api = DefaultApi(ApiClient(backend_client_config))

        self.git_base_path = (root_path / config['git_base_path']).resolve()
        flask_app.logger.info(f'Git base path: {self.git_base_path}')

        self.source_base_path = (root_path / config['source_base_path']).resolve()
        flask_app.logger.info(f'Source base path: {self.source_base_path}')

        self.current_version = get_current_version(self.git_base_path)
        flask_app.logger.info(f'Current version: {self.current_version}')

        self.instrument_directories = config[ 'instrument_file_globs']

        flask_app.logger.info(f'Instrument directories: {[str(d) for d in self.instrument_directories]}')

        self.application_name = config['application_name']