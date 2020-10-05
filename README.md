# utilsEx
#### Kotlin Jvm Utilities

[![jitPack](https://jitpack.io/v/dev.zieger/utils.svg)](https://jitpack.io/#dev.zieger/utils/core) 
[![jitCI](https://jitci.com/gh/SaschaZ/utils/svg)](https://jitci.com/gh/SaschaZ/utils) 
[![Android CI](https://github.com/SaschaZ/utils/workflows/Android%20CI/badge.svg)](https://github.com/SaschaZ/utils/actions?query=workflow%3A%22Android+CI%22)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue?logo=Apache)](https://raw.githubusercontent.com/SaschaZ/utils/master/LICENSE)
<br>
[![codecov](https://codecov.io/gh/SaschaZ/utils/branch/master/graph/badge.svg)](https://codecov.io/gh/SaschaZ/utils)
[![codebeat badge](https://codebeat.co/badges/84495349-71cb-461f-9840-860e9678f593)](https://codebeat.co/projects/github-com-saschaz-utils-master)
[![Maintainability](https://api.codeclimate.com/v1/badges/cbd0727a672b2178f75e/maintainability)](https://codeclimate.com/github/SaschaZ/utils/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/cbd0727a672b2178f75e/test_coverage)](https://codeclimate.com/github/SaschaZ/utils/test_coverage)
[![Codacy](https://api.codacy.com/project/badge/Grade/13ba69ac93234d2da6a3b55474a4fcc7)](https://www.codacy.com/manual/SaschaZ/utils?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=SaschaZ/utils&amp;utm_campaign=Badge_Grade)


## Contains utilities for
- [utilsEx](#utilsex)
  - [Get started](#get-started)
  - [TimeEx, DurationEx](#timeex-durationex)
  - [Observable, Controllable](#observable-controllable)
  - [MachineEx](#machineex)
  - [CoroutineEx](#coroutineex)
  - [PipelineEx](#pipelineex)
  - [Misc. Kotlin Extensions](#misc-kotlin-extensions)

<br>

## Get started
```gradle
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    // utils
    implementation "dev.zieger.utils:core:2.2.44"// platform independent
    implementation "dev.zieger.utils:android:2.2.44" // android
    implementation "dev.zieger.utils:jdk:2.2.44" // jdk

    // testing utils
    implementation "dev.zieger.utils:core-testing:2.2.44" // platform independent
    implementation "dev.zieger.utils:android-testing:2.2.44" // android
    implementation "dev.zieger.utils:jdk-testing:2.2.44" // jdk
}
```

## TimeEx, DurationEx

```kotlin
val firstTime = TimeEx() - 5.years
println("$firstTime") // -> 28.01.2015-19:50:13

val secondTime = TimeEx() - 5.years * 10 / 3
println("$secondTime") // -> 02.06.2003-12:50:14

val difference = firstTime - secondTime
println("$difference") // -> 11Y 8M 3D 7H 59MIN 59S 623MS

println("${10.minutes * 6}") // -> 1H
```
#

## Observable, Controllable

#

## MachineEx


State machine with the following features:
- maps actions to incoming events (event condition) and state changes (state condition)
- events and states can contain dynamic data instances that can be referenced in any condition
- DSL for defining event and state conditions
- full Coroutine support


#### **Usage**

First you need to define the events, states and data classes that should be used in the state machine:
Because event and state instances are not allowed to change it makes sense to use objects for them. Any dynamic data
can be attached with a `Data` class.

#### **Define `Event`s, `State`s and `Data`**

States need to implement the `State` class. :
```kotlin
sealed class TestState : State() {

 object INITIAL : TestState()
 object A : TestState()
 object B : TestState()
 object C : TestState()
 object D : TestState()
}
```

Events need to implement the `Event` class:
```
sealed class TestEvent(ignoreData: Boolean = false) : Event(ignoreData) {

 object FIRST : TestEvent()
 object SECOND : TestEvent(true)
 object THIRD : TestEvent()
 object FOURTH : TestEvent()
}
```
Data classes need to implement the `Data` class:
```
sealed class TestData : Data() {

 data class TestEventData(val foo: String) : TestData()
 data class TestStateData(val moo: Boolean) : TestData()
}
```

#### **`Event` conditions**

After you have defined the needed events, states and possible data you can start defining the event and state
conditions:
```kotlin
MachineEx(INITIAL) {
  // All conditions start with a - (minus) sign.
  // If the first parameter is an event the condition becomes an event condition.
  // If the first parameter is a state the condition becomes a state condition.
  // Some examples for event conditions:

  // When the current `State` is `INITIAL` and the `FIRST` event is received change the `State` to `A`.
  +FIRST + INITIAL set A

  // When the `SECOND` `Event` is received change `State` to `B`.
  +SECOND set B

  // When the current `State` is `A` or `B` and the `FIRST`, `SECOND` or `THIRD` `Event` is received change the
  // `State` to `C`.
  +FIRST + SECOND + THIRD + A + B set C

  // When the current `State` is `C` and the {THIRD] `Event` is received execute the provided lambda and set the
  // new `State` to `D`-
  +THIRD + C execAndSet {
    // do something
    D // return the new state that should be activated for the provided event condition.
  }
}
```

You can see that event conditions must contain at least one `Event` and optional `State`(s).
Because there can only be one incoming `Event` and one existing `State` only one `Event` or `State` of the
condition need to be equal. BUT keep in mind when providing no optional `State`(s) the event condition will
react on all `State`s.

#### **`State` conditions**

To react on `State`s when they get activated by an event condition you can use state conditions:

```kotlin
// When `C` gets activated execute the provided lambda.
+C exec {
  // do something
} // `State` conditions can not set a new `State`.

// When `C` gets activated with the incoming `Event` `FIRST` execute the provided lambda.
+C + FIRST exec {
  // do something
}
```

Summarize difference between `Event` and `State` conditions:
- First parameter of an `Event` condition must be an `Event` -> First parameter of a `State` condition must be
  a `State`.
- `Event` conditions are triggered on incoming `Event`s -> `State` conditions are triggered by the any state
  that gets activated by an `Event` condition
- `State` conditions can not activate a new `State`. One `Event` condition per incoming `Event` can activate a
  new `State`.
- `Event` conditions match against all `State`s when the condition does not contain `State` parameter ->
  `State` conditions match against all `Event`s when the condition does not contain an `Event` parameter

#### **`Data`**

`Data` instances can be attached to any `Event` or `State` with the * operator.
Attached `Data` is than included when matching conditions.
```kotlin
// When `State` is `C` and the incoming `Event` of `THIRD` has attached `TestEventData` with the content
"foo" the `State` `D` gets activated.
+THIRD TestEventData("foo") + C execAndSet {
  eventData<TestEventData>().foo assert "foo" % "foo String is not \"foo\""
  D
}
```
There are several methods and properties to access current and previous `Event`s, `State`s and `Data`.
See `ExecutorScope` for more details.

#### **Exclude**

You can also exclude specific `Event`(s) or `State`s that must not be matching.
Also works with attached `Data`.
```kotlin
// When `State` `C` gets activated not by `Event` `FIRST` execute provided lambda.
+C - FIRST exec {
  // do something
}
```

##### **Previous `State` changes**
##### **Applying `Event`s to the state machine**
##### **Benefits of using operators for defining conditions**
##### **Coroutine support**


## CoroutineEx

#

## PipelineEx

#

## Misc. Kotlin Extensions

#

# Changelog

##### 2.2.43

* multiple non released version because of JitPack foo
* fix for `suspendUntil` in `OnChanged` and `Observable`
* changed the name of `OnChanged2` to `OnChangedWithParent` (same for `ObservableWithParent`) 

##### 2.2.38

*  FiFo crash fix (add new value before removing old ones)

##### 2.2.37

* TypeContinuation crash fix

##### 2.2.36

* fixed build

##### 2.2.35

* added StateMachine observable
* `runCommand` fix

##### 2.2.34

* fixed FlakyTest exception output
* added LanternaConsole (WIP)

##### 2.2.33

* fixed MachineEx log output when state was changed

##### 2.2.32

* fix JitPack foo

##### 2.2.31

* OnChanged:
    * `suspendUntil` returns wanted value

##### 2.2.30

* FlakyTest:
    * proper output of throwables

##### 2.2.29

* FlakyTest:
    * print all failed test causes at the end of each test

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
    * allow multiple `suspend` calls for the same trigger event
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
