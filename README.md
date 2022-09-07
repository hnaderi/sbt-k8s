<p align="center">
  <img src="https://raw.githubusercontent.com/kubernetes/kubernetes/master/logo/logo.png" height="100px" alt="kubernetes icon" />
  <br/>
  <strong>sbt k8s</strong>
  <i>sbt plugin for scala k8s</i>
</p>

<a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" /></a>

![Kubernetes version](https://img.shields.io/badge/Kubernetes-v1.25.0-blue?style=flat-square&logo=kubernetes&logoColor=white)
[![sbt-k8s Scala version support](https://index.scala-lang.org/hnaderi/sbt-k8s/sbt-k8s/latest-by-scala-version.svg?style=flat-square)](https://index.scala-lang.org/hnaderi/sbt-k8s/sbt-k8s)
[![javadoc](https://javadoc.io/badge2/dev.hnaderi/sbt-k8s-docs_3/scaladoc.svg?style=flat-square)](https://javadoc.io/doc/dev.hnaderi/sbt-k8s-docs_3)  
<img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/hnaderi/sbt-k8s/Continuous%20Integration?style=flat-square">
<img alt="GitHub" src="https://img.shields.io/github/license/hnaderi/sbt-k8s?style=flat-square">
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

### Usage

This sbt plugin is available for sbt 1.x

To use the latest version of plugins, include the following in your `project/plugins.sbt`:

```scala
addSbtPlugin("dev.hnaderi" % "sbt-k8s-manifest" % "@VERSION@") // just manifest generation and objects
```

and your project in `build.sbt`

```scala
lazy val service = project
  .settings(
    k8sManifestObjects := Seq(
      // kubernetes objects from scala-k8s
    )
  )
  .enablePlugin(K8sManifestPlugin)
```

now you have `k8sManifestGen` and `k8sManifestPrint` tasks that will generate or print manifest for defined objects.
