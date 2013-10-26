// Import SBT
import sbt._
import Keys._

// Import Android plugin
import AndroidKeys._

// Import Eclipse plugin
import com.typesafe.sbteclipse.plugin._
import EclipsePlugin.{ EclipseKeys, EclipseCreateSrc, EclipseExecutionEnvironment }
import EclipseKeys._

// Import Scala XML
import scala.xml.{Node,Elem,UnprefixedAttribute,Text,Null}
import scala.xml.transform.RewriteRule

// Some settings for sbteclipse with sbt-android-plugin and
// AndroidProguardScala.
object AndroidEclipseDefaults {

  // Import AndroidEclipse helpers
  import AndroidEclipse._

  // Output settings that play well with Eclipse and ADT :
  //   * Add managed sources to the Eclipse classpath
  //   * Fix Eclipse output
  //   * Put the resources, manifest and assets to the root dir
  //   * Generate typed resources prior to any Eclipse project
  lazy val outputSettings = Seq(
    // We want managed sources in addition to the default settings
    createSrc :=
      EclipseCreateSrc.Default +
      EclipseCreateSrc.Managed,

    // Environment to Java 1.6 (1.7 not supported by Android at the moment)
    executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE16),

    // Initialize Eclipse Output to None (output will default to bin/classes)
    eclipseOutput := None,

    // Fix output directories
    classpathTransformerFactories <+= (eclipseOutput) {
      d => d match {
        case Some(s) => new ClasspathOutputFixer(s)
        case None => new ClasspathOutputFixer("bin/classes")
      }
    },

    // Resources, assets and manifest must be at the project root directory
    mainResPath in Android <<=
      (baseDirectory, resDirectoryName in Android) (_ / _) map (x=>x),
    mainAssetsPath in Android <<=
      (baseDirectory, assetsDirectoryName in Android) (_ / _),
    manifestPath in Android <<=
      (baseDirectory, manifestName in Android) map ((b,m) => Seq(b / m)) map (x=>x),

    // 
    preTasks := Seq(generateTypedResources in Android)
  )

  lazy val naturesSettings = Seq(
    // Set some options inside the project
    projectTransformerFactories ++= Seq(
      // Add Android and AndroidProguardScala natures
      new Transformer[Nature]("natures", TransformType.Append, Seq(
        "com.android.ide.eclipse.adt.AndroidNature",
        "com.restphone.androidproguardscala.Nature"
      )),

      // Add resource builder before everything else
      new Transformer[Builder]("buildSpec", TransformType.Prepend, Seq(
        "com.android.ide.eclipse.adt.ResourceManagerBuilder"
      )),

      // Add proguard, pre-compiler and apk builder after everything else
      new Transformer[Builder]("buildSpec", TransformType.Append, Seq(
        "com.restphone.androidproguardscala.Builder",
        "com.android.ide.eclipse.adt.PreCompilerBuilder",
        "com.android.ide.eclipse.adt.ApkBuilder"
      ))
    ),

    // Add the Android lib/ folder to the classpath
    classpathTransformerFactories ++= Seq(
      new Transformer[ClasspathContainer]("classpath", TransformType.Append, Seq(
        "com.android.ide.eclipse.adt.LIBRARIES"
      ))
    ),

    // Remove R.java from the managed sources
    //  (clashes with the Eclipse-generated R.java)
    classpathTransformerFactories <++=
      (managedSourceDirectories in Compile) {
        m => m map {
          path => new ClasspathExclusion(
            "src", path, Seq("**/R.java")
          )
        }
      }
  )

  // Set default settings
  lazy val settings = Seq (
    classpathTransformerFactories := Seq(),
    projectTransformerFactories := Seq()
  ) ++ outputSettings ++ naturesSettings

}
