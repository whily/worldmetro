import sbt._

import Keys._
//import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "worldmetro",
    //version := "0.1",
    //versionCode := 2,
    scalaVersion := "2.10",
    //platformName := "android-18",
    javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6")
  )

  val proguardSettings = Seq (
    //useProguard := true
  )

  lazy val fullAndroidSettings =
    General.settings ++
    //AndroidPlugin.androidDefaults ++
    // AndroidProject.androidSettings ++
    //TypedResources.settings ++
    proguardSettings //++
    //AndroidManifestGenerator.settings ++
    //AndroidMarketPublish.settings ++ Seq (
    //  keyalias in Android := "change-me",
    //  libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.2" % "test"
    //)
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "worldmetro",
    file("."),
    settings = General.fullAndroidSettings
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++
               //AndroidTest.androidSettings ++
               General.proguardSettings ++ Seq (
      name := "WorldMetroTests"
    )
  ) dependsOn main
}
