name := "CarsEvaluator"

version := "1.0.1"

lazy val `carsevaluator` = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
// Resolver is needed only for SNAPSHOT versions
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice, filters)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.webjars" % "bootstrap" % "4.0.0-1" exclude("org.webjars", "jquery")
libraryDependencies += "org.webjars" % "jquery" % "3.3.1-1"
libraryDependencies += "org.webjars" % "font-awesome" % "4.7.0"
libraryDependencies += "org.webjars" % "bootstrap-datepicker" % "1.8.0" exclude("org.webjars", "bootstrap")
libraryDependencies += "com.adrianhurt" %% "play-bootstrap-core" % "1.4-P26-SNAPSHOT"
libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B4-SNAPSHOT"
libraryDependencies += "org.webjars" % "chartjs" % "2.7.2"

routesGenerator := InjectedRoutesGenerator