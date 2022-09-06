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

import dev.hnaderi.k8s._
import dev.hnaderi.k8s.implicits._
import io.k8s.api.apps.v1.Deployment
import io.k8s.api.apps.v1.DeploymentSpec
import io.k8s.api.core.v1.ConfigMap
import io.k8s.api.core.v1.Container
import io.k8s.api.core.v1.LocalObjectReference
import io.k8s.api.core.v1.PodSpec
import io.k8s.api.core.v1.PodTemplateSpec
import io.k8s.api.core.v1.Secret
import io.k8s.api.core.v1.Service
import io.k8s.api.core.v1.ServicePort
import io.k8s.api.core.v1.ServiceSpec
import io.k8s.api.networking.v1.Ingress
import io.k8s.api.networking.v1.IngressSpec
import io.k8s.apimachinery.pkg.apis.meta.v1.LabelSelector
import io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta

final case class MicroserviceDefinition(
    name: String,
    namespace: String,
    image: String,
    imagePullSecret: Seq[String] = Nil,
    configs: Map[String, Data] = Map.empty,
    secrets: Map[String, Data] = Map.empty,
    variables: Map[String, String] = Map.empty,
    port: Option[Int] = None,
    host: Option[String] = None,
    path: Option[String] = None
) {
  private val metadata =
    ObjectMeta(
      name = name,
      namespace = namespace,
      labels = Map(Labels.name(name))
    )

  private def service = Some(
    Service(
      metadata = metadata,
      spec = ServiceSpec(
        selector = Map(Labels.name(name)),
        ports = Seq(
          ServicePort(
            port = 1,
            name = "",
            targetPort = 8080
          )
        )
      )
    )
  ).toList

  private def ingress = Some(
    Ingress(metadata = metadata, spec = IngressSpec())
  ).toList

  private def configObject = Some(ConfigMap()).toList
  private def secretObject = Some(Secret()).toList
  private def deployment = List(
    Deployment(
      metadata = metadata,
      spec = DeploymentSpec(
        selector = LabelSelector(matchLabels = Map(Labels.name(name))),
        template = PodTemplateSpec(
          metadata = metadata,
          spec = PodSpec(
            containers = Seq(Container(name = name, image = image)),
            imagePullSecrets =
              imagePullSecret.map(s => LocalObjectReference(s)),
            volumes = Seq()
          )
        )
      )
    )
  )

  def build: Seq[KObject] =
    service ::: ingress ::: configObject ::: secretObject ::: deployment

}
