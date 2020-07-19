
## Build

```
brew install sbt
sbt compile assembly
cp target/scala-2.13/galaxy.jar .
```

## Debug Run

```
sbt runMain galaxy.GalaxyInteract
```


## Run

```
java -Xss1g -jar seglang.jar
```

