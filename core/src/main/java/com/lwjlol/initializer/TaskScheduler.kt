package com.lwjlol.initializer

import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis
import android.content.Context as AndroidContext

/**
 *
 * 依赖任务调度器
 *
 * 可以实现异步任务按照依赖关系进行调度执行，常见使用场景：
 * - 执行任务一开始就已经准备就绪，依赖关系构造后可以直接调度执行，如启动任务依赖执行
 * - 执行任务一开始未准备就绪，只能先构造依赖关系，执行任务准备完毕后根据依赖关系来决定是否调度还是继续等待
 */
internal class InitializeTaskScheduler(
    val appContext: AndroidContext,
    val config: InitializeConfig,
    val taskGraph: DirectedAcyclicGraph<InitializeTask>,
) {
    private val mutexTaskInitializedCounter = Mutex()
    private var taskInitializedCounter = 0
    private val context =
        InitializeContext(appContext = appContext, coroutineScope = config.coroutineScope)
    private var initialized = false

    /**
     * 任务状态
     */
    private val taskStates: MutableMap<InitializeTask, State>
    private val allTasks = mutableListOf<InitializeTask>()
    private var startTime = 0L

    init {
        taskStates =
            taskGraph.graph.mapValuesTo(mutableMapOf()) {
                State.INIT
            }
    }

    /**
     * 获取当前任务状态
     * @param InitializeTask InitializeTask
     * @return State
     */
    private fun getInitializeTaskState(task: InitializeTask): State {
        return taskStates[task]!!
    }

    fun schedule(tasks: List<InitializeTask>) {
        startTime = System.currentTimeMillis()
        allTasks.clear()
        allTasks.addAll(tasks)
        tasks.forEach {
            schedule(it)
        }
    }

    /**
     * 调度任务
     * @param InitializeTask InitializeTask
     */
    private fun schedule(task: InitializeTask): InitializeTaskScheduler {
        // 统一在主线程调度，防止多线程问题
        context.coroutineScope.launch {
            val state = taskStates[task]
            if (state == State.INIT) {
                nextState(task)
                scheduleCurrent(task)
            }
        }
        return this
    }

    private fun scheduleCurrent(task: InitializeTask) {
        context.coroutineScope.launch {
            val state = taskStates[task]
            // 准备就绪的任务才参与调度
            if (state == State.PREPARED && isIncomingFinished(task)) {
                nextState(task)
                if (taskInitializedCounter == 0 && !initialized) {
                    initialized = true
                    config.callback?.onInitializationStart(task)
                }
                config.callback?.onTaskStart(task)
                measureTimeMillis {
                    task.execute(context)
                }.let {
                    config.callback?.onTaskComplete(task, it)
                }
                mutexTaskInitializedCounter.withLock {
                    taskInitializedCounter++
                }
                if (taskInitializedCounter == allTasks.size) {
                    config.callback?.onInitializationComplete(
                        task,
                        System.currentTimeMillis() - startTime,
                    )
                }
                nextState(task)
                scheduleNext(task)
            }
        }
    }

    private fun scheduleNext(task: InitializeTask) {
        taskGraph.getOutgoingNodes(task)?.forEach {
            scheduleCurrent(it)
        }
    }

    private fun nextState(task: InitializeTask) {
        taskStates[task] = getInitializeTaskState(task).next()
    }

    private fun isIncomingFinished(task: InitializeTask): Boolean {
        return taskGraph.getIncomingNodes(task)
            ?.all { taskStates[it] == State.FINISHED } ?: true
    }

    /**
     * 任务状态，枚举顺序为状态顺序
     */
    enum class State {
        /**
         * 初始状态
         */
        INIT,

        /**
         * 准备就绪
         */
        PREPARED,

        /**
         * 已被调度
         */
        SCHEDULED,

        /**
         * 完成
         */
        FINISHED,

        ;

        /**
         * 进入下一个状态
         * @return State
         */
        fun next(): State {
            val values = values()
            val nextOrdinal = (this.ordinal + 1) % values.size
            return values[nextOrdinal]
        }
    }
}
