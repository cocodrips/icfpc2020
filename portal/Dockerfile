FROM python:3.8

RUN apt update && apt install -y make g++

ENV APP_HOME /app
WORKDIR $APP_HOME
COPY portal /app

RUN make build-tools
ENV PATH $PATH:$APP_HOME/_build/demodulator/bin

RUN pip install -r requirements.txt
CMD exec gunicorn --bind :$PORT --workers 1 --threads 8 --timeout 0 app:app