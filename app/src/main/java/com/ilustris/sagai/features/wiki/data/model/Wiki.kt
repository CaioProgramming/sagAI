package com.ilustris.sagai.features.wiki.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.firebase.ai.type.Schema
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.toFirebaseSchema
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.wiki.data.model.WikiType.*
import java.util.Calendar

@Entity(
    tableName = "wikis",
    foreignKeys = [
        ForeignKey(
            entity = Saga::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Wiki(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val type: WikiType = WikiType.OTHER,
    val createdAt: Long = Calendar.getInstance().timeInMillis,
    @ColumnInfo(index = true)
    val sagaId: Int,
)

data class WikiGen(
    val wikis: List<Wiki>,
) {
    companion object {
        fun customSchema() =
            Schema.obj(
                mapOf("wikis" to Schema.array(items = toFirebaseSchema(Wiki::class.java))),
            )
    }
}

enum class WikiType {
    LOCATION,
    FACTION,
    ITEM,
    CREATURE,
    CONCEPT,
    ORGANIZATION,
    TECHNOLOGY,
    MAGIC,
    OTHER,
}

fun WikiType.iconForType(genre: Genre) =
    when (this) {
        LOCATION -> {
            if (genre == Genre.FANTASY) {
                R.drawable.ic_castle
            } else {
                R.drawable.city_building
            }
        }
        FACTION -> {
            if (genre == Genre.FANTASY) {
                R.drawable.ic_soldier
            } else {
                R.drawable.ic_bandit
            }
        }
        ITEM -> R.drawable.ic_cube
        CREATURE -> R.drawable.ic_creature
        CONCEPT -> R.drawable.ic_lamp
        ORGANIZATION -> R.drawable.ic_organization
        TECHNOLOGY -> R.drawable.ic_tech
        MAGIC -> R.drawable.ic_crow
        OTHER -> R.drawable.spark_round
    }
