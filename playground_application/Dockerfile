FROM python:3.6.5
WORKDIR /app
COPY requirements.txt /app/requirements.txt
RUN pip install --trusted-host pypi.python.org -r requirements.txt
RUN apt-get update && apt-get install --yes vim
COPY pyflame-bleeding /app/pyflame-bleeding
COPY metric_backend_client /app/metric_backend_client
COPY playground_application /app/playground_application
COPY scripts/example.py /app/example.py
EXPOSE 8081
ENV FLASK_ENV=development PATH="/app:${PATH}"
CMD ["python", "-m", "playground_application.__main__"]
