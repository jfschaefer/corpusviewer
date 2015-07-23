import sbt._

object MyBuild extends Build {
    lazy val root = Project("root", file(".")).dependsOn(sugLayout)
    lazy val sugLayout = RootProject(uri("https://github.com/jfschaefer/sugiyamalayout.git"))
}
