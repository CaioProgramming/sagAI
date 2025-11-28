import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga

data class SagaGen(
    val saga: Saga,
    val character: CharacterInfo,
)
