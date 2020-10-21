package dev.zieger.utils.time

import dev.zieger.utils.time.duration.IDurationEx


suspend fun delay(time: IDurationEx) = kotlinx.coroutines.delay(time.millis)