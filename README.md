<p align="center">
  <img src="https://raw.githubusercontent.com/kubernetes/kubernetes/master/logo/logo.png" height="100px" alt="kubernetes icon" />
  <br/>
  <strong>sbt k8s</strong>
  <i>sbt plugin for scala k8s</i>
</p>

<a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" /></a>

![Kubernetes version](https://img.shields.io/badge/Kubernetes-v1.26.3-blue?style=flat-square&logo=kubernetes&logoColor=white)
[![sbt-k8s Scala version support](https://index.scala-lang.org/hnaderi/sbt-k8s/sbt-k8s/latest-by-scala-version.svg?style=flat-square)](https://index.scala-lang.org/hnaderi/sbt-k8s/sbt-k8s) 
<img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/hnaderi/sbt-k8s/ci.yml?style=flat-square">
<img alt="GitHub" src="https://img.shields.io/github/license/hnaderi/sbt-k8s?style=flat-square">
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

### Usage

This sbt plugin is available for sbt 1.x

To use the latest version of plugins, include the following in your `project/plugins.sbt`:

```scala
addSbtPlugin("dev.hnaderi" % "sbt-k8s" % "@VERSION@") // everything
addSbtPlugin("dev.hnaderi" % "sbt-k8s-manifests" % "@VERSION@") // just manifest generation and objects
addSbtPlugin("dev.hnaderi" % "sbt-k8s-cookbook" % "@VERSION@") // easy to use recipes (RECOMMENDED)
```

and your project in `build.sbt`

#### If you have native packager docker plugin. (all settings are optional)
```scala
lazy val service = project
  .settings(
    microserviceResources := ResourceRequirements(
      limits = Map("cpu" -> Quantity("500m"), "memory" -> Quantity("512Mi")),
      requests = Map("cpu" -> Quantity("250m"), "memory" -> Quantity("128Mi"))
    ),
    microserviceEnvironments := Seq(
      Variable("SERVICE_NAME", "example"),
      ExternalSecretVariable("POSTGRES_PASSWORD", "password", "pg-credentials"),
      ConfigFile("service-config", Map(
          "app.conf" -> file(s"deployments/configs/service.conf"),
          "base.conf" -> file(s"deployments/configs/base.conf")
        ),
        "/mnt/config"
      )
    ),
    microserviceServices := Seq(ServiceDefinition("ws", 8080, 80))
  )
  .enablePlugins(DockerPlugin, K8sMicroservicePlugin)
```

#### If you don't want to use native packager (microserviceImage is required)
```scala
lazy val service = project
  .settings(
    microserviceImage := "your.registry/your-image:version"
  )
  .enablePlugins(K8sMicroservicePlugin)
```

#### If you want to create completely custom manifests (you are on your own, create any kubernetes object you want)

```scala
lazy val service = project
  .settings(
    k8sManifestObjects := Seq(
      // kubernetes objects from scala-k8s
    )
  )
  .enablePlugin(K8sManifestPlugin)
```

### How to run
now you have `k8sManifestGen` and `k8sManifestPrint` tasks that will generate or print manifest for defined objects.  
default generation will write manifest in `target/k8s/manifest.yml`

### Configure
You can configure everything using settings. current settings are prefixed by `k8sManifest` or `microservice`
