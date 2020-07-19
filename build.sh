#!/bin/sh

javac -d app/build yuizumi/eval/*.java
javac -d app/build yuizumi/GalaxyReader.java
javac -d app/build app/*.java
cp official/galaxy.txt app/build

cd app/build
jar cfe Main.jar app.Main *
