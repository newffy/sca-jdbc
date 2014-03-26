name := "sca-jdbc"

version := "1.0.0-SNAPSHOT"

organization := "ru.simplesys"

//scalaVersion := "2.10.3"

//javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

//scalacOptions ++= Seq("-deprecation", "-explaintypes", /*"-unchecked",*/ "-feature", "-language:postfixOps", "-language:implicitConversions", "-language:reflectiveCalls" /*, "-Yvirtualize"*/)

libraryDependencies ++= {
    Seq(
//	      "com.chuusai" % "shapeless" % "2.0.0-SNAPSHOT" cross CrossVersion.full changing(),
	      "org.scalaz" %% "scalaz-core" % "7.0.6",
//
        "com.simplesys" %% "common" % "1.0.0-SNAPSHOT" % "test",
        "joda-time" % "joda-time" % "2.2",
        "org.joda" % "joda-convert" % "1.3.1",
        "org.scalatest" %% "scalatest" % "2.1.0" % "test"
    )
}

