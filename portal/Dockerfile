FROM rust:1.45 as rust-env
WORKDIR /work
COPY . ./
RUN pwd
RUN ls

WORKDIR /work/_build/modulator

RUN mkdir -p bin
RUN rustc -O main.rs -o bin/modulator

# ----------------------------
FROM gcc:10 as gcc-env
RUN apt update && apt install -y make g++
WORKDIR /work
COPY . ./

WORKDIR /work/_build/demodulator
RUN make build
RUN ls bin

# ----------------------------
FROM python:3.8 as python

ENV APP_HOME /app
WORKDIR $APP_HOME

COPY . ./
RUN mkdir -p /app/bin

COPY --from=rust-env  /work/_build/modulator/bin/modulator /app/bin
COPY --from=gcc-env  /work/_build/demodulator/bin/demodulator /app/bin
RUN rm -rf _build

ENV PATH $PATH:$APP_HOME/bin
RUN pip install -r requirements.txt
CMD exec gunicorn --bind :$PORT --workers 1 --threads 8 --timeout 0 app:app