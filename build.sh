#!/bin/sh

javac -d app/build yuizumi/eval/*.java
javac -d app/build app/*.java

cd app/build
jar cfe Main.jar Main *
