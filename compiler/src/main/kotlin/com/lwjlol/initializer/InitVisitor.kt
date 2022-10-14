package com.lwjlol.initializer

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.OutputStream

class InitVisitor(
    val log: OutputStream?, val codeGenerator: CodeGenerator,
) : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        log.emit(classDeclaration.classKind.type, "visitClassDeclaration class type: ")
        classDeclaration.annotations.forEach {
            it.accept(this, Unit)
            log.emit(
                "${it.annotationType.resolve().declaration.qualifiedName?.asString()}",
                "visitClassDeclaration:"
            )
        }
        val className = classDeclaration.simpleName.getShortName()
        val parameters = classDeclaration.getConstructors().maxByOrNull {
            it.parameters.size
        }?.parameters ?: return


        classDeclaration.annotations.filter {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == Initialization::class.qualifiedName
        }.forEach { annotation ->
            val argsMap = mutableMapOf<String, Any?>()
            annotation.arguments.forEach {
                argsMap[it.name?.asString() ?: ""] = it.value
                log.emit(
                    "name:${it.name?.asString()},value:${it.value?.toString()},spread:${it.isSpread}",
                    "$className:${annotation.shortName.asString()}  "
                )
            }

            generateClass(
                annotations = classDeclaration.annotations,
                className = className,
                packageName = classDeclaration.packageName.asString(),
                args = argsMap,
                source = classDeclaration.containingFile ?: return@forEach
            )
        }
    }

    private fun generateClass(
        annotations: Sequence<KSAnnotation>,
        className: String,
        packageName: String,
        args: Map<String, Any?>,
        source: KSFile
    ) {

        log.emit(className)
        log.emit(packageName)
        @Suppress("UNCHECKED_CAST")
        val tasks = args["tasks"] as ArrayList<KSType>

        // addTask(xxTask())
        val addTaskStatements = tasks.mapIndexed { index, task ->
            val setDependenciesStatement =
                (allTasks[task.declaration.qualifiedName?.asString()]
                    ?: emptyList()).mapIndexed { _, depTask ->
                    "addDependency(\"${depTask.declaration.qualifiedName?.asString()}\")"
                }.joinToString("\n")
            val taskQualifiedName = task.declaration.qualifiedName?.asString() ?: ""
            "addTask($taskQualifiedName().apply {\n$setDependenciesStatement}, ${index == tasks.lastIndex})"
        }

        val fileName = "${className}Imp"
        val fileBuilder = TypeSpec.classBuilder(fileName)
            .addKdoc("this class is generated by kuril_initializer for [$packageName.$className], Please don't modify it!")
            .superclass(ClassName(packageName, className))
            .addInitializerBlock(CodeBlock.builder().apply {
                addTaskStatements.forEach {
                    addStatement(it)
                }
            }.build())
        // write file
        val file = FileSpec.builder(packageName, fileName).addType(fileBuilder.build()).build()
        file.writeTo(codeGenerator = codeGenerator, dependencies = Dependencies(false, source))
    }

}