ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.1"


lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "unidef",
    idePackagePrefix := Some("com.jeekrs.unidef")

    )

libraryDependencies += "io.circe" %% "circe-yaml" % "0.14.1"


