name := "ClientMonitorApi"
enablePlugins(FlywayPlugin)

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.8"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.21"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8"

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

libraryDependencies += "com.zaxxer" % "HikariCP" % "3.3.1"

libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1200-jdbc41"

libraryDependencies += "org.flywaydb" % "flyway-core" % "5.2.4"

libraryDependencies += "ch.megard" %% "akka-http-cors" % "0.4.0"

libraryDependencies += "org.scalamock" %% "scalamock" % "4.1.0" % Test

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test
