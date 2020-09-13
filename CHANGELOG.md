# Changelog

##### 2.3.0

* `OnChanged`, `Observable`:
  * renamed `OnChanged2` to `OnChangedWithParent` and `Observable2` to `ObservableWithParent`
  * thread safety:
    * added `changeValue` method: 
      * allows thread safe change of the properties value
      * if you are also changing the properties value with the `value` property, than you should enable `safeSet` or thread safety is not guaranteed
    * added `safeSet` parameter: 
      * if set to `true`, setting the properties value will be done inside a `Coroutine` with the same `Mutex` used in `changeValue`
      * because of that the properties new value is not immediately available after setting it when `safeSet` is set to `true`:
        ```kotlin
        val test by OnChanged(0, safeSet = true)
        val test = 1
        test == 1 // Not necessarily true
        ```
      * defaulting to `false`
* `MachineEx`:
  * fixed wrong `MatchScope` for state conditions
* `Log` v2:
  * enhanced version of the `Log` utils
  * support for log scopes
  * available under `dev.zieger.utils.log2` (former `Log` utils still remain in the library)
* `FiFo`:
  * reimplemented
  * added `take` method to remove the first/oldest element
  * added `DurationExFiFo`:
    * accepts values of type `ITimeEx`
    * size is limited by the duration of the oldest to the latest item
* `catch`, `asyncEx`, `launchEx`, `withCoroutineEx`:
  * added possibility to define `Throwable`s to `include` in or `exclude` from catching
* `assert` v2:
  * enhanced version of the `assert` utils
  * supports multiple assertions (nullability, greater, smaller, ...)
  * available under `dev.zieger.utils.core-testing.assertion2` (former `assert` utils remain in the library)

##### 2.2.23

* build fix

##### 2.2.22

* Continuation:
    * allow multiple `suspendUntilTrigger` calls for the same trigger event
    * made coroutine property nullable
    * never launch a coroutine for unsuspended trigger call when no coroutine was defined

##### 2.2.21

* build tools fix

##### 2.2.20

* removed `Controllable(2)` and made `value` of `OnChangedScope` writeable
* replaced `OnChanged` and `Observable` classes with typealiases
* some minor fixes to the Json helper

##### 2.2.19

* added `MatchScope` to `MachineEx` (allows accessing event, states and previous changes in external condition)
* added `TypeContinuation` (same as `Continuation` except it allows sending a value over the trigger event that can be 
received from the suspender)

##### 2.2.18

* added GKoin
    * manages multiple Koin instances inside one process
    * provides global calls for `startKoin`, `get`, `inject` and `stopKoin`
    * all calls have a `key` parameter to differentiate between multiple Koin instances
* added `CHANGELOG.md`
