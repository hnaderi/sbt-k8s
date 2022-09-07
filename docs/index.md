## sbt-k8s

### Usage

This sbt plugin is available for sbt 1.x

To use the latest version of plugins, include the following in your `project/plugins.sbt`:

```scala
addSbtPlugin("dev.hnaderi" % "sbt-k8s-manifests" % "@VERSION@") // just manifest generation and objects
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
