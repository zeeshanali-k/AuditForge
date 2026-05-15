package com.devscion.auditforge.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [],
    configurations = []
)
@ComponentScan("com.devscion.auditforge")
class AuditForgeKoinApp