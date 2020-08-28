#!/usr/bin/env kscript

import java.io.*
import java.nio.charset.*
import java.util.*

val gradleProperties = Properties().apply { load(File("gradle.properties").readText().reader()) }

fun File.updateText(save: Boolean = true, charset: Charset = Charsets.UTF_8, callback: (String) -> String) {
    val oldContent = readText()
    val newContent = callback(oldContent)
    if (save) {
        writeText(newContent)
    }
}

fun updatePomXml(pomFile: File) {
    println("Processing $pomFile...")
    pomFile.updateText(save = true) { oldContent ->
        oldContent.replace(Regex("""<(\w+?)\.version>(.*?)</\1\.version>""")) {
            val kind = it.groupValues[1]
            val oldVersion = it.groupValues[2]
            val newVersion = gradleProperties["${kind}_version"] ?: oldVersion
            if (oldVersion != newVersion) {
                println("  - Updating... $kind: '$oldVersion' -> '$newVersion'")
            }
            "<$kind.version>$newVersion</$kind.version>"
        }
    }
}

updatePomXml(File("other/maven-netty/pom.xml"))
updatePomXml(File("other/maven-google-appengine-standard/pom.xml"))
