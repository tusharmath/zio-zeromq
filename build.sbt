ThisBuild / scalaVersion := "2.13.1"

lazy val zioVersion = "1.0.0-RC18"

ThisBuild / libraryDependencies := Seq(
  "org.zeromq" % "jeromq"        % "0.5.2",
  "dev.zio"    %% "zio"          % zioVersion,
  "dev.zio"    %% "zio-test"     % zioVersion % "test",
  "dev.zio"    %% "zio-test-sbt" % zioVersion % "test"
)

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

ThisBuild / scalacOptions := Seq(
  "-language:postfixOps"
)

ThisBuild / organization := "com.tusharmath"
ThisBuild / organizationName := "zeromq"
ThisBuild / organizationHomepage := Some(url("https://tusharmath.com"))

ThisBuild / useGpg := true

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
