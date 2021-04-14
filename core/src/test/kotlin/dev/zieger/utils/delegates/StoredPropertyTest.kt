package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.delegates.store.IStoreContext
import dev.zieger.utils.delegates.store.StoreContext
import dev.zieger.utils.delegates.store.StoredProperty
import dev.zieger.utils.misc.name
import io.kotest.core.spec.style.FunSpec

class StoredPropertyTestContext :  IStoreContext by StoreContext(StoredPropertyTest::class.name) {

    var testProperty: Int by StoredProperty(0)
    var testProperty2: Int by StoredProperty(0, key = "testProperty")
}

class StoredPropertyTest : FunSpec({

    var ctx = StoredPropertyTestContext()

    beforeEach {
        ctx = StoredPropertyTestContext()
    }

    test("test store property") {
        ctx.run {
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
})