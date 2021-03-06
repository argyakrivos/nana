val circeV = "0.9.0-M2"
val kantanV = "0.3.0"
val akkaHttpV = "10.0.10"
val finchV = "0.16.0-M5"

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
    coverageExcludedPackages := ".*Main.*",
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
      "com.twitter" %% "twitter-server" % "17.11.0",
      "com.github.finagle" %% "finch-core" % finchV,
      "com.github.finagle" %% "finch-circe" % finchV,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.2",
      "com.typesafe.akka" %% "akka-http" % akkaHttpV,
      "com.typesafe.akka" %% "akka-slf4j" % "2.4.19",
      "de.heikoseeberger" %% "akka-http-circe" % "1.18.0",
      "com.auth0" % "java-jwt" % "3.3.0",
      "com.auth0" % "jwks-rsa" % "0.3.0",
      "org.scalatest" %% "scalatest" % "3.0.4" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test
    )
  )
