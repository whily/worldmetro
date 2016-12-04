resolvers += Resolver.url("scalasbt releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("org.scala-android" % "sbt-android" % "1.7.1")
