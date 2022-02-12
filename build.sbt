ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.1"


lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "unidef",
    idePackagePrefix := Some("com.jeekrs.unidef")

    )
val circeVersion = "0.14.1"

libraryDependencies += "io.circe" %% "circe-yaml" % circeVersion
libraryDependencies += "io.circe" %% "circe-parser" % circeVersion
libraryDependencies += "io.circe" %% "circe-generic" % circeVersion


