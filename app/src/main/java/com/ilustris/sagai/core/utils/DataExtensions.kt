package com.ilustris.sagai.core.utils

import com.google.firebase.ai.type.Schema
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.toString

fun toJsonSchema(
    clazz: Class<*>,
    lisItemMap: Map<String, Class<*>>? = null,
) = Schema.obj(
    properties = clazz.toSchemaMap(lisItemMap),
)

fun Class<*>.toSchema(
    nullable: Boolean,
    lisItemMap: Map<String, Class<*>>? = null,
): Schema {
    if (this.isEnum) {
        val enumConstants = this.enumConstants?.map { it.toString() } ?: emptyList()

        return Schema.enumeration(enumConstants, nullable = nullable)
    }

    if (this.isArray) {
        val arrayItemType = this.componentType
        return Schema.array(
            arrayItemType?.toSchema(nullable = false)
                ?: Schema.string(nullable = false),
            nullable = nullable,
        )
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
            val classMap = lisItemMap?.get(this.simpleName)

            Schema.array(
                classMap?.toSchema(nullable = false)
                    ?: Schema.string(nullable = false),
            )
        }

        else -> {
            Schema.obj(properties = this.toSchemaMap(), nullable = nullable)
        }
    }
}

fun Class<*>.toSchemaMap(lisItemMap: Map<String, Class<*>>? = null): Map<String, Schema> =
    declaredFields
        .filter { it.name != "\$stable" }
        .associate {
            val memberIsNullable =
                this
                    .kotlin.members
                    .find { member -> member.name == it.name }
                    ?.returnType
                    ?.isMarkedNullable
            val fieldName = it.name
            val fieldType = it.type

            val mapItem = lisItemMap?.get(fieldName)

            return@associate if (mapItem != null) {
                val schema = mapItem.toSchema(memberIsNullable == true, lisItemMap)
                fieldName to
                    Schema.array(
                        schema,
                        nullable = memberIsNullable == true,
                    )
            } else {
                val schema = fieldType.toSchema(memberIsNullable == true, lisItemMap)
                fieldName to schema
            }
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
): String {
    val deniedFields = filteredFields.plus("\$stable")
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
                        else -> "{}" // For nested objects, represent as empty JSON object
                    }
                "  \"$fieldName\": $fieldValue"
            }
    return "{\n$fields\n}"
}

fun Any.toJsonFormat() = Gson().toJson(this)

fun doNothing() = {}

fun Long.formatDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd 'of' MMMM yyyy", Locale.getDefault())
    return format.format(date)
}
