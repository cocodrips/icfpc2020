name := "icfpc2020-scala"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.0.0"
)

mainClass in assembly := Some("galaxy.GalaxyInteract")
assemblyJarName := "galaxy.jar"
