ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"
// scala 2 has better tooling and IDE support, especially inlay hints

lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(name := "unidef", idePackagePrefix := Some("com.jeekrs.unidef"))
val circeVersion = "0.14.1"

libraryDependencies += "io.circe" %% "circe-yaml" % circeVersion
libraryDependencies += "io.circe" %% "circe-parser" % circeVersion
libraryDependencies += "io.circe" %% "circe-generic" % circeVersion

// https://mvnrepository.com/artifact/org.apache.velocity/velocity-engine-core
libraryDependencies += "org.apache.velocity" % "velocity-engine-core" % "2.3"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10"
