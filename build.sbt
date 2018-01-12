import android.Keys._
import android.Dependencies.aar

enablePlugins(AndroidApp)

name := "worldmetro"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "net.whily" %% "scasci" % "0.0.1-SNAPSHOT",
  "net.whily" %% "scaland" % "0.0.1-SNAPSHOT")

platformTarget in Android := "android-23"

proguardCache in Android += "net.whily.scasci"
proguardCache in Android += "net.whily.scaland"

proguardOptions in Android ++= Seq(
  "-dontobfuscate",
  "-dontoptimize",
  "-keepattributes Signature,InnerClasses,EnclosingMethod",
  "-dontwarn scala.collection.**",
  "-dontwarn sun.misc.Unsafe",
  "-keep class net.whily.android.worldmetro.** { *; }",
  "-keep class scala.collection.SeqLike { public java.lang.String toString(); }")

scalacOptions in Compile ++= Seq("-feature", "-deprecation", "-Xexperimental")

javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6")

javacOptions in Compile  += "-deprecation"

run <<= run in Android

install <<= install in Android
