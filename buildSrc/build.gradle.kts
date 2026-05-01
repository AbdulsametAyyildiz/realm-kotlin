/*
 * Copyright 2020 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Add support for precompiled script plugins: https://docs.gradle.org/current/userguide/custom_plugins.html#sec:precompiled_plugins
// Note: kotlin-dsl-precompiled-script-plugins is bundled into kotlin-dsl since Gradle 6.0.
// Note: kotlin("jvm") removed — let kotlin-dsl auto-provide Gradle's embedded Kotlin to avoid
//       version mismatch with the Kotlin compiler bundled in Gradle.
plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("realm-publisher") {
            id = "realm-publisher"
            implementationClass = "org.realm.kotlin.RealmPublishPlugin"
        }
//        create("realm-compiler") { // Plugin name (used internally)
//            id = "tr.com.maverasoft.realm.kotlin.plugin-compiler" // Plugin ID (used to apply)
//            implementationClass = "io.realm.kotlin.compiler.Registrar"
//        }
    }
}

java {
    sourceCompatibility = Versions.sourceCompatibilityVersion
    targetCompatibility = Versions.targetCompatibilityVersion
}

// Kotlin 2.3 dropped language version 1.8 support; kotlin-dsl defaults to 1.8.
// K2 LightTree mode also doesn't support precompiled script plugins yet, so disable it.
// Override on every KotlinCompile task (covers `compilePluginsBlocks` too).
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
        freeCompilerArgs.add("-Xuse-fir-lt=false")
    }
}


repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}


// Setup dependencies for building the buildScript.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}") // Use the latest version
    }
}

// Setup dependencies for the buildscripts consuming the precompiled plugins
// These seem to propagate to all projects including the buildSrc/ directory, which also means
// they are not allowed to set the version. It can only be set from here.
dependencies {
    implementation(kotlin("gradle-plugin", version = Versions.kotlin))
    implementation("com.gradleup.nmcp.aggregation:com.gradleup.nmcp.aggregation.gradle.plugin:1.1.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Versions.detektPlugin}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:${Versions.kotlin}")
    implementation("org.gradle.kotlin:gradle-kotlin-dsl-plugins:5.2.0")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}")
    implementation("com.android.tools:r8:${Versions.Android.r8}")
    implementation("com.android.tools.build:gradle:${Versions.Android.buildTools}")
    implementation(kotlin("script-runtime"))
}
