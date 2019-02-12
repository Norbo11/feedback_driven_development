uwsgi --enable-threads --http-socket 127.0.0.1:8080 --module playground_application.__main__:flask_app > run_uwsgi_server.log 2>&1 &

