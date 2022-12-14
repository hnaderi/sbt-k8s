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

import dev.hnaderi.k8s.Data
import dev.hnaderi.k8s.DataMap
import dev.hnaderi.k8s.implicits._
import io.k8s.api.core.v1.ConfigMap
import io.k8s.api.core.v1.ConfigMapKeySelector
import io.k8s.api.core.v1.ConfigMapVolumeSource
import io.k8s.api.core.v1.EnvVar
import io.k8s.api.core.v1.EnvVarSource
import io.k8s.api.core.v1.Secret
import io.k8s.api.core.v1.SecretKeySelector
import io.k8s.api.core.v1.SecretVolumeSource
import io.k8s.api.core.v1.Volume
import io.k8s.api.core.v1.VolumeMount
import io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta

abstract class Environment extends Serializable with Product {
  def configMap: Option[ConfigMap] = None
  def secret: Option[Secret] = None
  def envVar: Option[EnvVar] = None
  def volumeMount: Option[VolumeMount] = None
  def volume: Option[Volume] = None
}
object Environment {
  final case class Variable(name: String, value: String) extends Environment {
    override def envVar: Option[EnvVar] = Some(
      EnvVar(name = name, value = value)
    )
  }

  final case class ConfigVariable(
      name: String,
      data: Data,
      key: String = "config",
      configMapName: Option[String] = None,
      optional: Option[Boolean] = None
  ) extends Environment {
    val resourceName = configMapName.getOrElse(name)

    override def configMap: Option[ConfigMap] = Some(
      ConfigMap(
        data = DataMap(key -> data),
        metadata = ObjectMeta(name = resourceName)
      )
    )

    override def envVar: Option[EnvVar] = Some(
      EnvVar(
        name = name,
        valueFrom = EnvVarSource(
          configMapKeyRef = ConfigMapKeySelector(
            key = key,
            name = resourceName,
            optional = optional
          )
        )
      )
    )
  }

  final case class SecretVariable(
      name: String,
      data: Data,
      key: String = "config",
      secretName: Option[String] = None,
      optional: Option[Boolean] = None
  ) extends Environment {
    val resourceName = secretName.getOrElse(name)

    override def secret: Option[Secret] = Some(
      Secret(
        data = DataMap.binary(key -> data),
        metadata = ObjectMeta(name = resourceName)
      )
    )

    override def envVar: Option[EnvVar] = Some(
      EnvVar(
        name = name,
        valueFrom = EnvVarSource(
          secretKeyRef = SecretKeySelector(
            key = key,
            name = resourceName,
            optional = optional
          )
        )
      )
    )
  }

  final case class ConfigFile(
      name: String,
      data: Map[String, Data],
      mountPath: String
  ) extends Environment {
    override def configMap: Option[ConfigMap] = Some(
      ConfigMap(
        metadata = ObjectMeta(name = name),
        binaryData = DataMap.binary(data.toSeq: _*)
      )
    )

    override def volumeMount: Option[VolumeMount] = Some(
      VolumeMount(
        name = name,
        mountPath = mountPath
      )
    )

    override def volume: Option[Volume] = Some(
      Volume(name = name, configMap = ConfigMapVolumeSource(name = name))
    )
  }

  final case class SecretFile(
      name: String,
      data: Map[String, Data],
      mountPath: String
  ) extends Environment {
    override def secret: Option[Secret] = Some(
      Secret(
        metadata = ObjectMeta(name = name),
        data = DataMap.binary(data.toSeq: _*)
      )
    )

    override def volumeMount: Option[VolumeMount] = Some(
      VolumeMount(
        name = name,
        mountPath = mountPath
      )
    )

    override def volume: Option[Volume] = Some(
      Volume(name = name, secret = SecretVolumeSource(secretName = name))
    )
  }

  final case class ExternalConfigFile(name: String, mountPath: String)
      extends Environment {
    override def volumeMount: Option[VolumeMount] = Some(
      VolumeMount(
        name = name,
        mountPath = mountPath
      )
    )

    override def volume: Option[Volume] = Some(
      Volume(name = name, configMap = ConfigMapVolumeSource(name = name))
    )
  }

  final case class ExternalSecretFile(name: String, mountPath: String)
      extends Environment {
    override def volumeMount: Option[VolumeMount] = Some(
      VolumeMount(
        name = name,
        mountPath = mountPath
      )
    )

    override def volume: Option[Volume] = Some(
      Volume(name = name, secret = SecretVolumeSource(secretName = name))
    )
  }

  final case class ExternalConfigVariable(
      name: String,
      key: String,
      target: String,
      optional: Option[Boolean] = None
  ) extends Environment {
    override def envVar: Option[EnvVar] = Some(
      EnvVar(
        name = name,
        valueFrom = EnvVarSource(configMapKeyRef =
          ConfigMapKeySelector(key = key, name = target, optional = optional)
        )
      )
    )
  }

  final case class ExternalSecretVariable(
      name: String,
      key: String,
      target: String,
      optional: Option[Boolean] = None
  ) extends Environment {
    override def envVar: Option[EnvVar] = Some(
      EnvVar(
        name = name,
        valueFrom = EnvVarSource(secretKeyRef =
          SecretKeySelector(key = key, name = target, optional = optional)
        )
      )
    )
  }

}
