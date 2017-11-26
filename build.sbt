val circeV = "0.8.0"
val kantanV = "0.3.0"

lazy val nana = project.in(file("."))
  .settings(
    name := "nana",
    organization := "me.akrivos",
    scalaVersion := "2.12.4",
    scalacOptions ++= Seq(
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Xfuture",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Ywarn-unused",
      "-language:reflectiveCalls",
      "-language:implicitConversions"
    ),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
      "com.github.nscala-time" %% "nscala-time" % "2.18.0",
      "com.nrinaudo" %% "kantan.csv" % kantanV,
      "com.nrinaudo" %% "kantan.csv-generic" % kantanV,
      "com.nrinaudo" %% "kantan.csv-joda-time" % kantanV,
      "io.circe" %% "circe-core" % circeV,
      "io.circe" %% "circe-generic" % circeV,
      "io.circe" %% "circe-parser" % circeV,
      "org.scalatest" %% "scalatest" % "3.0.4" % Test
    )
  )
