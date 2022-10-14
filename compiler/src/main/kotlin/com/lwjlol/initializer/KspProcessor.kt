package com.lwjlol.initializer

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import java.io.IOException
import java.io.OutputStream


/**
 * @author luwenjie on 2022/2/21 20:21:50
 */
class KspProcessor(
    val codeGenerator: CodeGenerator,
    val options: Map<String, String>,
    val logger: KSPLogger
) : SymbolProcessor {
    var invoked = false
    var logFile: OutputStream? = null


    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        allTasks.clear()
        if (LOG && !invoked) {
            logFile = codeGenerator.createNewFile(
                Dependencies(false),
                "report/",
                TAG,
                "txt"
            )
        }
        logFile.emit("$TAG: init($options)", "")
        val initialization = Initialization::class.qualifiedName ?: ""
        logFile.emit("$TAG: $initialization", "initializationClassName:")

        val initializationSymbols =
            resolver.getSymbolsWithAnnotation(Initialization::class.qualifiedName ?: "")
        val taskSymbols =
            resolver.getSymbolsWithAnnotation(Task::class.qualifiedName ?: "")

        val ret = initializationSymbols.filter { !it.validate() }
            .toList() + taskSymbols.filter { !it.validate() }.toList()
        logFile.emit("$TAG: process() ${initializationSymbols.count()} initializationSymbols", "")
        logFile.emit("$TAG: process() ${taskSymbols.count()} taskSymbols", "")
        logFile.emit("$TAG: process() ${resolver.getAllFiles().count()} AllFiles", "")
        val taskVisitor = TaskVisitor(logFile)
        val initVisitor = InitVisitor(logFile, codeGenerator)
        taskSymbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach {
                it.accept(taskVisitor, Unit)
            }
        initializationSymbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach {
                it.accept(initVisitor, Unit)
            }
        invoked = true
        return ret
    }


    override fun finish() {
        super.finish()
        allTasks.clear()
        if (LOG) {
            logFile?.close()
        }
    }
}

class KspProvider : SymbolProcessorProvider {

    companion object {
    }

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KspProcessor(environment.codeGenerator, environment.options, environment.logger)
    }
}

const val LOG = false
private const val TAG = "kuril_initializer"

// map[task] = dependency
val allTasks: MutableMap<String, List<KSType>> = mutableMapOf()

fun OutputStream.appendText(str: String) {
    try {
        this.write(str.toByteArray())
    } catch (e: IOException) {
    }
}
