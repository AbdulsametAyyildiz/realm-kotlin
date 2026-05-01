# Abdülsamet's fork of [Realm Kotlin](https://github.com/realm/realm-kotlin)

A fork of the deprecated [Realm Kotlin](https://github.com/realm/realm-kotlin),
modernised for Kotlin 2.3.21 + AGP 8.13.2 with the compiler plugin ported to
the K2 compiler API. The artifact group has been re-branded so it can be
consumed as a drop-in replacement for the legacy `io.realm.kotlin` dependency
without colliding with it on the classpath.

We have checked the diff between this revision and Realm's original project
to be exempt from any suspicious code or reference to unchecked binaries.

## Android consumer compatibility

This library is compiled with AGP **8.13.2**, but the gradle plugin uses
`compileOnly` against AGP, so the consumer brings their own AGP at apply time.
That means you can drop this library into an Android app on **any of:**

| Consumer AGP                  | Status                                            |
|-------------------------------|---------------------------------------------------|
| 8.13.x                        | ✅ Same version we build with — safest baseline.  |
| **9.0 / 9.1 / 9.2** (and 9.x onward) | ✅ Verified end-to-end with AGP 9.2.0 + Gradle 9.4.1 + Kotlin 2.3.21 on a real Android consumer app — compile, install, and full Realm CRUD/Flow/observer pass on device. |
| Older AGP 8.x (< 8.13)        | ⚠ Not tested in this fork; should work because the AAR/APK formats are stable, but Kotlin 2.3.21 is a hard requirement. |

The only AGP-9-specific caveat is for **consumers who are themselves Kotlin
Multiplatform libraries** with an Android target: AGP 9 deprecates the
`com.android.library` + `kotlin("multiplatform")` combination in favour of
`com.android.kotlin.multiplatform.library`, which currently lacks
`externalNativeBuild` support. That migration is a separate concern at the
consumer's KMP module — it does not affect plain Android apps consuming this
library.

## Other-platform consumer compatibility

| Target              | Published variants                            |
|---------------------|-----------------------------------------------|
| Android             | arm64-v8a, armeabi-v7a, x86, x86_64           |
| iOS device          | iosArm64                                      |
| iOS simulator       | iosSimulatorArm64, iosX64                     |
| JVM (desktop / server) | jvm (macOS arm64 native lib bundled)       |
| macOS desktop       | macosArm64                                    |

Intel macOS desktop (`macosX64`) is intentionally not published — see the
"What's NOT in this fork" section below.

## Version compatibility

| Fork version | Kotlin | AGP    | Gradle  | minSdk | compileSdk |
|--------------|--------|--------|---------|--------|------------|
| 3.3.0        | 2.3.21 | 8.13.2 | 8.14.3  | 21     | 36         |

## Coordinates

Group ID: `tr.com.maverasoft.realm.kotlin`
Version:  `3.3.0`

The library is published to **GitHub Packages** at:
<https://github.com/AbdulsametAyyildiz/realm-kotlin/packages>

It is **not** mirrored to Maven Central or JitPack.

## Authentication (required, even though the repo is public)

GitHub Packages requires authentication to download artifacts, even from public
repositories. This is a GitHub design choice, not a configuration of this fork.

Each consumer needs:

1. A **GitHub Personal Access Token** (Classic) with the `read:packages` scope.
   Create one at <https://github.com/settings/tokens>.
2. The token plus their own GitHub username, supplied either as Gradle
   properties or environment variables.

The recommended pattern is to put credentials in `~/.gradle/gradle.properties`
(outside any repository, so they are never committed):

```properties
# ~/.gradle/gradle.properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=ghp_YOUR_PERSONAL_ACCESS_TOKEN_WITH_read:packages
```

CI environments can rely on the standard `GITHUB_ACTOR` and `GITHUB_TOKEN`
environment variables instead — the snippets below fall back to them.

## Consumer setup

### `settings.gradle[.kts]`

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/AbdulsametAyyildiz/realm-kotlin")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/AbdulsametAyyildiz/realm-kotlin")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

### Root `build.gradle[.kts]`

```kotlin
buildscript {
    dependencies {
        classpath("tr.com.maverasoft.realm.kotlin:gradle-plugin:3.3.0")
    }
}
```

### Module `build.gradle[.kts]`

```kotlin
plugins {
    id("com.android.application")          // or com.android.library
    id("org.jetbrains.kotlin.android")
}
apply(plugin = "tr.com.maverasoft.realm.kotlin")

dependencies {
    implementation("tr.com.maverasoft.realm.kotlin:library-base:3.3.0")
}
```

