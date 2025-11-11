import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.saga.chat.data.model.Reaction

data class ReactionContent(
    @Embedded
    val data: Reaction,
    @Relation(
        parentColumn = "characterId",
        entityColumn = "id",
        entity = Character::class,
    )
    val character: Character,
)
