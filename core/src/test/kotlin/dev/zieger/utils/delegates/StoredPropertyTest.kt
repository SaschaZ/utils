package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.delegates.store.IStoreContext
import dev.zieger.utils.delegates.store.StoreContext
import dev.zieger.utils.delegates.store.StoredProperty
import dev.zieger.utils.misc.name
import io.kotlintest.specs.AbstractAnnotationSpec.Test

class StoredPropertyTest : IStoreContext by StoreContext(StoredPropertyTest::class.name) {

    private var testProperty: Int by StoredProperty(0)
    private var testProperty2: Int by StoredProperty(0, key = "testProperty")

    @Test
    fun testStoredProperty() = runTest {
        testProperty = 1
        testProperty assert 1

        testProperty = 2
        testProperty assert 2

        testProperty2 assert 2

        testProperty2 = 3
        testProperty assert 3
        testProperty2 assert 3
    }
}