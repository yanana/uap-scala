name := "uap-scala"

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Xlint"
)

val snakeyamlVersion = "1.25"
val scalatestVersion = "3.1.0"
val scalatestplusScalacheckVersion = "3.1.0.0-RC2"

lazy val coreDeps = Seq(
  "org.yaml" % "snakeyaml" % snakeyamlVersion,
)

lazy val testDeps = Seq(
  "org.scalatest" %% "scalatest" % scalatestVersion,
  "org.scalatestplus" %% "scalatestplus-scalacheck" % scalatestplusScalacheckVersion
) map (_ % "test")

lazy val buildSettings = Seq(
  organization := "com.github.yanana",
  scalaVersion := "2.12.10",
  crossScalaVersions := Seq("2.11.12", "2.12.10")
)

lazy val baseSettings = Seq(
  scalacOptions ++= compilerOptions,
  scalacOptions in (Compile, console) := compilerOptions,
  scalacOptions in (Compile, test) := compilerOptions,
  libraryDependencies ++= coreDeps ++ testDeps,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

libraryDependencies ++= coreDeps ++ testDeps

unmanagedResourceDirectories in Compile += baseDirectory.value / "core"
includeFilter in (Compile, unmanagedResources) := "regexes.yaml"
unmanagedResourceDirectories in Test += baseDirectory.value / "core"
includeFilter in (Test, unmanagedResources) := "*.yaml"

scalacOptions ++= compilerOptions

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/yanana/uap-scala")),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/yanana/uap-scala"),
      "scm:git:git@github.com:yanana/uap-scala.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>yanana</id>
        <name>Shun Yanaura</name>
        <url>https://github.com/yanana</url>
      </developer>
    </developers>
)

lazy val noPublishSettings = Seq(
  publish := (()),
  publishLocal := (()),
  publishArtifact := false
)

lazy val allSettings = buildSettings ++ baseSettings ++ publishSettings

lazy val root = (project in file("."))
  .settings(
    name := "uap-scala",
    description := "ua-parser library for Scala",
    moduleName := "uap-scala"
  )
  .settings(allSettings: _*)
