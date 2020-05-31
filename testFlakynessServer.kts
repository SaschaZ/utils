#!/usr/bin/env kscript
//DEPS io.ktor:ktor-client-apache:1.3.0,io.ktor:ktor-client-gson:1.3.0
//DEPS org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5,org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.5
//DEPS dev.zieger.utils:core:2.2.5,dev.zieger.utils:jdk:2.2.5

@file:Suppress("UNREACHABLE_CODE", "PropertyName")

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

