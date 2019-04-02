./killserver.sh
PYTHONPATH=../:$PYTHONPATH python -m playground_application.__main__ > runserver.log 2>&1 &
echo $! > runserver.pid
./request_simulator.sh
