package com.devscion.auditforge

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.devscion.auditforge.di.AuditForgeKoinApp
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.startKoin

fun main() = application {
    startKoin<AuditForgeKoinApp>()
    Window(
        onCloseRequest = ::exitApplication,
        title = "AuditForge",
    ) {
        App()
    }
}