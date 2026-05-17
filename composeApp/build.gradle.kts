import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.kotlinSerialization)
}

// Build flavor: pass -Pflavor=prod for real API. Default is dev (mock data).
val auditForgeFlavor: String = (project.findProperty("flavor") as? String) ?: "dev"
val isDevFlavor: Boolean = auditForgeFlavor != "prod"

val flavorOutputDir = layout.buildDirectory.dir("generated/source/appflavor")

abstract class GenerateAppFlavorTask : DefaultTask() {
    @get:Input
    abstract val flavor: Property<String>

    @get:Input
    abstract val useMockData: Property<Boolean>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outFile = outputDir.get().asFile.resolve("com/devscion/auditforge/AppFlavor.kt")
        outFile.parentFile.mkdirs()
        outFile.writeText(
            """
            |package com.devscion.auditforge
            |
            |object AppFlavor {
            |    const val FLAVOR: String = "${flavor.get()}"
            |    const val USE_MOCK_DATA: Boolean = ${useMockData.get()}
            |}
            |
            """.trimMargin()
        )
    }
}

val generateAppFlavor by tasks.registering(GenerateAppFlavorTask::class) {
    flavor.set(auditForgeFlavor)
    useMockData.set(isDevFlavor)
    outputDir.set(flavorOutputDir)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    jvm()


    js {
        browser()
        binaries.executable()
    }

//    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {
        commonMain {
            kotlin.srcDir(flavorOutputDir)
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(compose.materialIconsExtended)

                //Koin
                implementation(libs.koin.core)
                implementation(libs.koin.annotations)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose.viewmodel.navigation)

                // Navigation
                implementation(libs.navigation.compose)
                //Serialization
                implementation(libs.kotlinx.serialization.json)

                // Ktor Client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)

            }
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.okhttp)
        }
    }
}

tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
    dependsOn(generateAppFlavor)
}


compose.desktop {
    application {
        mainClass = "com.devscion.auditforge.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.devscion.auditforge"
            packageVersion = "1.0.0"
        }
    }
}
