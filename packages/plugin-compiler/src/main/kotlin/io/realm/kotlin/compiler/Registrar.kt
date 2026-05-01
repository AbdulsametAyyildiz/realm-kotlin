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

package io.realm.kotlin.compiler

import com.google.auto.service.AutoService
import io.realm.kotlin.compiler.fir.model.RealmModelRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

/**
 * Registrar for the Realm compiler plugin.
 *
 * Modern (K2-only) registrar. Schema/companion generation is performed by the FIR
 * extension `RealmModelRegistrar`; managed-object accessor rewiring and the
 * `RealmObjectInternal` mixin are emitted by `RealmModelLoweringExtension` at IR.
 *
 * Kotlin 2.0+ defaults to K2; the legacy K1 `SyntheticResolveExtension`
 * registrations were dropped in Kotlin 2.3 because the surrounding API
 * (`ComponentRegistrar`, `MockProject.extensionArea`) is no longer accessible
 * from compiler plugins.
 */
@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class Registrar : CompilerPluginRegistrar() {

    override val pluginId: String = "io.realm.kotlin.plugin-compiler"
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        messageCollector = configuration.get(
            CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            MessageCollector.NONE
        )
        SchemaCollector.properties.clear()

        // K2: FIR extension that synthesises companions, schema methods, etc.
        FirExtensionRegistrarAdapter.registerExtension(RealmModelRegistrar())

        // IR lowering: adds RealmObjectInternal, rewires accessors, captures
        // companion objects from RealmConfiguration constructor calls.
        IrGenerationExtension.registerExtension(RealmModelLoweringExtension())
    }
}
