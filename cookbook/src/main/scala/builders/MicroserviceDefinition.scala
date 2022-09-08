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
import io.k8s.api.core.v1.ServiceSpec
import io.k8s.api.networking.v1.Ingress
import io.k8s.api.networking.v1.IngressSpec
import io.k8s.apimachinery.pkg.apis.meta.v1.LabelSelector
import io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta

final case class MicroserviceDefinition(
    name: String,
    version: String,
    image: String,
    namespace: Option[String] = None,
    imagePullSecrets: Seq[String] = Nil,
    imagePullPolicy: Option[String] = None,
    startupProbe: Option[io.k8s.api.core.v1.Probe] = None,
    readinessProbe: Option[io.k8s.api.core.v1.Probe] = None,
    livenessProbe: Option[io.k8s.api.core.v1.Probe] = None,
    resources: Option[io.k8s.api.core.v1.ResourceRequirements] = None,
    args: Option[Seq[String]] = None,
    workingDir: Option[String] = None,
    environments: Seq[Environment] = Nil,
    services: Seq[ServiceBuilder] = Nil
) extends Recipe {
  private implicit class SeqOps[A](s: Seq[A]) {
    def toNonEmpty: Option[Seq[A]] = if (s.isEmpty) None else Some(s)
  }

  private val commonLabels =
    Seq(Labels.version(version), Labels.managedBy("dev.hnaderi.sbtk8s"))

  private val metadata =
    ObjectMeta(
      name = name,
      namespace = namespace,
      labels = Map(
        Labels.name(name)
      ) ++ commonLabels
    )

  private def service: Option[Service] =
    services
      .map(_.servicePort)
      .toNonEmpty
      .map(ports =>
        Service(
          metadata = metadata,
          spec = ServiceSpec(
            selector = Map(Labels.name(name)),
            ports = ports
          )
        )
      )

  private def ingress: Option[Ingress] =
    services
      .flatMap(_.ingressRule)
      .toNonEmpty
      .map(rules =>
        Ingress(
          metadata = metadata,
          spec = IngressSpec(rules = rules)
        )
      )

  private def configObject: List[ConfigMap] = environments
    .flatMap(_.configMap)
    .map(_.mapMetadata(_.addLabels(commonLabels: _*)))
    .toList
  private def secretObject: List[Secret] = environments
    .flatMap(_.secret)
    .map(_.mapMetadata(_.addLabels(commonLabels: _*)))
    .toList

  private def deployment = List(
    Deployment(
      metadata = metadata,
      spec = DeploymentSpec(
        selector = LabelSelector(matchLabels = Map(Labels.name(name))),
        template = PodTemplateSpec(
          metadata = metadata,
          spec = PodSpec(
            containers = Seq(
              Container(
                name = name,
                image = image,
                ports = services.map(_.containerPort).toNonEmpty,
                env = environments.flatMap(_.envVar).toNonEmpty,
                volumeMounts = environments.flatMap(_.volumeMount).toNonEmpty,
                startupProbe = startupProbe,
                readinessProbe = readinessProbe,
                livenessProbe = livenessProbe,
                resources = resources,
                args = args,
                workingDir = workingDir
              )
            ),
            imagePullSecrets =
              imagePullSecrets.map(s => LocalObjectReference(s)).toNonEmpty,
            volumes = environments.flatMap(_.volume).toNonEmpty
          )
        )
      )
    )
  )

  def build: Seq[KObject] = List(
    service.toList,
    ingress.toList,
    configObject,
    secretObject,
    deployment
  ).flatten

}
