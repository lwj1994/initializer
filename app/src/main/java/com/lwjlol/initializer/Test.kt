package com.lwjlol.initializer

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
    ],
)
abstract class AInitializer : Initializer()

@Initialization(
    tasks = [
        Task5::class,
        Task6::class,
        Task7::class,
        Task8::class,
        Task9::class,
    ],
)
abstract class BInitializer : Initializer()

class Task1(initializer: Initializer) : TestTask("Task1", initializer) {
    override val runProcessScope: RunProcessScope
        get() = RunProcessScope.mainProcess
}

@Task(dependencies = [Task1::class])
class Task2(initializer: Initializer) : TestTask("Task2", initializer) {
    override val runProcessScope: RunProcessScope
        get() = RunProcessScope.mainProcess
}

@Task(dependencies = [Task2::class])
class Task3(initializer: Initializer) : TestTask("Task3", initializer) {
    override val runProcessScope: RunProcessScope
        get() = RunProcessScope.mainProcess
}

@Task(dependencies = [Task3::class])
class Task4(initializer: Initializer) : TestTask("Task4", initializer) {
    override val runProcessScope: RunProcessScope
        get() = RunProcessScope.mainProcess
}

class Task5(initializer: Initializer) : TestTask("Task5", initializer) {
    override val runProcessScope: RunProcessScope
        get() = RunProcessScope.mainProcess

    override suspend fun initialize(context: InitializeContext) {
        withContext(Dispatchers.Default) {
            delay(4000)
            println("$name executed completely")
        }
    }
}

class Task6(initializer: Initializer) : TestTask("Task6", initializer) {
    override val runProcessScope: RunProcessScope
        get() = RunProcessScope.mainProcess
}

@Task(dependencies = [Task5::class, Task6::class])
class Task7(initializer: Initializer) : TestTask("Task7", initializer) {
    override val runProcessScope: RunProcessScope
        get() = RunProcessScope.mainProcess
}

@Task(dependencies = [Task7::class])
class Task8(initializer: Initializer) : TestTask("Task8", initializer) {
    override val runProcessScope: RunProcessScope
        get() = RunProcessScope.mainProcess
}

@Task(dependencies = [Task6::class])
class Task9(initializer: Initializer) : TestTask("Task9", initializer) {
    override val runProcessScope: RunProcessScope
        get() = RunProcessScope.subProcess
}

abstract class TestTask(val name: String, initializer: Initializer) : InitializeTask(initializer) {
    override suspend fun initialize(context: InitializeContext) {
        withContext(Dispatchers.IO) {
            delay(1000)
            println("$name executed completely")
        }
    }
}
