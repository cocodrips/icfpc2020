FROM rust:1.45 as rust-env
WORKDIR /work
COPY . ./

WORKDIR /work/_build/modulator
RUN mkdir -p bin
RUN rustc -O main.rs -o bin/modulator

FROM python:3.8 as python

RUN apt update && apt install -y make g++
ENV APP_HOME /app
WORKDIR $APP_HOME
COPY . ./

RUN mkdir -p /app/_build/demodulator/bin
COPY --from=rust-env  /work/_build/modulator/bin /app/_build/modulator/bin

RUN make build-tools
ENV PATH $PATH:$APP_HOME/_build/demodulator/bin
ENV PATH $PATH:$APP_HOME/_build/modulator/bin
RUN echo $PATH

RUN pip install -r requirements.txt
CMD exec gunicorn --bind :$PORT --workers 1 --threads 8 --timeout 0 app:app