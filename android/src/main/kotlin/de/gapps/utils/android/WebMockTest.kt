package de.gapps.utils.android

import android.app.Activity
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnitRunner
import de.gapps.utils.testing.IWebMock
import org.junit.Rule
import kotlin.reflect.KClass

inline fun <reified A : Activity> webMockTest(webMock: IWebMock) = WebMockTest(webMock, A::class)

@SmallTest
open class WebMockTest<A : Activity>(
    private val webMock: IWebMock,
    clazz: KClass<A>,
    @get:Rule
    var rule: ActivityTestRule<A> = ActivityTestRule(clazz.java)
) : AndroidJUnitRunner(), IWebMock by webMock