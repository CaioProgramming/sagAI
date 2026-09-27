package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Packs an embedding vector as raw bytes rather than JSON text — a few hundred floats as a decimal
 * string is both slower to (de)serialize and several times larger on disk than the same values as
 * 4-byte IEEE-754 words.
 */
object FloatArrayConverter {
    @TypeConverter
    @JvmStatic
    fun fromBytes(bytes: ByteArray?): FloatArray? {
        if (bytes == null) return null
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(bytes.size / Float.SIZE_BYTES) { buffer.float }
    }

    @TypeConverter
    @JvmStatic
    fun toBytes(values: FloatArray?): ByteArray? {
        if (values == null) return null
        val buffer =
            ByteBuffer
                .allocate(values.size * Float.SIZE_BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
        values.forEach { buffer.putFloat(it) }
        return buffer.array()
    }
}
