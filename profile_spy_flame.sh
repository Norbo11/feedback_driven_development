sudo env "PATH=$PATH" py-spy -d 10 --flame profile_spy_flamegraph.svg -p $(cat runserver.pid)

