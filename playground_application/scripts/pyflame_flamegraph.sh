OUTPUT=outputs/profile_flamegraph.txt

#sudo env "PATH=$PATH" pyflame --threads --abi 36 -x -s 10 -o $OUTPUT -p $(cat runserver.pid)
pyflame --threads --abi 36 -x -s 10 -o $OUTPUT -p $(cat runserver.pid)
flamegraph.pl $OUTPUT > profile_pyflame.svg

