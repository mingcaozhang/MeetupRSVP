lazy val root = Project("meetup", file(".")).settings(
  scalafmtOnCompile := true,
  libraryDependencies ++= Dependencies.deps
)
