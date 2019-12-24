package de.gapps.utils.misc

import de.gapps.utils.time.duration.IDurationEx

interface AntiSpamProxy {

    val updateIntervalDuration: IDurationEx

    fun execute(block: () -> Unit)


}