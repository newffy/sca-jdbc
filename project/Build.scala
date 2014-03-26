import sbt._
import Keys._

object BuildSettings {
  val paradiseVersion = "2.0.0-SNAPSHOT"
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "ru.simplesys",
    version := "1.0.0",
    scalacOptions ++= Seq("-deprecation", "-explaintypes", "-feature", "-language:postfixOps", "-language:implicitConversions", "-language:higherKinds"),
    scalaVersion := "2.10.4",
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full),
    libraryDependencies += "com.chuusai" % "shapeless_2.10.3" % "2.0.0-SNAPSHOT" changing()// cross CrossVersion.full changing()
  )
}

object ThisBuild extends Build {
  import BuildSettings._

  lazy val root: Project = Project(
    "sca-jdbc",
    file("."),
    settings = buildSettings
  ) dependsOn(macros)

  lazy val macros: Project = Project(
    "macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies ++= (
        if (scalaVersion.value.startsWith("2.10")) List("org.scalamacros" % "quasiquotes" % paradiseVersion cross CrossVersion.full)
        else Nil
        )
    )
  )
}
