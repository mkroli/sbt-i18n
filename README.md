# sbt-i18n

## Overview

sbt-i18n generates Scala code from properties-files. Additionally it will check configured translations for completeness.
The generated code still uses [java.util.ResourceBundle](https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/util/ResourceBundle.html).
This combines the flexibility of changing translations without re-compilation and compile-time checks of used labels.

Given the property file ```test/Test.properties```:
```properties
yes: Yes
no: No
```
The following will be generated:
```scala
package test

import java.util.{Locale, ResourceBundle}

class TestLabels private(resourceBundle: ResourceBundle) {
  val `yes` = resourceBundle.getString("yes")
  val `no` = resourceBundle.getString("no")
}

object TestLabels extends TestLabels(ResourceBundle.getBundle("test/Test")) {
  private val baseName = "test/Test"

  def apply(locale: Locale) = new TestLabels(ResourceBundle.getBundle(baseName, locale))
  def apply(locale: Locale, loader: ClassLoader) = new TestLabels(ResourceBundle.getBundle(baseName, locale, loader))
  def apply(locale: Locale, loader: ClassLoader, control: ResourceBundle.Control) = new TestLabels(ResourceBundle.getBundle(baseName, locale, loader, control))
  def apply(locale: Locale, control: ResourceBundle.Control) = new TestLabels(ResourceBundle.getBundle(baseName, control))
  def apply(control: ResourceBundle.Control) = new TestLabels(ResourceBundle.getBundle(baseName, control))
}
```
This can be accessed as follows:
```scala
test.TestLabels.yes
test.TestLabels.no
```

## Usage

### Installation

Add the plugin according to [sbt-documentation](https://www.scala-sbt.org/1.x/docs/Using-Plugins.html).

For instance, add the following lines to the file ```project/plugins.sbt``` in your project directory:

```sbt
resolvers += Resolver.bintrayIvyRepo("mkroli", "sbt-plugins")

addSbtPlugin("com.github.mkroli" % "sbt-i18n" % "0.1")
```

### Settings / Tasks

The following new settings are introduced:
```sbt
Compile / internationalisationVerifyLocales += "de"
```

The following new tasks are introduced:
```sbt
internationalisationGenerate
internationalisationVerify
```

```internationalisationGenerate``` will by default generate code for files in "src/main/i18n/*.properties".
```internationalisationVerify``` will by default verify completeness of translations defined in the setting ```Compile / internationalisationVerifyLocales```.
Both are automatically executed during the ```compile``` step.

### License

sbt-i18n is licensed under the [Apache License, Version 2.0](LICENSE).
