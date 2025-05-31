package com.ilustris.sagai.core.utils

import com.google.firebase.ai.type.Schema

fun toJsonSchema(clazz: Class<*>) =
    Schema.obj(
        properties = clazz.toSchemaMap(),
    )

fun Class<*>.toSchema(): Schema =
    when {
        this.name.contains(String::class.java.simpleName, true) -> {
            Schema.string()
        }
        this.name.contains(Int::class.java.simpleName, true) -> {
            Schema.integer()
        }
        this.name.contains(Boolean::class.java.simpleName, true) -> {
            Schema.boolean()
        }
        this.name.contains(Double::class.java.simpleName, true) -> {
            Schema.double()
        }
        this.name.contains(Float::class.java.simpleName, true) -> {
            Schema.float()
        }
        this.name.contains(Long::class.java.simpleName, true) -> {
            Schema.long()
        }
        this.name.contains("List", true) || this.name.contains("Array", true) -> {
            Schema.array(Schema.string()) // Default to string array for lists/arrays
        }
        else -> {
            Schema.string() // Default fallback
        }
    }

fun Class<*>.toSchemaMap(): Map<String, Schema> =
    declaredFields
        .filter { it.name != "\$stable" }
        .associate {
            it.name to it.type.toSchema()
        }

fun joinDeclaredFields(
    clazz: Class<*>,
    replaceSpecifFieldType: Pair<String, String>? = null,
): String =
    clazz
        .declaredFields
        .filter {
            it.name != "\$stable"
        }.joinToString(separator = ",\n") {
            if (replaceSpecifFieldType != null && it.name == replaceSpecifFieldType.first) {
                "\"${replaceSpecifFieldType.first}\": \"${replaceSpecifFieldType.second}\""
            } else {
                "\"${it.name}\": \"${it.type.toString().removePackagePrefix()}\""
            }
        }

fun String.removePackagePrefix(): String =
    this
        .substringAfterLast(".")
        .replace(".", "")
