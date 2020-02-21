# utilsEx
Kotlin Jvm Utilities 
[![](https://jitpack.io/v/SaschaZ/utils.svg)](https://jitpack.io/#SaschaZ/utils/core)

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
[![](https://jitpack.io/v/SaschaZ/utils.svg)](https://jitpack.io/#SaschaZ/utils/core)
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

#

## CoroutineEx

#

## PipelineEx

#

## Misc. Kotlin Extensions