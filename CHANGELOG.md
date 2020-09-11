# Changelog

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
