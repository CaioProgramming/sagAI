package com.ilustris.sagai.core.utils

import com.google.firebase.ai.type.Schema
import java.lang.reflect.ParameterizedType
import kotlin.toString

fun toJsonSchema(clazz: Class<*>) =
    Schema.obj(
        properties = clazz.toSchemaMap(),
    )

fun Class<*>.toSchema(nullable: Boolean): Schema {
    if (this.isEnum) {
        val enumConstants = this.enumConstants?.map { it.toString() } ?: emptyList()

        return Schema.enumeration(enumConstants, nullable = nullable)
    }

    return when (this) {
        String::class.java -> {
            Schema.string(
                nullable = nullable,
            )
        }

        Int::class.java, Integer::class.java -> {
            Schema.integer(
                nullable = nullable,
            )
        }

        Boolean::class.java -> {
            Schema.boolean(
                nullable = nullable,
            )
        }

        Double::class.java -> {
            Schema.double(
                nullable = nullable,
            )
        }

        Float::class.java -> {
            Schema.float(
                nullable = nullable,
            )
        }

        Long::class.java -> {
            Schema.long(
                nullable = nullable,
            )
        }

        List::class.java, Array::class.java -> {
            val itemType =
                this.genericInterfaces
                    .filterIsInstance<ParameterizedType>()
                    .firstOrNull()
                    ?.actualTypeArguments
                    ?.firstOrNull() as? Class<*>

            Schema.array(
                itemType?.toSchema(nullable = nullable) ?: Schema.string(nullable = nullable),
            ) // Default to string array for lists/arrays
        }

        else -> {
            Schema.obj(properties = this.toSchemaMap(), nullable = nullable) // Default fallback
        }
    }
}

fun Class<*>.toSchemaMap(): Map<String, Schema> =
    declaredFields
        .filter { it.name != "\$stable" }
        .associate {
            val memberIsNullable =
                this
                    .kotlin.members
                    .find { member -> member.name == it.name }
                    ?.returnType
                    ?.isMarkedNullable
            it.name to it.type.toSchema(memberIsNullable == true)
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
