# Abdülsamet's fork of [Realm Kotlin](https://github.com/realm/realm-kotlin)

This is a fork of the deprecated [Realm Kotlin](https://github.com/realm/realm-kotlin),
based on [Infomaniak's fork](https://github.com/Infomaniak/realm-kotlin) which had originally
been made compatible with newer Kotlin versions thanks to the work of @XilinJia on their
[krdb](https://github.com/XilinJia/krdb) fork.

It has been further updated for Kotlin 2.3.21 and modern Android tooling, with a
re-branded artifact group so it can be used as a drop-in replacement when published
to a local or private Maven repository.

We have checked the diff between our revision and Realm's original project to be exempt
from any suspicious code or reference to unchecked binaries.

Project structure is set up to make IntelliJ IDE work.
`gradlew` needs to be run from the root directory rather than `packages/`.

## Version compatibility

| Fork version | Kotlin   | AGP    | Gradle  |
|--------------|----------|--------|---------|
| 3.2.9        | 2.3.21   | 8.13.2 | 8.14.3  |

## How to use

Replace the `io.realm.kotlin` Maven group with `tr.com.maverasoft.realm.kotlin`, and use
the `3.2.9` version.

The artifacts are published to your local Maven repository (`~/.m2/`) via
`./gradlew publishToMavenLocal`. They are not published to Maven Central.

### Setup the repository

In your project's `settings.gradle[.kts]`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        // Other repos...
        mavenLocal() // <-- Add this so the Realm fork is resolvable
    }
}

dependencyResolutionManagement {
    // repositoriesMode...
    repositories {
        // Other repos...
        mavenLocal() // <-- Add this too
    }
}
```

In your project's `build.gradle[.kts]`:

```kotlin
buildscript {
    dependencies {
        classpath("tr.com.maverasoft.realm.kotlin:gradle-plugin:3.2.9")
    }
}
```

* Remove the version from any `id("io.realm.kotlin")` declaration in the `plugins` block.
* Bring your Kotlin to a compatible version (see the table above).

### Build the project

See the [Releasing guide](RELEASING.md) or the [contributing guide](CONTRIBUTING.md).

#### Common Gradle commands

```bash
./gradlew clean
./gradlew jvmTest
./gradlew publishToMavenLocal
```

#### Native build prerequisites

Realm-core is a C++ database engine that must be compiled for each target. To build the
Android target you need:

- JDK 17
- Android SDK with NDK 27.0.12077973 installed (`~/Library/Android/sdk/ndk/...`)
- `cmake`, `ninja`, `ccache`, `swig` available on `PATH` (e.g. `brew install cmake ninja ccache swig`)
- Submodules initialised: `git submodule update --init --recursive`

------------------------------------

Original README of Realm-Kotlin can be found [here](https://github.com/realm/realm-kotlin).

# Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details.

# License

Realm Kotlin is published under the [Apache 2.0 license](LICENSE).
