## sbt-k8s

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
  .enablePlugins(DockerPlugin, K8SMicroservicePlugin)
```

#### If you don't want to use native packager (microserviceImage is required)
```scala
lazy val service = project
  .settings(
    microserviceImage := "your.registry/your-image:version"
  )
  .enablePlugins(K8SMicroservicePlugin)
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
