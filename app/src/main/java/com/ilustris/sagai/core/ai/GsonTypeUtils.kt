package com.ilustris.sagai.core.ai

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.core.ai.model.AIGeneration
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.utils.toJsonMap
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

/**
 * R8-safe Gson types.
 */
fun gsonTypeOfMapString(valueClass: Class<*>): Type =
    TypeToken.getParameterized(Map::class.java, String::class.java, valueClass).type

fun gsonTypeOfStringAnyMap(): Type = gsonTypeOfMapString(Any::class.java)

fun gsonTypeOfMapStringString(): Type = gsonTypeOfMapString(String::class.java)

fun gsonTypeOfStringList(): Type =
    TypeToken.getParameterized(List::class.java, String::class.java).type

fun gsonTypeOfIntList(): Type =
    TypeToken.getParameterized(List::class.java, Int::class.javaObjectType).type

fun gsonTypeOfList(itemClass: Class<*>): Type =
    TypeToken.getParameterized(List::class.java, itemClass).type

inline fun <reified T> gsonTypeOfList(): Type = gsonTypeOfList(T::class.java)

/**
 * Parses `{ "reasoning": "...", "data": ... }` without resolving [AIGeneration] as a generic TypeToken.
 */
@Suppress("UNCHECKED_CAST")
fun <T> parseAIGenerationFromJson(
    gson: Gson,
    json: String,
    dataType: Type,
): AIGeneration<T> {
    val root = JsonParser.parseString(json)
    if (!root.isJsonObject) {
        throw JsonSyntaxException("Expected JSON object for AIGeneration")
    }
    val obj = root.asJsonObject
    val reasoning =
        obj.get("reasoning")
            ?.takeUnless { it.isJsonNull }
            ?.asString
            .orEmpty()
    val dataElement =
        obj.get("data")
            ?: throw JsonSyntaxException("Missing 'data' in AIGeneration JSON")

    val data: T =
        when (dataType) {
            String::class.java ->
                if (dataElement.isJsonPrimitive) {
                    dataElement.asString as T
                } else {
                    gson.fromJson(dataElement, String::class.java) as T
                }
            else -> gson.fromJson(dataElement, dataType) as T
        }
    return AIGeneration(reasoning = reasoning, data = data)
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> parseAIGenerationFromJson(
    gson: Gson,
    json: String,
): AIGeneration<T> = parseAIGenerationFromJson(gson, json, getJavaType<T>())

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> buildAIPromptOutputStructure(filterOutputFields: List<String> = emptyList()): String {
    val type = getJavaType<T>()
    val dataStructure = buildDataStructure(type, filterOutputFields)

    return toJsonMap(
        AIGeneration::class.java,
        fieldCustomDescriptions = listOf("data" to dataStructure),
    )
}

/**
 * Robust way to get java.lang.reflect.Type from a reified T.
 * Falls back to T::class.java for simple types if typeOf throws or fails in R8.
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> getJavaType(): Type {
    val clazz = T::class.java
    return try {
        // If it's a simple class without generic parameters, return it directly.
        // This avoids the risky typeOf call for 90% of use cases.
        if (clazz.typeParameters.isEmpty()) {
            clazz
        } else {
            typeOf<T>().javaType
        }
    } catch (e: Throwable) {
        clazz
    }
}

fun buildDataStructure(type: Type, filterOutputFields: List<String>): String {
    return when (type) {
        is Class<*> -> {
            when {
                type == String::class.java -> "\"string\""
                type == Int::class.java || type == Integer::class.java || type == Long::class.java || type == java.lang.Long::class.java -> "999"
                type == Boolean::class.java || type == java.lang.Boolean::class.java -> "false"
                type.isEnum -> type.enumConstants?.joinToString(" | ") ?: "ENUM_VALUE"
                else -> toJsonMap(type, filteredFields = filterOutputFields)
            }
        }
        is java.lang.reflect.ParameterizedType -> {
            val rawType = type.rawType
            when (rawType) {
                List::class.java, ArrayList::class.java -> {
                    val itemType = type.actualTypeArguments.firstOrNull() ?: Any::class.java
                    "[${buildDataStructure(itemType, filterOutputFields)}]"
                }
                Map::class.java, HashMap::class.java -> {
                    val valueType = type.actualTypeArguments.getOrNull(1) ?: Any::class.java
                    "{ \"key\": ${buildDataStructure(valueType, filterOutputFields)} }"
                }
                GeneratedContent::class.java -> {
                    val innerType = type.actualTypeArguments.firstOrNull() ?: Any::class.java
                    toJsonMap(
                        GeneratedContent::class.java,
                        fieldCustomDescriptions = listOf("data" to buildDataStructure(innerType, filterOutputFields)),
                        filteredFields = filterOutputFields
                    )
                }
                else -> toJsonMap(rawType as Class<*>, filteredFields = filterOutputFields)
            }
        }
        else -> "\"any\""
    }
}
