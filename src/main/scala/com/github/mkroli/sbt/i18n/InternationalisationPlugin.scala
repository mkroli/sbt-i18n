/*
 * Copyright 2020 Michael Krolikowski
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

package com.github.mkroli.sbt.i18n

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object InternationalisationPlugin extends AutoPlugin {

  object autoImport {
    val internationalisationGenerate      = taskKey[Seq[File]]("Generates internationalisation classes")
    val internationalisationVerifyLocales = settingKey[Seq[String]]("Locales to verify")
    val internationalisationVerify        = taskKey[Unit]("Verifies completeness of translations")
  }

  import autoImport._

  override def requires = JvmPlugin

  override def trigger = allRequirements

  lazy val baseInternationalisationSettings: Seq[Def.Setting[_]] = Seq(
    sourceDirectory in internationalisationGenerate := sourceDirectory.value / "i18n",
    sourceDirectories in internationalisationGenerate := Seq((sourceDirectory in internationalisationGenerate).value),
    includeFilter in internationalisationGenerate := "*.properties",
    internationalisationVerifyLocales := Seq.empty,
    internationalisationVerify := VerifyTranslations.verify.value,
    target in internationalisationGenerate := sourceManaged.value / "i18n",
    internationalisationGenerate := GenerateLabels.generate.dependsOn(internationalisationVerify).value,
    sourceGenerators += internationalisationGenerate.taskValue,
    unmanagedResourceDirectories ++= (sourceDirectories in internationalisationGenerate).value
  )

  override lazy val projectSettings = inConfig(Compile)(baseInternationalisationSettings)
}
