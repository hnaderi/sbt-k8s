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
import dev.hnaderi.k8s.scalacheck.Generators._
import io.k8s.api.apps.v1.Deployment
import io.k8s.api.core.v1.Service
import io.k8s.api.networking.v1.Ingress
import io.k8s.apimachinery.pkg.apis.meta.v1.LabelSelector
import io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta
import munit.Location
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

import MicroserviceDefinitionSuite._
import implicits._

class MicroserviceDefinitionSuite extends ScalaCheckSuite {
  type Manifest = Seq[KObject]

  val baseDef = MicroserviceDefinition(
    name = "example",
    version = "0.0.1",
    image = "example:latest"
  )

  property("Simple Deployment") {
    forAll(microservices) { defs =>
      val manifest = defs.build

      assertEquals(manifest.size, 1)
      val deployment = getDeployment(manifest)
      assertCommonMeta(deployment.metadata, defs)
      val spec = assertExists(deployment.spec)
      import spec.template
      assertCommonMeta(template.metadata, defs)
      assertEquals(
        spec.selector,
        LabelSelector(matchLabels = Map(Labels.name(defs.name)))
      )
      val podSpec = assertExists(template.spec)
      assertEquals(podSpec.containers.size, 1)
      val container = assertExists(podSpec.containers.headOption)
      assertEquals(container.image, Some(defs.image))
      assertEquals(container.name, defs.name)
      assertEquals(container.ports, None)
      assertEquals(container.env, None)
      assertEquals(container.volumeMounts, None)
    }
  }

  test("Must not create service when no service is defined") {
    val defs = baseDef.copy(services = Nil)
    val manifest = defs.build

    assertNoService(manifest)
    assertNoIngress(manifest)
  }

  test("Must not create ingress when no public service is defined") {
    val defs = baseDef.copy(services = Seq(ServiceDefinition("ws", 8080)))
    val manifest = defs.build

    assertNoIngress(manifest)
    val srv = getService(manifest)
    assertCommonMeta(srv.metadata, defs)
    assertExists(srv.spec)
  }

  private def assertExists[T](opt: Option[T])(implicit loc: Location) = {
    assert(opt.isDefined, "Expected to exist!")
    opt.get
  }

  private def assertCommonMeta(
      meta: Option[ObjectMeta],
      defs: MicroserviceDefinition
  )(implicit loc: Location) = {
    val m = assertExists(meta)
    val labels = assertExists(m.labels).toSeq
    assert(labels.contains(Labels.name(defs.name)))
    assert(labels.contains(Labels.version(defs.version)))
  }

  private def getDeployment(
      manifest: Manifest
  )(implicit loc: Location): Deployment = {
    val dep = manifest.collect { case d: Deployment => d }
    assert(dep.isDefined, "There is no deployment!")
    dep.head
  }
  private def getService(
      manifest: Manifest
  )(implicit loc: Location): Service = {
    val dep = manifest.collect { case d: Service => d }
    assert(dep.isDefined, "There is no service!")
    dep.head
  }
  private def getIngress(
      manifest: Manifest
  )(implicit loc: Location): Ingress = {
    val dep = manifest.collect { case d: Ingress => d }
    assert(dep.isDefined, "There is no ingress!")
    dep.head
  }
  private def assertNoService(
      manifest: Manifest
  )(implicit loc: Location): Unit = {
    val h = manifest.collectFirst { case d: Service => d }
    assertEquals(h, None, "There is a service!")

  }
  private def assertNoIngress(
      manifest: Manifest
  )(implicit loc: Location): Unit = {
    val h = manifest.collectFirst { case d: Ingress => d }
    assertEquals(h, None, "There is an ingress!")

  }
}

object MicroserviceDefinitionSuite {
  private implicit val arbData: Arbitrary[Data] = Arbitrary(
    Gen.alphaStr.map(Data(_))
  )

  private implicit val dummyEnvironment: Arbitrary[Seq[Environment]] =
    Arbitrary(Gen.const(Nil))
  private implicit val dummyServices: Arbitrary[Seq[ServiceBuilder]] =
    Arbitrary(Gen.const(Nil))

  private val microservices = Gen.resultOf(MicroserviceDefinition)

}
