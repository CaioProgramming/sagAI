package com.ilustris.sagai.core.utils

import com.google.firebase.ai.type.Schema

fun toJsonSchema(clazz: Class<*>) =
    Schema.obj(
        properties = clazz.toSchemaMap(),
    )

fun Class<*>.toSchema(): Schema =
    when {
        this.name.contains("String") -> Schema.string()
        this.name.contains("Int") -> Schema.integer()
        this.name.contains("Boolean") -> Schema.boolean()
        this.name.contains("Double") -> Schema.double()
        this.name.contains("Float") -> Schema.float()
        this.name.contains("Long") -> Schema.long()
        else -> Schema.string()
    }

fun Class<*>.toSchemaMap(): Map<String, Schema> =
    declaredFields
        .filter { it.name != "stable" }
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
