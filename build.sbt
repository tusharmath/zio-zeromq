ThisBuild / scalaVersion := "2.13.1"

lazy val zioVersion = "1.0.0-RC18"

ThisBuild / libraryDependencies := Seq(
  "org.zeromq" % "jeromq"             % "0.5.2",
  "dev.zio"    %% "zio"               % zioVersion,
  "dev.zio"    %% "zio-test"          % zioVersion % "test",
  "dev.zio"    %% "zio-test-sbt"      % zioVersion % "test"
)

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
