OUTPUT=outputs/pyflame_flamechart.txt

sudo env "PATH=$PATH" pyflame --flamechart --threads -x -s 10 -o $OUTPUT -p $(cat runserver.pid)
cat $OUTPUT | flame-chart-json > pyflame_flamechart.cpuprofile

