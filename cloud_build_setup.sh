#!/bin/sh
mkdir -p portal/_build
cp -r modem portal/_build/

mkdir -p portal/api/thirdparty
cp -r python/*.py portal/api/thirdparty/