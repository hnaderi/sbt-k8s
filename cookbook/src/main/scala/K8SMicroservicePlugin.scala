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
package cookbook

import dev.hnaderi.k8s
import dev.hnaderi.k8s.cookbook._
import sbt.AutoPlugin
import sbt.Keys._
import sbt._

object K8SMicroservicePlugin extends AutoPlugin {
  object autoImport {
    val microserviceName: SettingKey[String] = settingKey(
      "name used as deployment, config, secret, and all other resource names. defaults to [project / name]"
    )
    val microserviceNamespace: SettingKey[String] = settingKey(
      "namespace used to deploy resources, defaults to \"default\""
    )
    val microserviceImage: SettingKey[String] = settingKey(
      "container image, will be populated automatically if you have also DockerPlugin from native packager"
    )

    val microserviceConfigs: SettingKey[Map[String, k8s.Data]] = settingKey(
      "config data, if any data is provided, will be used in a single `ConfigMap` named like other resources, defaults to Map.empty"
    )
    val microserviceSecrets: SettingKey[Map[String, k8s.Data]] = settingKey(
      "secret data, if any data is provided, will be used in a single `Secret` named like other resources, defaults to Map.empty"
    )
    val microserviceVariables: SettingKey[Map[String, String]] = settingKey(
      "environment variables"
    )

    val microserviceEnvironments: SettingKey[Seq[EnvironmentDefinition]] =
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
    microserviceNamespace := "default",
    microserviceEnvironments := Nil,
    microserviceServices := Nil,
    microserviceDefinition := MicroserviceDefinition(
      name = microserviceName.value,
      version = version.value,
      namespace = microserviceNamespace.value,
      image = microserviceImage.value,
      environments = microserviceEnvironments.value,
      services = microserviceServices.value
    ),
    k8sManifestObjects := microserviceDefinition.value.build
  )
}
