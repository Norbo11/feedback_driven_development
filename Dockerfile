FROM python:3.6.5
WORKDIR /app

RUN apt-get update && apt-get install --yes vim

COPY feedback_wrapper/requirements.txt /app/feedback_wrapper/requirements.txt
RUN pip install --trusted-host pypi.python.org -r feedback_wrapper/requirements.txt

COPY playground_application/requirements.txt /app/playground_application/requirements.txt
RUN pip install --trusted-host pypi.python.org -r playground_application/requirements.txt

COPY playground_application/.git /app/playground_application/.git
COPY feedback_wrapper/pyflame-bleeding /app/feedback_wrapper/pyflame-bleeding
COPY playground_application/playground_application /app/playground_application/playground_application
COPY feedback_wrapper/feedback_wrapper /app/feedback_wrapper/feedback_wrapper
COPY feedback_wrapper/metric_backend_client /app/feedback_wrapper/metric_backend_client

EXPOSE 8081
ENV FLASK_ENV=development PATH="/app/feedback_wrapper/:${PATH}" PYTHONPATH="/app/feedback_wrapper/:/app/playground_application/:${PYTHONPATH}"
CMD ["python", "-m", "playground_application.__main__"]