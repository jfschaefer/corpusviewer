name := "corpusviewer"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "8.0.40-R8",
  "de.saar.coli" % "basics" % "1.2.34",
  "org.controlsfx" % "controlsfx" % "8.20.8",
  "it.unimi.dsi" % "fastutil" % "6.1.0",
  "org.jgrapht" % "jgrapht-jdk1.5" % "0.7.3",
  "net.sf.jung" % "jung2" % "2.0.1",
  "net.sf.jung" % "jung-visualization" % "2.0.1",
  "net.sf.jung" % "jung-algorithms" % "2.0.1",
  "net.sf.jung" % "jung-samples" % "2.0.1",
  "de.up.ling" %% "tclup-perf" % "1.0",
  "de.up.ling" % "alto" % "2.0",
  "de.up.ling" % "tuio-gesture" % "1.0.4-SNAPSHOT",
  "de.jfschaefer.sugiyamalayout" % "sugiyamalayout" % "1.0-SNAPSHOT",
  "de.jfschaefer.layeredgraphlayout" % "layeredgraphlayout" % "1.0-SNAPSHOT"
)

unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar"))

resolvers += "TCL Releases" at "http://tcl.ling.uni-potsdam.de/artifactory/release"

resolvers += "TCL Snapshots" at "http://tcl.ling.uni-potsdam.de/artifactory/snapshots"

resolvers += "TCL External" at "http://www.ling.uni-potsdam.de/tcl/maven2/external"

resolvers += Resolver.mavenLocal
