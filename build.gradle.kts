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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget.Companion.fromTarget

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
    }
}

plugins {
    id("realm-publisher")
}

val subprojects = listOf("packages")
fun taskName(subdir: String): String {
    return subdir.split("/", "-").map { it.replaceFirstChar { c -> c.uppercase() } }.joinToString(separator = "")
}

allprojects {
   version = Realm.version
   group = Realm.group

   // Define JVM bytecode target for all Kotlin targets
   tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile> {
       compilerOptions {
           jvmTarget.set(fromTarget(Versions.kotlinJvmTarget))
       }
   }
}

tasks {
    register("ktlintCheck") {
        description = "Runs ktlintCheck on all projects."
        group = "Verification"
        dependsOn(subprojects.map { "ktlintCheck${taskName(it)}" })
    }

    register("ktlintFormat") {
        description = "Runs ktlintFormat on all projects."
        group = "Formatting"
        dependsOn(subprojects.map { "ktlintFormat${taskName(it)}" })
    }

    register("detekt") {
        description = "Runs detekt on all projects."
        group = "Verification"
        dependsOn(subprojects.map { "detekt${taskName(it)}" })
    }

    subprojects.forEach { subdir ->
        register<Exec>("ktlintCheck${taskName(subdir)}") {
            description = "Run ktlintCheck on /$subdir project"
            workingDir = file("${rootDir}/$subdir")
            commandLine = listOf("./gradlew", "ktlintCheck")
        }
        register<Exec>("ktlintFormat${taskName(subdir)}") {
            description = "Run ktlintFormat on /$subdir project"
            workingDir = file("${rootDir}/$subdir")
            commandLine = listOf("./gradlew", "ktlintFormat")
        }
        register<Exec>("detekt${taskName(subdir)}") {
            description = "Run detekt on /$subdir project"
            workingDir = file("${rootDir}/$subdir")
            commandLine = listOf("./gradlew", "detekt")
        }
    }

     register<Task>("publishCIPackages") {
         group = "Publishing"
         description = "Publish packages that has been configured for this CI node. See `gradle.properties`."

         // Figure out which targets are configured. This will impact which sub modules will be published
         val availableTargets = setOf(
             "iosArm64",
             "iosX64",
             "jvm",
             "macosX64",
             "macosArm64",
             "android",
             "metadata",
             "compilerPlugin",
             "gradlePlugin"
         )

         val mainHostTarget: Set<String> = setOf("metadata") // "kotlinMultiplatform"

         val isMainHost: Boolean = project.properties["realm.kotlin.mainHost"]?.let { it == "true" } ?: false

         // Find user configured platforms (if any)
         val userTargets: Set<String>? = (project.properties["realm.kotlin.targets"] as String?)
             ?.split(",")
             ?.map { it.trim() }
             ?.filter { it.isNotEmpty() }
             ?.toSet()

         userTargets?.forEach {
             if (!availableTargets.contains(it)) {
                 project.logger.error("Unknown publication: $it")
                 throw IllegalArgumentException("Unknown publication: $it")
             }
         }

         // Configure which platforms publications we do want to publish
         val publicationTargets = (userTargets ?: availableTargets).let {
             when (isMainHost) {
                 true -> it + mainHostTarget
                 false -> it - mainHostTarget
             }
         }

         publicationTargets.forEach { target: String ->
             when (target) {
                 "iosArm64" -> {
                     dependsOn(
                         ":packages:cinterop:publishIosArm64PublicationToTestRepository",
                         ":packages:cinterop:publishIosSimulatorArm64PublicationToTestRepository",
                         ":packages:library-base:publishIosArm64PublicationToTestRepository",
                         ":packages:library-base:publishIosSimulatorArm64PublicationToTestRepository",
//                         ":packages:library-sync:publishIosArm64PublicationToTestRepository",
//                         ":packages:library-sync:publishIosSimulatorArm64PublicationToTestRepository",
                     )
                 }
                 "iosX64" -> {
                     dependsOn(
                         ":packages:cinterop:publishIosX64PublicationToTestRepository",
                         ":packages:library-base:publishIosX64PublicationToTestRepository",
//                         ":packages:library-sync:publishIosX64PublicationToTestRepository",
                     )
                 }
                 "jvm" -> {
                     dependsOn(
                         ":packages:jni-swig-stub:publishAllPublicationsToTestRepository",
                         ":packages:cinterop:publishJvmPublicationToTestRepository",
                         ":packages:library-base:publishJvmPublicationToTestRepository",
//                         ":packages:library-sync:publishJvmPublicationToTestRepository",
                     )
                 }
                 "macosX64" -> {
                     dependsOn(
                         ":packages:cinterop:publishMacosX64PublicationToTestRepository",
                         ":packages:library-base:publishMacosX64PublicationToTestRepository",
//                         ":packages:library-sync:publishMacosX64PublicationToTestRepository",
                     )
                 }
                 "macosArm64" -> {
                     dependsOn(
                         ":packages:cinterop:publishMacosArm64PublicationToTestRepository",
                         ":packages:library-base:publishMacosArm64PublicationToTestRepository",
//                         ":packages:library-sync:publishMacosArm64PublicationToTestRepository",
                     )
                 }
                 "android" -> {
                     dependsOn(
                         ":packages:jni-swig-stub:publishAllPublicationsToTestRepository",
                         ":packages:cinterop:publishAndroidReleasePublicationToTestRepository",
                         ":packages:library-base:publishAndroidReleasePublicationToTestRepository",
//                         ":packages:library-sync:publishAndroidReleasePublicationToTestRepository",
                     )
                 }
                 "metadata" -> {
                     dependsOn(
                         ":packages:cinterop:publishKotlinMultiplatformPublicationToTestRepository",
                         ":packages:library-base:publishKotlinMultiplatformPublicationToTestRepository",
//                         ":packages:library-sync:publishKotlinMultiplatformPublicationToTestRepository",
                     )
                 }
                 "compilerPlugin" -> {
                     dependsOn(
                         ":packages:plugin-compiler:publishAllPublicationsToTestRepository",
                         ":packages:plugin-compiler-shaded:publishAllPublicationsToTestRepository"
                     )
                 }
                 "gradlePlugin" -> {
                     dependsOn(":packages:gradle-plugin:publishAllPublicationsToTestRepository")
                 }
                 else -> {
                     throw IllegalArgumentException("Unsupported target: $target")
                 }
             }
         }
     }
}
