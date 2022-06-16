import ReleaseTransformations._

ThisBuild / scalaVersion := "3.1.2"
ThisBuild / organization := "com.jeekrs"

releaseUseGlobalVersion := false
Global / onChangedBuildSource := ReloadOnSourceChanges

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "jitpack" at "https://jitpack.io"

releaseProcess := Seq[ReleaseStep](
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  setNextVersion,
  commitNextVersion
)
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

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10"

libraryDependencies += "com.github.jsqlparser" % "jsqlparser" % "4.3"
// https://mvnrepository.com/artifact/com.alibaba/druid
libraryDependencies += "com.alibaba" % "druid" % "1.2.10"

// https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.12.0"

libraryDependencies += "com.github.saasquatch" % "json-schema-inferrer" % "0.1.4"

libraryDependencies += "org.scala-lang" %% "scala3-tasty-inspector" % scalaVersion.value

// https://mvnrepository.com/artifact/com.google.jimfs/jimfs
libraryDependencies += "com.google.jimfs" % "jimfs" % "1.2"

// https://mvnrepository.com/artifact/org.apache.commons/commons-text
libraryDependencies += "org.apache.commons" % "commons-text" % "1.9"

// https://mvnrepository.com/artifact/org.apache.commons/commons-io
libraryDependencies += "org.apache.commons" % "commons-io" % "1.3.2"

// https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
libraryDependencies += "org.junit.jupiter" % "junit-jupiter-api" % "5.8.2" % Test
