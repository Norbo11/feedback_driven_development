import os
import subprocess

from flask import current_app


def generate_flamegraph(directory, pyflame_output, profile_id):
    command = 'flamegraph.pl'
    process = subprocess.Popen(command.split(' '), stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    stdout, stderr = process.communicate(input=pyflame_output)

    filename = os.path.join(directory, 'flamegraphs', f'{profile_id}.svg')
    with open(filename, 'w') as file:
        file.write(stdout)

    filename = os.path.join(directory, 'flamegraphs', f'{profile_id}.txt')
    with open(filename, 'w') as file:
        file.write(pyflame_output)

    current_app.logger.info(f'Wrote flamegraph to: {filename}')