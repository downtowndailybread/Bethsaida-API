name := "ClientMonitorApi"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.7"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.21"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
