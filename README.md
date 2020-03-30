# utilsEx
Kotlin Jvm Utilities 
[![](https://jitpack.io/v/SaschaZ/utils.svg)](https://jitpack.io/#SaschaZ/utils/core) ![](https://github.com/SaschaZ/utils/workflows/Android%20CI/badge.svg)

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
    // core contains all jdk utils (compatible with android and jdk)
    implementation "dev.zieger.utils:core:$utilsVersion"

    // android only utils
    implementation "dev.zieger.utils:android:$utilsVersion"

    // testing utils (jdk + android)
    implementation "dev.zieger.utils:testing:$utilsVersion"

    // jdk only utils
    implementation "dev.zieger.utils:console:$utilsVersion"
}
```

Where `utilsVersion` is the latest from
[![](https://jitpack.io/v/SaschaZ/utils.svg)](https://jitpack.io/#SaschaZ/utils/core) ![](https://github.com/SaschaZ/utils/workflows/Android%20CI/badge.svg)
#

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
  eventData<TestEventData>().foo onFail "foo String is not \"foo\"" assert "foo"
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