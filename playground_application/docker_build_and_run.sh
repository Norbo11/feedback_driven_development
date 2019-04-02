#!/bin/bash
docker build --tag=playground_application . && \
docker run --cap-add=SYS_PTRACE -p 4000:8081 playground_application
