/*
 * Copyright 2022 Hossein Naderi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.hnaderi.sbtk8s

import dev.hnaderi.k8s.KObject
import dev.hnaderi.k8s.manifest._
import sbt.Keys._
import sbt._

import java.io.PrintWriter

object K8sManifestPlugin extends AutoPlugin {
  object autoImport {
    val k8sManifestObjects: SettingKey[Seq[KObject]] = SettingKey(
      "k8s objects to create"
    )
    val k8sManifestFileName: SettingKey[String] = SettingKey(
      "manifest file name"
    )

    val k8sManifestPrint = taskKey[Unit]("prints kubernetes manifests")
    val k8sManifestGen = taskKey[Unit]("generate kubernetes manifest")
  }

  import autoImport._

  override def trigger = noTrigger
  override def requires = sbt.plugins.JvmPlugin

  override val projectSettings = Seq(
    k8sManifestObjects := Nil,
    k8sManifestGen / target := (ThisProject / target).value / "k8s",
    k8sManifestFileName := "manifest.yml",
    k8sManifestPrint := {
      println(s"printing manifest for ${name.value}")
      printManifest(k8sManifestObjects.value)
    },
    k8sManifestGen := {
      generateManifest(
        k8sManifestObjects.value,
        (k8sManifestGen / target).value,
        k8sManifestFileName.value
      )
    }
  )

  private def generateManifest(
      objs: Seq[KObject],
      target: File,
      fileName: String
  ) = writeOutput(target, fileName)(objs.asManifest)

  private def printManifest(objs: Seq[KObject]) = println(objs.asManifest)

  private def writeOutput(buildTarget: File, outName: String)(
      content: String
  ) = {
    buildTarget.mkdirs()
    val file = new File(buildTarget, outName)
    val printWriter = new PrintWriter(file)

    try {
      printWriter.println(content)
    } finally {
      printWriter.close()
    }
  }
}
