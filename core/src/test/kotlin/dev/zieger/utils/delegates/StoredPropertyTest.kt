package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.delegates.store.IStoreContext
import dev.zieger.utils.delegates.store.StoreContext
import dev.zieger.utils.delegates.store.StoredProperty
import dev.zieger.utils.misc.name
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.Test

class StoredPropertyTest : IStoreContext by StoreContext(StoredPropertyTest::class.name) {

    private var testProperty: Int by StoredProperty(0, Int.serializer())

    @Test
    fun testStoredProperty() = runTest {
        testProperty = 1
        testProperty assert 1

        testProperty = 2
        testProperty assert 2
    }
}