package com.lwjlol.initializer

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import java.io.OutputStream

/**
 * @author luwenjie on 2022/10/11 17:48:56
 */
class TaskVisitor(val log: OutputStream?) : KSVisitorVoid() {
    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit,
    ) {
        classDeclaration.annotations.forEach {
            it.accept(this, Unit)
        }
        val className = classDeclaration.simpleName.getShortName()
        val parameters =
            classDeclaration.getConstructors().maxByOrNull {
                it.parameters.size
            }?.parameters ?: return
        classDeclaration.annotations.filter {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == Task::class.qualifiedName
        }.forEach { annotation ->
            val argsMap = mutableMapOf<String, Any?>()
            annotation.arguments.forEach {
                argsMap[it.name?.asString() ?: ""] = it.value
                log.emit(
                    "name:${it.name?.asString()},value:${it.value?.toString()},spread:${it.isSpread}",
                    "$className:${annotation.shortName.asString()}  ",
                )
            }
            @Suppress("UNCHECKED_CAST")
            allTasks[classDeclaration.qualifiedName?.asString() ?: ""] =
                argsMap["dependencies"] as List<KSType>
        }
    }

    companion object {
        private const val TAG = "TaskVisitor"
    }
}
