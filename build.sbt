ThisBuild / scalaVersion := "2.13.8"
ThisBuild / organization := "com.jeekrs"
ThisBuild / version := "0.1.0-SNAPSHOT"
Global / onChangedBuildSource := ReloadOnSourceChanges
// scala 2 has better tooling and IDE support, especially inlay hints
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(name := "unidef")

nativeImageOptions ++= List(
  s"-H:ConfigurationFileDirectories=${target.value / "native-image-configs"}",
  s"-H:ReflectionConfigurationFiles=${target.value / "native-image-configs" / "reflect-config.json"}",
  s"-H:ResourceConfigurationFiles=${target.value / "native-image-configs" / "resource-config.json"}",
  "-H:+JNI",
  "--no-fallback",
  "--allow-incomplete-classpath",
  "--no-server"
)

val circeVersion = "0.14.1"

libraryDependencies += "io.circe" %% "circe-yaml" % circeVersion
libraryDependencies += "io.circe" %% "circe-parser" % circeVersion
libraryDependencies += "io.circe" %% "circe-generic" % circeVersion

// https://mvnrepository.com/artifact/org.apache.velocity/velocity-engine-core
libraryDependencies += "org.apache.velocity" % "velocity-engine-core" % "2.3"
libraryDependencies += "org.apache.velocity.tools" % "velocity-tools-generic" % "3.1"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10"

libraryDependencies += "com.github.jsqlparser" % "jsqlparser" % "4.3"

// https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.12.0"
