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

object GenerateLabels {

  import InternationalisationPlugin.autoImport._

  val generate = Def.task {
    val log = streams.value.log
    val bundles = collectFiles(
      sourceDirectories in internationalisationGenerate,
      includeFilter in internationalisationGenerate,
      excludeFilter in internationalisationGenerate
    ).value.toSet
    val cache = streams.value.cacheDirectory / s"i18n_${scalaBinaryVersion.value}"

    val _mappings = Path.rebase((sourceDirectories in internationalisationGenerate).value, (target in internationalisationGenerate).value)

    def mappings(f: File) = {
      val mappedFile = _mappings(f).get
      mappedFile.getParentFile / (mappedFile.getName.dropRight(".properties".size) + "Labels.scala")
    }

    val cachedCompile = FileFunction.cached(cache, inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) { in =>
      in.collect {
        case in if !in.getName.contains("_") => {
          val out = mappings(in)
          val pkg = out.relativeTo((target in internationalisationGenerate).value).get.getParent.replace(java.io.File.separatorChar, '.')
          val cls = out.getName.dropRight(".scala".size)
          log.info(s"Generating ${out}")
          val keys = loadPropertyKeys(in)
          IO.write(out, GenerateLabels.template(pkg, cls, keys))
          out
        }
      }
    }

    cachedCompile(bundles).toSeq
  }

  private def template(pkg: String, cls: String, keys: Set[String]) = {
    val members = keys.map { key =>
      s"""  val `${key}` = resourceBundle.getString("${key}")"""
    }
    val baseName = s"${pkg.replace('.', '/')}/${cls.dropRight("Labels".size)}"
    s"""package ${pkg}
       |
       |import java.util.{Locale, ResourceBundle}
       |
       |class ${cls} private(resourceBundle: ResourceBundle) {
       |${members.mkString("\n")}
       |}
       |
       |object ${cls} extends ${cls}(ResourceBundle.getBundle("${baseName}")) {
       |  private val baseName = "${baseName}"
       |
       |  def apply(locale: Locale) = new ${cls}(ResourceBundle.getBundle(baseName, locale))
       |  def apply(locale: Locale, loader: ClassLoader) = new ${cls}(ResourceBundle.getBundle(baseName, locale, loader))
       |  def apply(locale: Locale, loader: ClassLoader, control: ResourceBundle.Control) = new ${cls}(ResourceBundle.getBundle(baseName, locale, loader, control))
       |  def apply(locale: Locale, control: ResourceBundle.Control) = new ${cls}(ResourceBundle.getBundle(baseName, control))
       |  def apply(control: ResourceBundle.Control) = new ${cls}(ResourceBundle.getBundle(baseName, control))
       |}
       |""".stripMargin
  }
}
