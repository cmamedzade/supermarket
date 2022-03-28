ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.8"
val AkkaVersion = "2.6.18"

lazy val root = (project in file("."))
  .settings(
    name := "SuperMarket"
  )
  .enablePlugins(AkkaGrpcPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.0.5",
  "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "3.0.4",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "3.0.4",
  "ch.qos.logback" % "logback-classic" % "1.2.11"
)


