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

import _root_.io.k8s.api.core.v1.Probe
import _root_.io.k8s.api.core.v1.ResourceRequirements
import sbt.AutoPlugin
import sbt.Keys._
import sbt._

object K8SMicroservicePlugin extends AutoPlugin {
  object autoImport {
    val microserviceName: SettingKey[String] = settingKey(
      "name used as deployment, config, secret, and all other resource names. defaults to [project / name]"
    )
    val microserviceNamespace: SettingKey[Option[String]] = settingKey(
      "namespace used to deploy resources, defaults to \"default\""
    )
    val microserviceImage: SettingKey[String] = settingKey(
      "container image, will be populated automatically if you have also DockerPlugin from native packager"
    )

    val microserviceImagePullSecrets: SettingKey[Seq[String]] = settingKey(
      "image pull secrets"
    )
    val microserviceImagePullPolicy: SettingKey[String] = settingKey(
      "container image pull policy"
    )
    val microserviceStartupProbe: SettingKey[Probe] = settingKey(
      "container startup probe"
    )
    val microserviceReadinessProbe: SettingKey[Probe] = settingKey(
      "container readiness probe"
    )
    val microserviceLivenessProbe: SettingKey[Probe] = settingKey(
      "container liveness probe"
    )
    val microserviceResources: SettingKey[ResourceRequirements] = settingKey(
      "container resource requirements"
    )
    val microserviceArgs: SettingKey[Seq[String]] = settingKey("container args")
    val microserviceWorkingDir: SettingKey[String] = settingKey(
      "container working dir"
    )

    val microserviceEnvironments: SettingKey[Seq[Environment]] =
      settingKey(
        "service environment definitions"
      )
    val microserviceServices: SettingKey[Seq[ServiceDefinition]] = settingKey(
      "service definitions"
    )

    val microserviceDefinition: SettingKey[MicroserviceDefinition] = settingKey(
      "final deployment model"
    )
  }

  import autoImport._
  import K8sManifestPlugin.autoImport.k8sManifestObjects

  override def trigger = noTrigger
  override def requires = K8sManifestPlugin

  override val projectSettings = Seq(
    microserviceName := name.value,
    microserviceNamespace := None,
    microserviceEnvironments := Nil,
    microserviceServices := Nil,
    microserviceDefinition := MicroserviceDefinition(
      name = microserviceName.value,
      version = version.value,
      namespace = microserviceNamespace.value,
      image = microserviceImage.value,
      imagePullSecrets = (microserviceImagePullSecrets ?? Nil).value,
      imagePullPolicy = (microserviceImagePullPolicy ?).value,
      startupProbe = (microserviceStartupProbe ?).value,
      readinessProbe = (microserviceReadinessProbe ?).value,
      livenessProbe = (microserviceLivenessProbe ?).value,
      resources = (microserviceResources ?).value,
      args = (microserviceArgs ?).value,
      workingDir = (microserviceWorkingDir ?).value,
      environments = microserviceEnvironments.value,
      services = microserviceServices.value
    ),
    k8sManifestObjects := microserviceDefinition.value.build
  )
}
