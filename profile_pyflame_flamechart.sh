sudo env "PATH=$PATH" pyflame --flamechart --threads -x -s 10 -o profile_flamechart.txt -p $(cat runserver.pid)
cat profile_flamechart.txt | flame-chart-json > profile_flamechart.cpuprofile

