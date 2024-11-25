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
    private var _config = defaultConfig

    val config: InitializeConfig
        get() = _config

    fun addTask(
        task: InitializeTask,
        last: Boolean = false,
    ) {
        allTasks.add(task)
        if (last) {
            allTasks.forEach {
                addTaskToGraph(it)
            }
        }
    }

    fun init(
        context: AndroidContext,
        config: InitializeConfig = defaultConfig,
    ) {
        _config = config
        val taskScheduler =
            InitializeTaskScheduler(
                context,
                config,
                taskGraphBuilder.build(config.isDebug),
            )
        taskScheduler.schedule(allTasks)
    }

    private fun addTaskToGraph(task: InitializeTask) {
        if (task.dependencies.isEmpty()) {
            taskGraphBuilder.addNode(task)
        } else {
            task.dependencies.forEach { dependencyQualifiedName ->
                val dependencyTask =
                    allTasks.find {
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

interface InitializeCallback {
    fun onInitializationStart(firstTask: InitializeTask)

    fun onTaskStart(task: InitializeTask)

    fun onTaskComplete(
        task: InitializeTask,
        timeConsuming: Long,
    )

    fun onInitializationComplete(
        lastTask: InitializeTask,
        totalTimeConsuming: Long,
    )
}

abstract class InitializeTask(val initializer: Initializer) {
    val dependencies: Array<String>
        get() = _dependencies.toTypedArray()
    private val _dependencies = mutableListOf<String>()

    abstract suspend fun initialize(context: InitializeContext)

    fun addDependency(qualifiedName: String) {
        _dependencies.add(qualifiedName)
    }

    /**
     * 标记当前 task 运行在哪个进程
     */
    abstract val runProcessScope: RunProcessScope

    suspend fun execute(context: InitializeContext) {
        when (runProcessScope) {
            RunProcessScope.mainProcess -> {
                if (initializer.config.isMainProcess()) {
                    initialize(context)
                }
            }

            RunProcessScope.subProcess -> {
                if (!initializer.config.isMainProcess()) {
                    initialize(context)
                }
            }

            RunProcessScope.all -> {
                initialize(context)
            }
        }
    }
}

enum class RunProcessScope {
    mainProcess,
    subProcess,
    all,
}

abstract class InitializeConfig(
    val callback: InitializeCallback? = null,
    val isDebug: Boolean = false,
    val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    abstract fun isMainProcess(): Boolean
}

val defaultConfig =
    object : InitializeConfig() {
        override fun isMainProcess(): Boolean {
            return true
        }
    }
