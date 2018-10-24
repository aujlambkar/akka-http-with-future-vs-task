lazy val akkaHttpVersion = "10.1.4"
lazy val akkaVersion    = "2.5.16"
lazy val remoteNexusRepo = "10.154.3.140:8123/"
val pkgVersion = "2.0.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.ril",
      scalaVersion    := "2.12.6"
    )),
    name := "activity-discovery",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "javax.ws.rs"       %  "javax.ws.rs-api"      % "2.1" artifacts( Artifact("javax.ws.rs-api", "jar", "jar")),
      "org.apache.kafka"  %   "kafka-clients"       % "2.0.0",
      "org.apache.kafka"  % "kafka-streams"         % "2.0.0",
      "org.apache.kafka"  %% "kafka-streams-scala"  % "2.0.0",
      "org.slf4j"         %   "slf4j-log4j12"       % "1.7.25",

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test,
      "net.manub"         %% "scalatest-embedded-kafka" % "2.0.0"     % Test
    )
  )

mainClass in Compile := Some("com.ril.d2d.Application")
dockerBaseImage := "frolvlad/alpine-oraclejdk8"
dockerExposedPorts := Seq(7001)
packageName in Docker := remoteNexusRepo + "d2d/activity-discovery"
version in Docker := pkgVersion

resourceGenerators in Compile += Def.task {
  val file = new File("build.version")
  val contents = "image=" + remoteNexusRepo + "d2d/activity-discovery:" + pkgVersion
  IO.write(file, contents)
  Seq(file)
}.taskValue

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)
