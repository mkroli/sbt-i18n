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

import sbt.Defaults.collectFiles
import sbt.Keys._
import sbt._

object VerifyTranslations {

  import InternationalisationPlugin.autoImport._

  private object Filename {
    def unapply(f: java.io.File): Option[String] = Some(f.getName)
  }

  private val ResourceBundleFilename = """([^_.]+)(?:_(.*))?\.properties""".r

  val verify = Def.task {
    val log = streams.value.log
    val bundles = collectFiles(
      sourceDirectories in internationalisationGenerate,
      includeFilter in internationalisationGenerate,
      excludeFilter in internationalisationGenerate
    ).value.toSet
    val locales = internationalisationVerifyLocales.value.toSet

    bundles
      .groupBy {
        case Filename(ResourceBundleFilename(base, _)) => base
      }
      .mapValues { files =>
        val base = files.collectFirst {
          case f @ Filename(name) if !name.contains("_") => f
        }
        (base, files)
      }
      .collect {
        case (key, (Some(base), files)) => key -> (base, files)
      }
      .foreach {
        case (base, (basefile, translations)) =>
          translations.foreach {
            case f @ Filename(ResourceBundleFilename(_, locale)) => {
              if (locales(locale)) {
                val baseProps       = loadPropertyKeys(basefile)
                val translatedProps = loadPropertyKeys(f)

                log.info(s"$basefile $f")

                if (baseProps == translatedProps) {
                  log.info(s"Verified locale $locale for internationalisation $base")
                } else {
                  def s(prefix: String, content: Set[String]) = if (content.isEmpty) "" else s" (${prefix}: ${content.mkString(", ")})"

                  val missing = baseProps -- translatedProps
                  val extra   = translatedProps -- baseProps
                  sys.error(s"Verifying locale $locale for internationalisation $base failed:${s("missing", missing)}${s("extra", extra)}")
                }
              }
            }
          }
      }
  }
}
