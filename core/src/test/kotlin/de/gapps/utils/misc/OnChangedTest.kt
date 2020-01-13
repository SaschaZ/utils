package de.gapps.utils.misc

import de.gapps.utils.delegates.OnChanged
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking

class OnChangedTest : AnnotationSpec() {

    private var toTestVar: Int by OnChanged(0) { new ->
        toTestOnChangeOldVar = this
        toTestOnChangeNewVar = new
    }

    private var toTestOnChangeOldVar: Int? = null
    private var toTestOnChangeNewVar: Int? = null

    @Test
    fun testIt() = runBlocking {
        assert(toTestVar == 0)
        assert(toTestOnChangeOldVar == null)
        assert(toTestOnChangeNewVar == null)

        toTestVar = 0
        assert(toTestVar == 0)
        assert(toTestOnChangeOldVar == null)
        assert(toTestOnChangeNewVar == null)

        toTestVar = 1
        assert(toTestVar == 1)
        assert(toTestOnChangeOldVar == 0)
        assert(toTestOnChangeNewVar == 1)

        toTestVar = 2
        assert(toTestVar == 2)
        assert(toTestOnChangeOldVar == 1)
        assert(toTestOnChangeNewVar == 2)
    }.asUnit()
}