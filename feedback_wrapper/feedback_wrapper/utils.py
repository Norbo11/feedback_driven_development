import os
import subprocess

from flask import current_app


def generate_flamegraph(directory, pyflame_output, filename):
    command = 'flamegraph.pl'
    process = subprocess.Popen(command.split(' '), stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    stdout, stderr = process.communicate(input=pyflame_output)

    pathname = os.path.join(directory, 'flamegraphs', f'{filename}.svg')
    with open(pathname, 'w') as file:
        file.write(stdout)

    pathname = os.path.join(directory, 'flamegraphs', f'{filename}.txt')
    with open(pathname, 'w') as file:
        file.write(pyflame_output)

    current_app.logger.info(f'Wrote flamegraph to: {pathname}')
