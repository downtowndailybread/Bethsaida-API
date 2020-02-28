import sbtassembly.MergeStrategy

name := "ClientMonitorApi"

enablePlugins(FlywayPlugin)

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.8"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.21"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8"

libraryDependencies += "com.zaxxer" % "HikariCP" % "3.3.1"

libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1200-jdbc41"

libraryDependencies += "org.flywaydb" % "flyway-core" % "5.2.4"

libraryDependencies += "ch.megard" %% "akka-http-cors" % "0.4.0"

libraryDependencies += "com.auth0" % "java-jwt" % "3.8.0"

libraryDependencies += "org.dmfs" % "lib-recur" % "0.11.2"

libraryDependencies += "software.amazon.awssdk" % "s3" % "2.10.71"

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-core" % "4.0.0"

libraryDependencies += "org.scalamock" %% "scalamock" % "4.1.0" % Test

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.1" % Test

libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.19" % Test

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.1.8" % Test

assemblyMergeStrategy in assembly := {
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case x if x.contains("module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-n", "org.downtowndailybread.bethsaida.tag.UnitTest", "-oD")
