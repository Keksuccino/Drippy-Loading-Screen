# Repository Guidelines

## Project Structure & Module Organization
- Drippy Loading Screen is a Minecraft Java 26.1.1 mod (the version number is not a typo) that uses the MultiLoader layout with shared logic under `common` and loader-specific wrappers under `fabric` and `neoforge`.
- It also has a special `earlywindow` submodule for NeoForge's "Early Loading Screen".
- Place shared Java sources in `common/src/main/java` and assets such as menu JSON, translations, or textures in `common/src/main/resources` so they ship with every loader build.
- Loader-only hooks belong inside each module's `src/main/java` tree; keep local run directories like `run_client` and `run_server` for iterative testing but never depend on them for assets.

## Environment
- You are operating in a WSL2 environment running inside a Windows system.

## Coding Style & Naming Conventions
- Target Java 25 with 4-space indentation and UTF-8 encoding (WITHOUT BOM), matching the Gradle toolchain configuration.
- Follow existing packages under `de.keksuccino.drippyloadingscreen`, mirroring existing sub-packages to keep cross-loader boundaries clear.
- Name resources with the `drippyloadingscreen` prefix (e.g., `drippyloadingscreen.mixins.json`, `drippyloadingscreen.accesswidener`) so Gradle and the loaders resolve them consistently.
- Prefer explicit nullability annotations from `jsr305`.
- Keep Mixin classes lightweight.

## Mixin Structurization
- Place shared mixins under `common/src/main/java/de/keksuccino/drippyloadingscreen/mixin/mixins/common/<side>` and mirror the existing folder depth when adding new targets.
- Declare `@Mixin` classes (and accessor interfaces) with imports grouped at the top, list `@Unique` members before any `@Shadow` declarations, and extend or implement the vanilla type when necessary; supply a suppressed dummy constructor when subclasses require it.
- Suffix every unique field or helper with `_Drippy`. Static finals use all caps with `_DRIPPY`, and injected method names follow the `before/after/on/wrap/cancel_<VanillaMethod>_Drippy` pattern. Accessor/invoker methods also end in `_Drippy`.
- Cluster related injections together (for example, all `setScreen` hooks in `MixinMinecraft`) and keep helper wrappers private unless a wider contract is required.
- Use short `//` comments for quick reminders and `/** @reason ... */` blocks ahead of injections that change vanilla behaviour, matching the authoring tone in existing files.
- Drippy Loading Screen has access to Mixin Extras.
- Prefer using features from Mixin Extras instead of using normal Mixin redirects or overrides.
- When leveraging Mixin Extras (`WrapOperation`, `WrapWithCondition`, etc.), name helpers after the intent (`wrap_..._Drippy`, `cancel_..._Drippy`) and call the provided `Operation` when returning to vanilla flow.

## Localization
- Always add en_us localizations for the features you add. Only en_us.
- The en_us.json file is pretty large, too large for you to read the full file, so if you need something from it, search for specific lines.
- ALWAYS add new locals to the END OF THE FILE (without breaking the JSON syntax).
- When you add something to a system that already has localizations available for other parts of the system, first read the existing localizations to understand how the new localizations should get formatted.
- Always read and write en_us.json with an explicit UTF-8-without-BOM encoding.

## Minecraft Sources
- You have access to the full Minecraft 26.1.1 sources in `/library_sources/minecraft_26.1.1/fabric/` and `/library_sources/minecraft_26.1.1/neoforge/`.
- You have access to the full Minecraft 1.21.11 sources in `/library_sources/minecraft_1.21.11/fabric/` and `/library_sources/minecraft_1.21.11/neoforge/`.
- Use the Minecraft sources for research when working with Minecraft-related code.
- Always prefer the sources provided in the `/library_sources/` folder instead of trying to unpack source JARs yourself. Only do that when the provided sources don't contain what you need.
- Minecraft 1.21.11 is the version before Minecraft 26.1.1.

## Testing
- After making changes to the code, use the repo-root launcher script `./run-loader-wsl.sh` from WSL to run loader dev clients for testing. It intentionally calls Windows `gradlew.bat`, not Linux `gradlew`, so it reuses the existing Windows Gradle cache and Windows Java installation instead of requiring a separate WSL toolchain.
- The shared instance directories are the repo-root `run_client` and `run_server` folders for both loaders.
- Supported launch forms are `./run-loader-wsl.sh fabric`, `./run-loader-wsl.sh fabric server`, `./run-loader-wsl.sh neoforge`, and `./run-loader-wsl.sh neoforge server`. Additional Gradle arguments can be appended, for example `./run-loader-wsl.sh fabric --stacktrace`.
- For command-line control of the running game, the launcher must be started in an interactive TTY session. Plain pipe-based execution shows the game log, but stdin ends up closed and `konkretedebug` commands cannot be delivered.
- When the TTY session starts, `cmd.exe` may emit a cursor-position query escape (`ESC [ 6 n`). Reply with `\u001b[1;1R` once so the Windows console handshake completes and Gradle output continues normally.
- Wait for the Konkrete readiness line before sending commands: `[KONKRETE DEBUG] Command line debugging is ready. Use 'konkretedebug help'.`
- Send debug commands with CRLF line endings, for example `konkretedebug help\r\n`. Using only `\n` is less reliable through the Windows console chain.
- A proven Fabric test flow is:
    - Launch `./run-loader-wsl.sh fabric` in a TTY.
    - Answer the initial `cmd.exe` cursor query with `\u001b[1;1R` if it appears.
    - Wait for the Konkrete debug readiness line.
    - Send `konkretedebug help\r\n` to see all available debug commands, including ones to navigate in menus and load into a world.
- Before launching another test client, make sure no previous repo-backed `java.exe`, `javaw.exe`, or `cmd.exe` processes of the game are still running. Do not stack multiple leftover instances.
