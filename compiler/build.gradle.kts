plugins {
    kotlin("jvm")
}

apply(from = "${rootProject.projectDir}/plugin_maven_publish.gradle")

@Suppress("UNCHECKED_CAST")
dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:1.10.2")
    implementation("com.squareup:kotlinpoet-ksp:1.10.2")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.10-1.0.6")
    implementation(project(":core"))
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview"
}
