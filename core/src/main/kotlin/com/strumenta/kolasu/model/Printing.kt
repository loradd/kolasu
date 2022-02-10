package com.strumenta.kolasu.model

import java.lang.reflect.ParameterizedType
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

private const val indentBlock = "  "

fun Node.relevantMemberProperties() = this.javaClass.kotlin.memberProperties
    .filter { !it.name.startsWith("component") && it.name != "position" && it.name != "parent" }

data class DebugPrintConfiguration(
    val skipEmptyCollections: Boolean = false,
    val skipNull: Boolean = false,
    val forceShowPosition: Boolean = false,
    val hide: List<String> = emptyList()
)

private fun Node.showSingleAttribute(indent: String, sb: StringBuilder, propertyName: String, value: Any?) {
    sb.append("$indent$indentBlock$propertyName = ${value}\n")
}

fun Any?.debugPrint(indent: String = "", configuration: DebugPrintConfiguration = DebugPrintConfiguration()): String {
    val sb = StringBuilder()
    sb.append("$indent${this}\n")
    return sb.toString()
}

// some fancy reflection tests make sure the cast always succeeds
@Suppress("UNCHECKED_CAST")
fun Node.debugPrint(indent: String = "", configuration: DebugPrintConfiguration = DebugPrintConfiguration()): String {
    val sb = StringBuilder()
    if (this.relevantMemberProperties().isEmpty()) {
        sb.append("$indent${this.javaClass.simpleName}\n")
    } else {
        sb.append("$indent${this.javaClass.simpleName} {\n")
        var positionShouldBeDisplayed = configuration.forceShowPosition
        this.relevantMemberProperties().forEach { property ->
            if (property.name == "position") {
                positionShouldBeDisplayed = false
            }
            if (configuration.hide.contains(property.name) || property.name == "properties") {
                // skipping
            } else {
                val mt = property.returnType.javaType
                if (mt is ParameterizedType && mt.rawType == List::class.java) {
                    val value = property.get(this) as List<*>
                    if (value == null) {
                        sb.append("$indent$indentBlock${property.name} = null")
                    } else if (value.isEmpty() && configuration.skipEmptyCollections) {
                        // nothing to do
                    } else {
                        val paramType = mt.actualTypeArguments[0]
                        if (paramType is Class<*> && Node::class.java.isAssignableFrom(paramType)) {
                            sb.append("$indent$indentBlock${property.name} = [\n")
                            (value as List<Node>).forEach {
                                sb.append(
                                    it.debugPrint(
                                        indent + indentBlock + indentBlock, configuration
                                    )
                                )
                            }
                            sb.append("$indent$indentBlock]\n")
                        } else {
                            sb.append("$indent$indentBlock${property.name} = [\n")
                            value.forEach {
                                sb.append(
                                    it?.debugPrint(
                                        indent + indentBlock + indentBlock, configuration
                                    )
                                )
                            }
                            sb.append("$indent$indentBlock]\n")
                        }
                    }
                } else {
                    if (property.visibility == PUBLIC) {
                        val value = property.get(this)
                        if (value == null && configuration.skipNull) {
                            // nothing to do
                        } else {
                            if (value is Node) {
                                sb.append("$indent$indentBlock${property.name} = [\n")
                                sb.append(
                                    value.debugPrint(
                                        indent + indentBlock + indentBlock, configuration
                                    )
                                )
                                sb.append("$indent$indentBlock]\n")
                            } else {
                                this.showSingleAttribute(indent, sb, property.name, value)
                            }
                        }
                    }
                }
            }
        }
        if (positionShouldBeDisplayed) {
            val value = this.position
            if (value == null && configuration.skipNull) {
                // nothing to do
            } else {
                this.showSingleAttribute(indent, sb, "position", value)
            }
        }
        sb.append("$indent} // ${this.javaClass.simpleName}\n")
    }
    return sb.toString()
}
