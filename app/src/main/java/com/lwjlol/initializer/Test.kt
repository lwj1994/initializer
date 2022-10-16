package com.lwjlol.initializer

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object Test {
    val initializerA = Initializer.Builder(AInitializer::class.java).build()
    val initializerB = Initializer.Builder(BInitializer::class.java).build()

    private const val TAG = "Test"

}


@Initialization(
    tasks = [
        Task1::class,
        Task2::class,
        Task3::class,
        Task4::class,
    ]
)
abstract class AInitializer : Initializer()


@Initialization(
    tasks = [
        Task5::class,
        Task6::class,
        Task7::class,
        Task8::class,
        Task9::class,
    ]
)
abstract class BInitializer : Initializer()

class Task1 : TestTask("Task1")

@Task(dependencies = [Task1::class])
class Task2 : TestTask("Task2")

@Task(dependencies = [Task2::class])
class Task3 : TestTask("Task3")

@Task(dependencies = [Task3::class])
class Task4 : TestTask("Task4")

class Task5 : TestTask("Task5"){
    override suspend fun initialize(context: InitializeContext) {
        withContext(Dispatchers.Default) {
            delay(4000)
            println("$name executed completely")
        }
    }
}

class Task6 : TestTask("Task6")

@Task(dependencies = [Task5::class, Task6::class])
class Task7 : TestTask("Task7")

@Task(dependencies = [Task7::class])
class Task8 : TestTask("Task8")


@Task(dependencies = [Task6::class])
class Task9 : TestTask("Task9")

open class TestTask(val name: String) : InitializeTask() {
    override suspend fun initialize(context: InitializeContext) {
        withContext(Dispatchers.IO) {
            delay(1000)
            println("$name executed completely")
        }
    }
}


