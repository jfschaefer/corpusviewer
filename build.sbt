name := "corpusviewer"

version := "1.0"

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "8.0.40-R8",
  "de.saar.coli" % "basics" % "1.2.34",
  //"de.up.ling" % "irtg" % "1.1-SNAPSHOT",
  "org.controlsfx" % "controlsfx" % "8.20.8",
  //"de.up.ling" %% "alto" % "1.1-SNAPSHOT" from "http://tcl.ling.uni-potsdam.de/artifactory/snapshots/de/up/ling/alto/1.1-SNAPSHOT/alto-1.1-20150701.120313-37.pom",
  //"de.up.ling" %% "alto" % "1.1-SNAPSHOT" from "http://tcl.ling.uni-potsdam.de/artifactory/snapshots/de/up/ling/alto/1.1-SNAPSHOT/alto-1.1-20150701.120313-37-jar-with-dependencies.jar",
  //"de.upling" % "alto" % "1.1-SNAPSHOT" from "http://tcl.ling.uni-potsdam.de/artifactory/snapshots/de/up/ling/alto/1.1-SNAPSHOT/alto-1.1-20150629.140311-32.pom",
  "de.up.ling" % "alto" % "1.1-SNAPSHOT" from "http://tcl.ling.uni-potsdam.de/artifactory/snapshots/de/up/ling/alto/1.1-SNAPSHOT/alto-1.1-20150629.140311-32-jar-with-dependencies.jar",
  "de.up.ling" % "tuio-gesture" % "1.0.3-SNAPSHOT",
  "it.unimi.dsi" % "fastutil" % "6.1.0",
  "org.jgrapht" % "jgrapht-jdk1.5" % "0.7.3",
  "net.sf.jung" % "jung2" % "2.0.1",
  "net.sf.jung" % "jung-visualization" % "2.0.1",
  "net.sf.jung" % "jung-algorithms" % "2.0.1",
  "net.sf.jung" % "jung-samples" % "2.0.1"
)

resolvers += "TCL Releases" at "http://tcl.ling.uni-potsdam.de/artifactory/release"

resolvers += "TCL Snapshots" at "http://tcl.ling.uni-potsdam.de/artifactory/snapshots"

resolvers += "TCL External" at "http://www.ling.uni-potsdam.de/tcl/maven2/external"
