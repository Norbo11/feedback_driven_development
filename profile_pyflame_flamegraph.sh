sudo env "PATH=$PATH" pyflame --threads --abi 36 -x -s 10 -o profile_flamegraph.txt -p $(cat runserver.pid)
flamegraph.pl profile_flamegraph.txt > profile_pyflame.svg

