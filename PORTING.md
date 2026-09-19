# Minecraft 26.3 preparation

The game identifier is `26.3`; the workspace and branch are named `26.3.0`.
The build uses Java 25, Gradle 9.7.1, Loom 1.18.2, Fabric Loader 0.19.5,
Fabric API 0.161.0+26.3, and NeoForge 26.3.0.6-beta with NeoGradle 7.1.39.
The early-window module uses FML 12.0.0 and Shadow 9.6.1. Its LWJGL baseline
remains 3.4.1, matching FML's separate early-display implementation.
Java sources and Mixin targets still need the code port.

Konkrete 1.11.1 and Melody 1.0.17 use their published 26.3 builds.
FancyMenu has no published 26.3 release as of 2026-09-19. Its latest 26.2 build
(3.9.12) is retained only as a compile-time API. It is deliberately absent from
the development runtime, so clients cannot run until the required dependency
is available.

After FancyMenu is ported and published, set `fancymenu_minecraft_version=26.3`
and, if necessary, update `fancymenu_version` in `gradle.properties`. Both loader
modules then automatically add the selected build to their runtime classpaths.

Run validation with Java 25:

```sh
JAVA_HOME=$(/usr/libexec/java_home -v 25) sh gradlew :fabric:test :fabric:compileJava :neoforge:compileJava --continue --stacktrace
```

The NeoForge compile also checks the early-window dependency.
Preparation logs are under the ignored `build/reports/port-preparation/` directory.

Validation on 2026-09-19:

- Gradle configuration, wrapper regeneration, resource processing, and artifact
  resolution passed for both loaders and all declared classpaths.
- Both loaders fail on the old RenderPipeline and ShaderType references. The early-window module compiles, and its shaded JAR builds with the relocated Japng classes and project license.
- The normal `:fabric:test :fabric:compileJava :neoforge:compileJava --continue`
  invocation stops downstream of shared production compilation. A separate
  diagnostic invocation excluded `:common:compileJava` so each loader could
  compile the shared sources directly and expose its own errors. This exclusion
  was only a command-line diagnostic; the build configuration was not weakened.
- Source inventory: 0 test classes containing 0 annotated test methods.
  JUnit did not start: 0 executed, 0 passed, 0 failed, 0 errored, 0 skipped.
  These totals do not indicate passing tests. No game client was launched.

The selected versions were checked against the
[Fabric repositories](https://maven.fabricmc.net/),
[NeoForge repositories](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.3.0.6-beta/),
and [Gradle release metadata](https://services.gradle.org/versions/current).
Minecraft's [26.3 release notes](https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3)
specify resource pack version 97.1 and the switch from GLFW to SDL3.

Track the missing dependency on the [FancyMenu release page](https://modrinth.com/mod/fancymenu/versions).
