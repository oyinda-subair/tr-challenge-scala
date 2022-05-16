ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val AkkaVersion = "2.6.19"
val AkkaHttpVersion = "10.2.9"
val PlayJsonVersion = "2.9.2"
val PlayJsonSupportVersion = "1.39.2"
val MongoDbVersion = "4.6.0"
val AkkaHttpCorsVersion = "1.1.3"
val Slf4jVersion = "1.7.36"
val QosVersion = "1.2.11"


val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
val akkaStream = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
val akkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
val akkaHttpCors = "ch.megard" %% "akka-http-cors" % AkkaHttpCorsVersion

val playJson = "com.typesafe.play" %% "play-json" % PlayJsonVersion
val playJsonSupport = "de.heikoseeberger" %% "akka-http-play-json" % PlayJsonSupportVersion

val slf4j      = "org.slf4j" % "slf4j-api" % Slf4jVersion
val qos            = "ch.qos.logback" % "logback-classic" % QosVersion
val qosCore       = "ch.qos.logback" % "logback-core" % QosVersion

val akkaHttpTest = "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
val scalaTest = "org.scalatest" %% "scalatest" % "3.2.11" % Test
val akkaStreamTest = "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion
val akkaTest = "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion

val mongodb = "org.mongodb.scala" %% "mongo-scala-driver" % MongoDbVersion


lazy val commonSettings = Seq(
  name := "tr-backend-challenge-scala",
  resolvers ++= Seq(
    "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
  )
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      akkaActor,
      akkaStream,
      akkaHttp,
      akkaHttpCors,
      playJson,
      playJsonSupport,
      slf4j,
      qos,
      qosCore,
      mongodb,
      akkaHttpTest,
      scalaTest,
      akkaStreamTest,
      akkaTest
    )
  )