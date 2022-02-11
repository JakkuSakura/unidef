ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.1"

enablePlugins(NativeImagePlugin)

lazy val root = (project in file("."))
  .settings(
    name := "unidef",
    idePackagePrefix := Some("com.jeekrs.unidef")
  )
