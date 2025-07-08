package com.ilustris.sagai.core.utils

import android.util.Log
import com.google.firebase.ai.type.Schema
import com.google.gson.Gson
import java.lang.reflect.ParameterizedType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.toString

fun toFirebaseSchema(clazz: Class<*>) =
    Schema.obj(
        properties = clazz.toSchemaMap(),
    )

fun Class<*>.toSchema(nullable: Boolean): Schema {
    if (this.isEnum) {
        val enumConstants = this.enumConstants?.map { it.toString() } ?: emptyList()

        return Schema.enumeration(enumConstants, nullable = nullable)
    }

    if (this.name.contains(Long::class.simpleName.toString(), true)) {
        return Schema.long(nullable = true)
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
            Log.i("SAGAI_MAPPER", "toSchema: mapping ${this.name} as object}")
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

fun Pair<String, String>.formatToString() = """ ${this.first} : "${this.second}" """

fun Class<*>.toJsonString(): String {
    val fields =
        declaredFields
            .filter { it.name != "\$stable" }
            .joinToString(separator = ",\n") { field ->
                val fieldName = field.name
                val fieldType = field.type
                val fieldValue =
                    when {
                        fieldType.isEnum -> "[ ${fieldType.enumConstants?.joinToString { it.toString() }} ]"
                        fieldType == String::class.java -> "\"\""
                        fieldType == Int::class.java || fieldType == Integer::class.java -> "0"
                        fieldType == Boolean::class.java -> "false"
                        fieldType == Double::class.java -> "0.0"
                        fieldType == Float::class.java -> "0.0f"
                        fieldType == Long::class.java -> "0L"
                        List::class.java.isAssignableFrom(fieldType) || Array::class.java.isAssignableFrom(fieldType) -> "[]"
                        else -> "{}" // For nested objects, represent as empty JSON object
                    }
                "  \"$fieldName\": $fieldValue"
            }
    return "{\n$fields\n}"
}

fun toJsonMap(
    clazz: Class<*>,
    filteredFields: List<String> = emptyList(),
    fieldCustomDescription: Pair<String, String>? = null,
): String {
    val deniedFields =
        filteredFields
            .plus("\$stable")
            .plus("companion")
    val fields =
        clazz
            .declaredFields
            .filter {
                deniedFields.contains(it.name).not()
            }.joinToString(separator = ",\n") { field ->
                val fieldName = field.name
                val fieldType = field.type
                val fieldValue =
                    when {
                        fieldType.isEnum -> "[ ${fieldType.enumConstants?.joinToString { it.toString() }} ]"
                        fieldType == String::class.java -> "\"\""
                        fieldType == Int::class.java || fieldType == Integer::class.java -> "0"
                        fieldType == Boolean::class.java -> "false"
                        fieldType == Double::class.java -> "0.0"
                        fieldType == Float::class.java -> "0.0f"
                        fieldType == Long::class.java -> "0L"
                        List::class.java.isAssignableFrom(fieldType) || Array::class.java.isAssignableFrom(fieldType) -> "[]"
                        else -> toJsonMap(fieldType)
                    }
                if (fieldCustomDescription != null && field.name == fieldCustomDescription.first) {
                    "\"${fieldCustomDescription.first}\": \"${fieldCustomDescription.second}\""
                } else {
                    "\"$fieldName\": $fieldValue"
                }
            }
    return "{\n$fields\n}"
}

fun Any?.toJsonFormat(): String {
    if (this == null) return emptyString()
    return Gson().toJson(this)
}

fun doNothing() = {}

fun Long.formatDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd 'of' MMMM yyyy 'at' HH:mm", Locale.getDefault())
    return format.format(date)
}

fun Long.formatHours(): String {
    val date = Date(this)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}
