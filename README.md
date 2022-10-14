## initializr

use ksp annotation organize the order of tasks.

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

## proguard

```java
-keep class *extends com.lwjlol.initializer.InitializeTask
        -keep class *extends com.lwjlol.initializer.Initializer
```

## thanks

https://github.com/wurensen/TaskScheduler
