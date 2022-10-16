import org.typelevel.sbt.gha.WorkflowStep.Sbt
import org.typelevel.sbt.TypelevelCiReleasePlugin

ThisBuild / tlBaseVersion := "0.1"

ThisBuild / organization := "dev.hnaderi"
ThisBuild / organizationName := "Hossein Naderi"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("hnaderi", "Hossein Naderi")
)

val scala212 = "2.13.10"

ThisBuild / tlSonatypeUseLegacyHost := false
ThisBuild / tlSitePublishBranch := Some("main")
ThisBuild / scalaVersion := scala212
ThisBuild / githubWorkflowBuildSbtStepPreamble := Nil
ThisBuild / githubWorkflowBuild ~= {
  _.map {
    case Sbt(commands, id, Some("Test"), cond, env, params) =>
      Sbt(List("+test"), name = Some("Test"))
    case other => other
  }
}

ThisBuild / resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS" at "https://s01.oss.sonatype.org/content/repositories/releases"
)

lazy val root =
  project
    .in(file("."))
    .aggregate(core, manifest, cookbook, docs)
    .enablePlugins(AutomateHeaderPlugin, NoPublishPlugin)

val scalaK8sVersion = "0.5.1"
val munitVersion = "0.7.29"

lazy val manifest = project
  .enablePlugins(AutomateHeaderPlugin, SbtPlugin)
  .settings(
    name := "sbt-k8s-manifests",
    pluginCrossBuild / sbtVersion := "1.2.8", // set minimum sbt version
    libraryDependencies += "dev.hnaderi" %% "scala-k8s-manifests" % scalaK8sVersion
  )

lazy val cookbook = project
  .enablePlugins(AutomateHeaderPlugin, SbtPlugin)
  .settings(
    name := "sbt-k8s-cookbook",
    pluginCrossBuild / sbtVersion := "1.2.8", // set minimum sbt version
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.scalameta" %% "munit-scalacheck" % munitVersion % Test,
      "dev.hnaderi" %% "scala-k8s-scalacheck" % scalaK8sVersion % Test
    )
  )
  .dependsOn(manifest)

lazy val core = project
  .enablePlugins(AutomateHeaderPlugin, SbtPlugin)
  .settings(
    name := "sbt-k8s",
    pluginCrossBuild / sbtVersion := "1.2.8", // set minimum sbt version
    mimaReportBinaryIssues := {}
  )
  .dependsOn(manifest, cookbook)

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .settings(
    tlSiteRelatedProjects := Seq(
      "scala k8s" -> url("https://github.com/hnaderi/scala-k8s"),
      "sbt" -> url("https://github.com/sbt/sbt")
    )
  )
