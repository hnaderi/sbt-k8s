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
import sbt.AutoPlugin
import sbt.Keys._
import sbt._

object K8SMicroservicePlugin extends AutoPlugin {
  object autoImport {
    val namespace: SettingKey[String] = settingKey("namespace")
    val image: SettingKey[String] = settingKey("container image")

    val configs: SettingKey[Map[String, k8s.Data]] = settingKey("config names")
    val secrets: SettingKey[Map[String, k8s.Data]] = settingKey("secret names")
    val variables: SettingKey[Map[String, String]] = settingKey(
      "environment variables"
    )

    val port: SettingKey[Option[Int]] = settingKey("service port")
    val host: SettingKey[Option[String]] = settingKey("host name to expose on")
    val path: SettingKey[Option[String]] = settingKey("http path to expose on")

    val deployment: SettingKey[MicroserviceDefinition] = SettingKey(
      "final deployment model"
    )
  }

  import autoImport._
  import K8SPlugin.autoImport.manifestObjects

  override def trigger = noTrigger
  override def requires = K8SPlugin

  override val projectSettings = Seq(
    name := name.value,
    namespace := s"k8s-${(name).value}",
    configs := Map.empty,
    secrets := Map.empty,
    variables := Map.empty,
    port := None,
    host := None,
    path := None,
    deployment := MicroserviceDefinition(
      name = (name).value,
      namespace = (namespace).value,
      image = (image).value,
      configs = (configs).value,
      secrets = (secrets).value,
      variables = (variables).value.toMap,
      port = (port).value,
      host = (host).value,
      path = (path).value
    ),
    manifestObjects := (deployment).value.build
  )
}
