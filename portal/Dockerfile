FROM python:3.8

RUN apt update && apt install -y make g++

ENV APP_HOME /app
WORKDIR $APP_HOME
COPY . ./

WORKDIR $APP_HOME/portal
RUN cd portal & make build-tools
ENV PATH $PATH:$APP_HOME/portal/_build/demodulator/bin

RUN pip install -r requirements.txt
CMD exec gunicorn --bind :$PORT --workers 1 --threads 8 --timeout 0 app:app