That is the full consumer surface. Internally Gradle resolves the right
platform variant for each Kotlin target (`-android`, `-iosarm64`,
`-iossimulatorarm64`, `-iosx64`, `-jvm`, `-macosarm64`) automatically.

### Migrating from the upstream Realm Kotlin

If you previously depended on `io.realm.kotlin:library-base:1.x`:

1. Replace every `io.realm.kotlin:` Maven coordinate with
   `tr.com.maverasoft.realm.kotlin:`.
2. Drop the version from any `id("io.realm.kotlin")` declaration in the
   `plugins` block (the version comes from the buildscript classpath now).
3. Bring your project to Kotlin **2.3.21** (older Kotlin versions are not
   supported by the K2-based compiler plugin in this fork).

The on-disk file format is unchanged, so existing `.realm` files keep working.

## What's NOT in this fork

- **MongoDB Atlas Sync** (`library-sync`, `kbson` mongo serializers, App
  Services login, flexible/partition sync). The whole sync pipeline was
  removed because this fork is for offline-only use cases.
- **Pre-publish CI tasks** (S3 debug-symbol upload, Sonatype upload,
  dokka HTML upload). These targeted Realm Inc.'s own infrastructure.
- **`com.android.kotlin.multiplatform.library` migration** (AGP 9 KMP plugin).
  realm-core requires `externalNativeBuild` for CMake/NDK, which the new
  plugin does not yet support.
- **macOS x86_64** (Intel desktop) variants — AppleClang 21 (Xcode 26.4) fails
  Universal binary detection and we don't have a use for Intel macOS desktop
  binaries.

## Project structure

```
packages/
  cinterop/            KMP module wrapping realm-core via JNI
  library-base/        User-facing API (RealmObject, queries, Flows)
  gradle-plugin/       Gradle plugin entry point
  plugin-compiler/     Kotlin K2 compiler plugin
  plugin-compiler-shaded/  Shaded variant of the compiler plugin
  jni-swig-stub/       SWIG-generated JNI bindings
  external/core        realm-core C++ submodule (private to this build)
```

`gradlew` must be run from the root directory, not from `packages/`.

## Native build prerequisites

Realm-core is a C++ database engine compiled per target. To build this fork
locally you need:

- **JDK 17**
- **Android SDK** with NDK `27.0.12077973` installed at
  `~/Library/Android/sdk/ndk/`
- **Xcode 26+** (for iOS / macOS targets)
- `cmake`, `ninja`, `ccache`, `swig` available on `PATH`
  (`brew install cmake ninja ccache swig` on macOS)
- Submodules initialised:
  `git submodule update --init --recursive`

## Building / publishing locally

```bash
# Run all unit tests on the JVM target
./gradlew jvmTest

# Publish every artifact to your local Maven cache (~/.m2/)
./gradlew \
    :packages:cinterop:publishAllPublicationsToMavenLocal \
    :packages:library-base:publishAllPublicationsToMavenLocal \
    :packages:gradle-plugin:publishToMavenLocal \
    :packages:plugin-compiler:publishToMavenLocal \
    :packages:plugin-compiler-shaded:publishToMavenLocal \
    :packages:jni-swig-stub:publishToMavenLocal \
    -x :packages:cinterop:publishMacosX64PublicationToMavenLocal \
    -x :packages:library-base:publishMacosX64PublicationToMavenLocal

# Publish to GitHub Packages (requires GITHUB_TOKEN with write:packages)
./gradlew \
    :packages:cinterop:publishAllPublicationsToGitHubPackagesRepository \
    :packages:library-base:publishAllPublicationsToGitHubPackagesRepository \
    :packages:gradle-plugin:publishAllPublicationsToGitHubPackagesRepository \
    :packages:plugin-compiler:publishAllPublicationsToGitHubPackagesRepository \
    :packages:plugin-compiler-shaded:publishAllPublicationsToGitHubPackagesRepository \
    :packages:jni-swig-stub:publishAllPublicationsToGitHubPackagesRepository \
    -x :packages:cinterop:publishMacosX64PublicationToGitHubPackagesRepository \
    -x :packages:library-base:publishMacosX64PublicationToGitHubPackagesRepository \
    -x :packages:cinterop:capiMacosUniversal \
    -PsignBuild=false
```

------------------------------------

The original Realm Kotlin README is preserved at the
[upstream repository](https://github.com/realm/realm-kotlin).

# Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details.

# License

Realm Kotlin is published under the [Apache 2.0 license](LICENSE).
