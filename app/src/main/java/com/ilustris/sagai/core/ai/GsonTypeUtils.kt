package com.ilustris.sagai.core.ai

import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.core.ai.model.AIGeneration
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.utils.toJsonMap
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

/**
 * R8-safe Gson types — avoids anonymous [TypeToken] subclasses whose generic supertype
 * is stripped to a raw [Class], causing ClassCastException on [ParameterizedType].
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> gsonTypeOf(): Type = typeOf<T>().javaType

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> gsonTypeOfAIGeneration(): Type =
    TypeToken.getParameterized(AIGeneration::class.java, gsonTypeOf<T>()).type

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
