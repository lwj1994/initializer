## initializer
![](https://maven-badges.herokuapp.com/maven-central/com.lwjlol.initializer/core/badge.svg?style=for-the-badge)

use ksp annotation organize the order of tasks.

## usage

add gradle

```groovy
XX {
    repositories {
        mavenCentral()
    }
}


dependencies {
    implementation 'com.lwjlol.initializer:core:0.1.0'
    ksp 'com.lwjlol.initializer:compiler:0.1.0'
}
```

```kotlin
@Initialization(
    tasks = [
        Task1::class,
        Task2::class,
        Task3::class,
        Task4::class,
    ]
)
abstract class AppInitializer : Initializer()

class Task1 : TestTask("Task1")

@Task(dependencies = [Task1::class])
class Task2 : TestTask("Task2")

@Task(dependencies = [Task2::class])
class Task3 : TestTask("Task3")

@Task(dependencies = [Task3::class])
class Task4 : TestTask("Task4")
```

then execute init:

```kotlin
val initializer = Initializer.Builder(AppInitializer::class.java).build()
initializer.init(this, debug = true, callback = object : Callback {
    override fun onInitializationStart(firstTask: InitializeTask) {

    }

    override fun onTaskStart(task: InitializeTask) {

    }

    override fun onTaskComplete(task: InitializeTask, timeConsuming: Long) {

    }

    override fun onInitializationComplete(
        lastTask: InitializeTask,
        totalTimeConsuming: Long
    ) {

    }

})
```

## proguard

```java
-keep class *extends com.lwjlol.initializer.InitializeTask
        -keep class *extends com.lwjlol.initializer.Initializer
```

## thanks

* https://github.com/wurensen/TaskScheduler
* https://github.com/johnsonlee/initializr
