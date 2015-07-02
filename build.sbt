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
  "de.upling" % "alto" % "1.1-SNAPSHOT" from "http://tcl.ling.uni-potsdam.de/artifactory/snapshots/de/up/ling/alto/1.1-SNAPSHOT/alto-1.1-20150629.140311-32-jar-with-dependencies.jar",
  "de.up.ling" % "tuio-gesture" % "1.0.3-SNAPSHOT"
)

resolvers += "TCL Releases" at "http://tcl.ling.uni-potsdam.de/artifactory/release"

resolvers += "TCL Snapshots" at "http://tcl.ling.uni-potsdam.de/artifactory/snapshots"

resolvers += "TCL External" at "http://www.ling.uni-potsdam.de/tcl/maven2/external"
