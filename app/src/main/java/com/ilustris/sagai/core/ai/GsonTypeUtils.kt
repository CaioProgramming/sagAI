package com.ilustris.sagai.core.ai

import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.core.ai.model.AIGeneration
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.utils.toJsonMap
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

/**
 * R8-safe Gson types.
 *
 * Do NOT pass [typeOf]().javaType into Gson/TypeToken — Kotlin's Type wrappers can become raw
 * [Class] instances at runtime and trigger ClassCastException inside Gson's TypeToken.
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> gsonTypeOf(): Type = gsonTypeOf(typeOf<T>())

@OptIn(ExperimentalStdlibApi::class)
fun gsonTypeOf(kType: KType): Type =
    when (kType.classifier) {
        Map::class -> {
            val keyClass = kType.arguments.getOrNull(0)?.type?.jvmErasure?.java ?: String::class.java
            val valueClass = kType.arguments.getOrNull(1)?.type?.jvmErasure?.java ?: Any::class.java
            TypeToken.getParameterized(Map::class.java, keyClass, valueClass).type
        }
        List::class -> {
            val itemClass = kType.arguments.getOrNull(0)?.type?.jvmErasure?.java ?: Any::class.java
            TypeToken.getParameterized(List::class.java, itemClass).type
        }
        GeneratedContent::class -> {
            val inner = innerTypeArgument(kType)
            TypeToken.getParameterized(GeneratedContent::class.java, inner).type
        }
        else -> kType.jvmErasure.java
    }

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> gsonTypeOfAIGeneration(): Type =
    TypeToken.getParameterized(AIGeneration::class.java, gsonTypeOf<T>()).type

private fun innerTypeArgument(kType: KType): Type {
    val arg = kType.arguments.firstOrNull()?.type
    return when (arg?.classifier) {
        String::class -> String::class.java
        Int::class -> Int::class.javaObjectType
        Long::class -> Long::class.javaObjectType
        Boolean::class -> Boolean::class.javaObjectType
        Double::class -> Double::class.javaObjectType
        Float::class -> Float::class.javaObjectType
        null -> String::class.java
        else -> gsonTypeOf(arg)
    }
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> buildAIPromptOutputStructure(filterOutputFields: List<String> = emptyList()): String {
    val dataStructure =
        when {
            typeOf<T>() == typeOf<String>() -> "\"string\""
            typeOf<T>().classifier == GeneratedContent::class -> {
                val innerClass =
                    typeOf<T>().arguments.firstOrNull()?.type?.jvmErasure?.java
                        ?: String::class.java
                val innerStructure =
                    if (innerClass == String::class.java) {
                        "\"string\""
                    } else {
                        toJsonMap(innerClass, filteredFields = filterOutputFields)
                    }
                toJsonMap(
                    GeneratedContent::class.java,
                    fieldCustomDescriptions = listOf("data" to innerStructure),
                    filteredFields = filterOutputFields,
                )
            }
            else -> toJsonMap(T::class.java, filteredFields = filterOutputFields)
        }
    return toJsonMap(
        AIGeneration::class.java,
        fieldCustomDescriptions = listOf("data" to dataStructure),
    )
}

fun gsonTypeOfStringAnyMap(): Type =
    TypeToken.getParameterized(Map::class.java, String::class.java, Any::class.java).type

fun gsonTypeOfStringList(): Type =
    TypeToken.getParameterized(List::class.java, String::class.java).type

fun gsonTypeOfIntList(): Type =
    TypeToken.getParameterized(List::class.java, Int::class.javaObjectType).type

inline fun <reified T> gsonTypeOfList(): Type =
    TypeToken.getParameterized(List::class.java, T::class.java).type
