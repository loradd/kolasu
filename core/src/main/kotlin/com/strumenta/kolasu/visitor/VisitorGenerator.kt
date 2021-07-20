package com.strumenta.kolasu.visitor

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

enum class Language {
    JAVA,
    KOTLIN
}

class VisitorGenerator : CliktCommand() {
    val language: Language by option(help = "Language to use for generation").enum<Language>().default(Language.KOTLIN)
    val rootNode: String by argument(help = "Root node class")
    val packageName: String by argument(help = "Package name to generate code")
    val srcDir: File by argument(help = "Source dir where to generate code")
        .file(canBeFile = false, canBeDir = true, mustBeWritable = true, mustExist = true)

    override fun run() {

    }
}


fun main(args: Array<String>) {
    VisitorGenerator().main(args)
}