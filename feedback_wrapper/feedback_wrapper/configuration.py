import yaml
import pathlib
from yaml import CLoader

from metric_backend_client.configuration import Configuration
from metric_backend_client.api_client import ApiClient
from metric_backend_client.api.default_api import DefaultApi
from feedback_wrapper.version_provider import get_current_version

PYFLAME_ARGS = {
    'abi': 36, # Without this, error code 1 is returned
    'seconds': 9999,
    'rate': 0.01, # Default is 0.01
}


class FeedbackConfiguration():

    def __init__(self, flask_app, feedback_config_filepath):
        root_path = pathlib.Path(flask_app.root_path)
        flask_app.logger.info(f'Flask base path: {root_path}')

        with (root_path / feedback_config_filepath).open('r') as config_file:
            config = yaml.load(config_file, Loader=CLoader)

        flask_app.logger.info(f'Read config from: {root_path / feedback_config_filepath}')

        # TODO: Validate config
        backend_client_config = Configuration()
        backend_client_config.host = config['metric_backend_url']
        self.metric_handling_api = DefaultApi(ApiClient(backend_client_config))

        self.enable = config['enable'] if 'enable' in config else True
        self.send_to_backend = config['send_to_backend'] if 'send_to_backend' in config else True
        self.pyflame_args = dict(PYFLAME_ARGS)

        if 'pyflame_sampling_rate' in config:
            self.pyflame_args['rate'] = config['pyflame_sampling_rate'] 

        self.application_name = config['application_name']
        flask_app.logger.info(f'Application name: {self.application_name}')

        self.git_base_path = (root_path / config['git_base_path']).resolve()
        flask_app.logger.info(f'Git base path: {self.git_base_path}')

        self.source_base_path = (root_path / config['source_base_path']).resolve()
        flask_app.logger.info(f'Source base path: {self.source_base_path}')

        self.current_version = get_current_version(self.git_base_path)
        flask_app.logger.info(f'Current version: {self.current_version}')

        self.instrument_directories = config[ 'instrument_file_globs']

        flask_app.logger.info(f'Instrument directories: {[str(d) for d in self.instrument_directories]}')

