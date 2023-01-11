import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    id("org.cadixdev.licenser") version "0.6.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val KT_VER = "1.8.0"

group = "me.dkim19375"
version = "1.0.2"

val basePackage = "me.dkim19375.${project.name.toLowerCase()}.libs"
val fileName = tasks.shadowJar.get().archiveFileName.get()

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://libraries.minecraft.net/")
    maven("https://repo.triumphteam.dev/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/releases/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")

    compileOnly(fileTree("libs"))

    implementation("io.github.dkim19375:dkim-bukkit-core:3.3.45") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KT_VER") {
        exclude(module = "annotations")
    }
}

license {
    header.set(resources.text.fromFile(rootProject.file("HEADER")))
    include("**/*.kt")
}

val shadowJar = tasks.shadowJar.get()

val removeBuildJars by tasks.registering {
    doLast {
        File(project.rootDir, "build/libs").deleteRecursively()
    }
}

val server = "1.18"
val servers = setOf(
    "1.8",
    "1.16",
    "1.17",
    "1.18"
)

val copyFile by tasks.registering {
    doLast {
        val jar = shadowJar.archiveFile.get().asFile
        val pluginFolder = file(rootDir).resolve("../.TestServers/${server}/plugins")
        if (pluginFolder.exists()) {
            jar.copyTo(File(pluginFolder, shadowJar.archiveFileName.get()), true)
        }
    }
}

val deleteAll by tasks.registering {
    doLast {
        for (deleteServer in servers) {
            for (file in File("../.TestServers/${deleteServer}/plugins").listFiles() ?: emptyArray()) {
                if (file.name.startsWith(shadowJar.archiveBaseName.get())) {
                    file.delete()
                }
            }
        }
    }
}

tasks.processResources {
    outputs.upToDateWhen { false }
    expand("pluginVersion" to project.version)
}

val relocations = setOf(
    "kotlin",
    "kotlinx",
    "me.dkim19375.dkimcore",
    "org.jetbrains.annotations",
    "me.dkim19375.dkimbukkitcore",
    "org.intellij.lang.annotations",
)

tasks.shadowJar {
    relocations.forEach { name ->
        relocate(name, "${basePackage}.$name")
    }
    exclude("DebugProbesKt.bin", "**/**.kotlin_builtins")
    mergeServiceFiles()
    // finalizedBy(tasks.getByName("copyFile"))
}