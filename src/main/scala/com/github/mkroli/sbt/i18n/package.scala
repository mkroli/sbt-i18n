package com.github.mkroli.sbt

import java.io.{File, FileInputStream}
import scala.collection.JavaConverters._

package object i18n {
  private[i18n] def loadProperties(f: File) = {
    val props = new java.util.Properties
    val fis   = new FileInputStream(f)
    try {
      props.load(fis)
    } finally {
      fis.close()
    }
    props
  }

  private[i18n] def loadPropertyKeys(f: File) = {
    loadProperties(f).keySet.asScala.map(_.toString).toSet
  }
}
