package com.lwjlol.initializer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import android.content.Context as AndroidContext

/**
 * @author luwenjie on 2022/10/10 16:23:14
 */
abstract class Initializer {
    private val allTasks: MutableList<InitializeTask> = mutableListOf()
    private val taskGraphBuilder = DirectedAcyclicGraph.Builder<InitializeTask>()
    val defaultScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun addTask(task: InitializeTask, last: Boolean = false) {
        allTasks.add(task)
        if (last) {
            allTasks.forEach {
                addTaskToGraph(it)
            }
        }
    }


    inline fun init(
        context: AndroidContext,
        scope: CoroutineScope = defaultScope,
        debug: Boolean = false,
        crossinline onInitializationStart: (firstTask: InitializeTask) -> Unit = {},
        crossinline onTaskStart: (task: InitializeTask) -> Unit = {},
        crossinline onTaskComplete: (task: InitializeTask, timeConsuming: Long) -> Unit = { _, _ -> },
        crossinline onInitializationComplete: (
            lastTask: InitializeTask,
            totalTimeConsuming: Long
        ) -> Unit = { _, _ ->

        },
    ) {
        init(context, scope, debug, object : Callback {
            override fun onInitializationStart(firstTask: InitializeTask) {
                onInitializationStart(firstTask)
            }

            override fun onTaskStart(task: InitializeTask) {
                onTaskStart(task)
            }

            override fun onTaskComplete(task: InitializeTask, timeConsuming: Long) {
                onTaskComplete(task, timeConsuming)
            }

            override fun onInitializationComplete(
                lastTask: InitializeTask,
                totalTimeConsuming: Long
            ) {
                onInitializationComplete(lastTask, totalTimeConsuming)
            }

        })
    }


    fun init(
        context: AndroidContext,
        scope: CoroutineScope = defaultScope,
        debug: Boolean = false,
        callback: Callback? = null
    ) {
        val taskScheduler = InitializeTaskScheduler(
            context,
            scope,
            taskGraphBuilder.build(debug),
            callback = callback
        )
        taskScheduler.schedule(allTasks)
    }

    private fun addTaskToGraph(task: InitializeTask) {
        if (task.dependencies.isEmpty()) {
            taskGraphBuilder.addNode(task)
        } else {
            task.dependencies.forEach { dependencyQualifiedName ->
                val dependencyTask = allTasks.find {
                    it::class.qualifiedName == dependencyQualifiedName
                } ?: return@forEach
                taskGraphBuilder.addEdge(dependencyTask, task)
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    class Builder<T>(val clazz: Class<T>) {
        fun build(): T {
            return Class.forName(clazz.canonicalName + "Imp").newInstance() as T
        }
    }
}


interface Callback {
    fun onInitializationStart(firstTask: InitializeTask)

    fun onTaskStart(task: InitializeTask)

    fun onTaskComplete(task: InitializeTask, timeConsuming: Long)

    fun onInitializationComplete(lastTask: InitializeTask, totalTimeConsuming: Long)
}


abstract class InitializeTask {
    val dependencies: Array<String>
        get() = _dependencies.toTypedArray()
    private val _dependencies = mutableListOf<String>()

    abstract suspend fun initialize(context: InitializeContext)

    fun addDependency(qualifiedName: String) {
        _dependencies.add(qualifiedName)
    }
}
