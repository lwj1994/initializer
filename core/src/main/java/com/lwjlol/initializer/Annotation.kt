package com.lwjlol.initializer

import kotlin.reflect.KClass

/**
 * @author luwenjie on 2022/10/10 16:22:41
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Task(
    vararg val dependencies: KClass<out InitializeTask> = [],
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Initialization(
    vararg val tasks: KClass<out InitializeTask> = [],
)
