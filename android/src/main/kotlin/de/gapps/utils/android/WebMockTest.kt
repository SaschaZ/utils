package de.gapps.utils.android

//inline fun <reified A : Activity> webMockTest(webMock: IWebMock) = WebMockTest(webMock, A::class)
//
//@SmallTest
//open class WebMockTest<A : Activity>(
//    private val webMock: IWebMock,
//    clazz: KClass<A>,
//    @get:Rule
//    var rule: ActivityTestRule<A> = ActivityTestRule(clazz.java)
//) : AndroidJUnitRunner(), IWebMock by webMock