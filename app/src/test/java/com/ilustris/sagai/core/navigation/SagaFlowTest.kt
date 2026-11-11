package com.ilustris.sagai.core.navigation

import com.ilustris.sagai.ui.navigation.BookReaderKey
import com.ilustris.sagai.ui.navigation.CharacterDetailKey
import com.ilustris.sagai.ui.navigation.ChatKey
import com.ilustris.sagai.ui.navigation.HomeKey
import com.ilustris.sagai.ui.navigation.MilestoneKey
import com.ilustris.sagai.ui.navigation.SagaDetailKey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SagaFlowTest {
    @Test
    fun `the saga's own screens qualify`() {
        assertTrue(ChatKey("4").belongsToSagaFlow(4, emptySet()))
        assertTrue(SagaDetailKey("4").belongsToSagaFlow(4, emptySet()))
        assertTrue(BookReaderKey(4, 1).belongsToSagaFlow(4, emptySet()))
        assertTrue(CharacterDetailKey(9).belongsToSagaFlow(4, setOf(9)))
    }

    @Test
    fun `home, other sagas and the milestone reveal do not`() {
        assertFalse(HomeKey.belongsToSagaFlow(4, emptySet()))
        assertFalse(null.belongsToSagaFlow(4, emptySet()))
        assertFalse(ChatKey("5").belongsToSagaFlow(4, emptySet()))
        assertFalse(MilestoneKey(4).belongsToSagaFlow(4, emptySet()))
        assertFalse(CharacterDetailKey(9).belongsToSagaFlow(4, setOf(1)))
    }
}
