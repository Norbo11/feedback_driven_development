#!/usr/bin/zsh

python example.py &
PID=$!
pyflame -s 99999 --abi 36 -x --threads -o profile.txt -p $PID &
echo $!
wait $PID
flamegraph.pl profile.txt > profile.svg


