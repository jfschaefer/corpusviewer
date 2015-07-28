name := "corpusviewer"

version := "1.0"

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "8.0.40-R8",
  "de.saar.coli" % "basics" % "1.2.34",
  "org.controlsfx" % "controlsfx" % "8.20.8",
  "de.up.ling" % "alto" % "2.0",
  "de.up.ling" % "tuio-gesture" % "1.0.4-SNAPSHOT",
  "it.unimi.dsi" % "fastutil" % "6.1.0",
  "org.jgrapht" % "jgrapht-jdk1.5" % "0.7.3",
  "net.sf.jung" % "jung2" % "2.0.1",
  "net.sf.jung" % "jung-visualization" % "2.0.1",
  "net.sf.jung" % "jung-algorithms" % "2.0.1",
  "net.sf.jung" % "jung-samples" % "2.0.1",

  // tclup-perf
  "com.typesafe.slick" %% "slick" % "3.0.0",
  // "de.up.ling" % "alto" % "2.1-SNAPSHOT",
  "mysql" % "mysql-connector-java" % "latest.release",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0",
  "com.typesafe.slick" %% "slick-codegen" % "3.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.github.wookietreiber" %% "scala-chart" % "0.4.2", //%latest.integration",
  "com.zaxxer" % "HikariCP" % "2.3.3"
)

resolvers += "TCL Releases" at "http://tcl.ling.uni-potsdam.de/artifactory/release"

resolvers += "TCL Snapshots" at "http://tcl.ling.uni-potsdam.de/artifactory/snapshots"

resolvers += "TCL External" at "http://www.ling.uni-potsdam.de/tcl/maven2/external"
