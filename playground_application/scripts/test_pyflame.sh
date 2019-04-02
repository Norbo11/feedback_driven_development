#!/usr/bin/zsh

python example.py &
PID=$!
pyflame-bleeding -s 9999999 --abi 36 -x --threads -o profile.txt -p $PID &
PID2=$!

echo "example.py: $PID1"
echo "pyflame-bleeding: $PID2"

#sleep 2
#kill -SIGINT $PID2
#flamegraph.pl profile.txt > profile.svg


