package com.devscion.auditforge

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.devscion.auditforge.di.AuditForgeKoinApp
import org.koin.plugin.module.dsl.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    startKoin<AuditForgeKoinApp>()
    ComposeViewport {
        App()
    }
}