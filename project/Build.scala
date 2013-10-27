import sbt._

import Keys._
import AndroidKeys._

object General {
  // Some basic configuration
  val settings = Defaults.defaultSettings ++ Seq (
    name := "worldmetro",
    version := "0.1",
    versionCode := 2,
    scalaVersion := "2.10.0",
    platformName in Android := "android-18",
    javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6")
  )

  // Default Proguard settings
  lazy val proguardSettings = inConfig(Android) (Seq (
    useProguard := true,
    proguardOptimizations += "-keep class net.whily.android.worldmetro.** { *; }",
    proguardOptimizations += "-keep class scala.collection.SeqLike { public java.lang.String toString(); }"
  ))

  // Full Android settings
  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    proguardSettings ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "change-me",
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"
    )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "main",
    file("."),
    settings = General.fullAndroidSettings ++ AndroidEclipseDefaults.settings
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++
               AndroidEclipseDefaults.settings ++
               AndroidTest.androidSettings ++
               General.proguardSettings ++ Seq (
      name := "worldmetroTests"
    )
  ) dependsOn main
}
