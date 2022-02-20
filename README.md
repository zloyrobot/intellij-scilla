# Scilla Language Plugin

[**github.com/zloyrobot/intellij-scilla**](https://github.com/zloyrobot/intellij-scilla)

Scilla language plugin for IDEA.

![Build Status](https://github.com/zloyrobot/intellij-scilla/actions/workflows/gradle.yml/badge.svg)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

![screenshot](screenshot.png)


### Building the plugin and launching it in a sandbox

1. Install SDK and prepare backend plugin build using Gradle
    * if using IntelliJ IDEA:

      Open the `intellij-scilla` project in IntelliJ IDEA. `intellij-scilla` uses the [gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin) Gradle plugin that downloads the IntelliJ Platform SDK, packs the plugin and installs it into a sandboxed IDE.

      Open the *Gradle* tool window in IntelliJ IDEA (*View | Tool Windows | Gradle*), and execute the `scilla/Tasks/intellij/buildPlugin` task.

    * if using Gradle command line:

        ```
        $ cd ./intellij-scilla
        $ ./gradlew buildPlugin
        ```

2. Launch IDEA with the plugin installed

    * if using IntelliJ IDEA:

      Open the *Gradle* tool window in IntelliJ IDEA (*View | Tool Windows | Gradle*), and execute the `scilla/Tasks/intellij/runIde` task. This will install the plugin to a sandbox, and launch IDEA with the plugin.

    * if using Gradle command line:

        ```
        $ ./gradlew runIde
        ```

### Installing to an existing IDEA instance

1. Execute the `buildPlugin` Gradle task.
    
   
        $ cd ./intellij-scilla
        $ ./gradlew buildPlugin
        
2. Install the plugin (`intellij-scilla/build/distributions/Scilla.zip`) to your IDEA installation [from disk](https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk).
