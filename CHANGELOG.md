# Changelog

##### 2.2.28

* FlakyTest fix

##### 2.2.27

* added more log output to FlakyTest

##### 2.2.26

* fixed build

##### 2.2.25

* added FlakyTest
    * runs a flaky test again when it fails

##### 2.2.24

* upgraded to kotlin 1.4.10
    * Coroutines: 1.3.9
    * Koin: 2.2.0-beta-1
    * Moshi: 1.10.0

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
