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

import _root_.io.k8s.api.core.v1.ConfigMapVolumeSource
import dev.hnaderi.k8s.Data
import dev.hnaderi.k8s.DataMap
import dev.hnaderi.k8s.implicits._
import io.k8s.api.core.v1.ConfigMapKeySelector
import io.k8s.api.core.v1.EnvVar
import io.k8s.api.core.v1.EnvVarSource
import io.k8s.api.core.v1.SecretKeySelector
import io.k8s.api.core.v1.SecretVolumeSource
import munit.Location
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

import EnvironmentsSuite._

class EnvironmentsSuite extends ScalaCheckSuite {
  property("Variable") {
    forAll(variables) { env =>
      expectNo(env.secret)
      expectNo(env.configMap)
      expectNo(env.volume)
      expectNo(env.volumeMount)
      expectEq(env.envVar, EnvVar(name = env.name, value = env.value))
    }
  }

  property("Secret Variable") {
    forAll(secretVars) { env =>
      val secretName = env.secretName.getOrElse(env.name)

      expect(env.secret) { secret =>
        assertEquals(
          secret.data,
          Some(Map(env.key -> env.data.getBase64Content))
        )
        expectNo(secret.stringData)
        assertEquals(secret.metadata.flatMap(_.name), Some(secretName))
      }
      expectEq(
        env.envVar,
        EnvVar(
          name = env.name,
          valueFrom = EnvVarSource(secretKeyRef =
            SecretKeySelector(
              key = env.key,
              name = secretName,
              optional = env.optional
            )
          )
        )
      )
      expectNo(env.configMap)
      expectNo(env.volume)
      expectNo(env.volumeMount)
    }
  }

  property("Config Variable") {
    forAll(configVars) { env =>
      val configMapName = env.configMapName.getOrElse(env.name)

      expect(env.configMap) { config =>
        assertEquals(
          config.data,
          Some(Map(env.key -> env.data.getContent))
        )
        expectNo(config.binaryData)
        assertEquals(config.metadata.flatMap(_.name), Some(configMapName))
      }
      expectEq(
        env.envVar,
        EnvVar(
          name = env.name,
          valueFrom = EnvVarSource(configMapKeyRef =
            ConfigMapKeySelector(
              key = env.key,
              name = configMapName,
              optional = env.optional
            )
          )
        )
      )

      expectNo(env.secret)
      expectNo(env.volume)
      expectNo(env.volumeMount)
    }
  }

  property("External Secret Variable") {
    forAll(externalSecretVars) { env =>
      expectEq(
        env.envVar,
        EnvVar(
          name = env.name,
          valueFrom = EnvVarSource(secretKeyRef =
            SecretKeySelector(
              key = env.key,
              name = env.name,
              optional = env.optional
            )
          )
        )
      )

      expectNo(env.secret)
      expectNo(env.configMap)
      expectNo(env.volume)
      expectNo(env.volumeMount)
    }
  }

  property("External Config Variable") {
    forAll(externalConfigVars) { env =>
      expectEq(
        env.envVar,
        EnvVar(
          name = env.name,
          valueFrom = EnvVarSource(configMapKeyRef =
            ConfigMapKeySelector(
              key = env.key,
              name = env.name,
              optional = env.optional
            )
          )
        )
      )

      expectNo(env.secret)
      expectNo(env.configMap)
      expectNo(env.volume)
      expectNo(env.volumeMount)
    }
  }

  property("Config file") {
    forAll(configFiles) { env =>
      expect(env.configMap) { config =>
        assertEquals(
          config.binaryData,
          Some(DataMap.binaryFrom(env.data))
        )
        expectNo(config.data)
        expectEq(config.metadata.flatMap(_.name), env.name)
      }
      expect(env.volume) { vol =>
        assertEquals(vol.name, env.name)
        expectEq(vol.configMap, ConfigMapVolumeSource(name = env.name))
      }
      expect(env.volumeMount) { mount =>
        assertEquals(mount.name, env.name)
        assertEquals(mount.mountPath, env.mountPath)
      }

      expectNo(env.secret)
      expectNo(env.envVar)
    }
  }

  property("Secret file") {
    forAll(secretFiles) { env =>
      expect(env.secret) { config =>
        assertEquals(
          config.data,
          Some(DataMap.binaryFrom(env.data))
        )
        expectNo(config.stringData)
        expectEq(config.metadata.flatMap(_.name), env.name)
      }
      expect(env.volume) { vol =>
        assertEquals(vol.name, env.name)
        expectEq(vol.secret, SecretVolumeSource(secretName = env.name))
      }
      expect(env.volumeMount) { mount =>
        assertEquals(mount.name, env.name)
        assertEquals(mount.mountPath, env.mountPath)
      }

      expectNo(env.configMap)
      expectNo(env.envVar)
    }
  }

  property("External Secret file") {
    forAll(externalSecretFiles) { env =>
      expect(env.volume) { vol =>
        assertEquals(vol.name, env.name)
        expectEq(vol.secret, SecretVolumeSource(secretName = env.name))
      }
      expect(env.volumeMount) { mount =>
        assertEquals(mount.name, env.name)
        assertEquals(mount.mountPath, env.mountPath)
      }

      expectNo(env.secret)
      expectNo(env.configMap)
      expectNo(env.envVar)
    }
  }

  property("External Config file") {
    forAll(externalConfigFiles) { env =>
      expect(env.volume) { vol =>
        assertEquals(vol.name, env.name)
        expectEq(vol.configMap, ConfigMapVolumeSource(name = env.name))
      }
      expect(env.volumeMount) { mount =>
        assertEquals(mount.name, env.name)
        assertEquals(mount.mountPath, env.mountPath)
      }

      expectNo(env.secret)
      expectNo(env.configMap)
      expectNo(env.envVar)
    }
  }

  private def expect[T](opt: Option[T], msg: String = "Expected to exist!")(
      f: T => Unit
  )(implicit loc: Location) = opt match {
    case Some(t) => f(t)
    case None    => fail(msg)
  }
  private def expectNo[T](opt: Option[T])(implicit loc: Location) =
    assertEquals(opt, None)
  private def expectEq[T](opt: Option[T], t: T)(implicit loc: Location) =
    expect(opt)(assertEquals(_, t))
}

object EnvironmentsSuite {
  private implicit val arbData: Arbitrary[Data] = Arbitrary(
    Gen.alphaStr.map(Data(_))
  )
  private val variables = Gen.resultOf(Environment.Variable)
  private val secretVars = Gen.resultOf(Environment.SecretVariable)
  private val configVars = Gen.resultOf(Environment.ConfigVariable)
  private val configFiles = Gen.resultOf(Environment.ConfigFile)
  private val secretFiles = Gen.resultOf(Environment.SecretFile)
  private val externalConfigFiles = Gen.resultOf(Environment.ExternalConfigFile)
  private val externalSecretFiles = Gen.resultOf(Environment.ExternalSecretFile)
  private val externalConfigVars =
    Gen.resultOf(Environment.ExternalConfigVariable)
  private val externalSecretVars =
    Gen.resultOf(Environment.ExternalSecretVariable)
}
