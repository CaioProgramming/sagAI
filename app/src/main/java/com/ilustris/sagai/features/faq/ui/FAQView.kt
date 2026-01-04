package com.ilustris.sagai.features.faq.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.features.faq.data.model.FAQCategory
import com.ilustris.sagai.features.faq.data.model.FAQContent
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun FAQView(
    viewModel: FAQViewModel = hiltViewModel(),
    navHostController: NavHostController,
) {
    val state by viewModel.faqState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    SharedTransitionLayout {
        AnimatedContent(state, transitionSpec = {
            fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
        }) { faqState ->
            Box(Modifier.fillMaxSize()) {
                when (faqState) {
                    FAQState.Loading -> {
                        Column(
                            Modifier
                                .align(Alignment.Center)
                                .reactiveShimmer(true),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_spark),
                                null,
                                Modifier
                                    .size(24.dp)
                                    .gradientFill(gradientAnimation(holographicGradient))
                                    .sharedElement(
                                        rememberSharedContentState("saga-loader"),
                                        this@AnimatedContent,
                                    ),
                            )
                            Text(
                                "Buscando informações...",
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        textAlign = TextAlign.Center,
                                    ),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .reactiveShimmer(true),
                            )
                        }
                    }

                    is FAQState.FaqsError -> {
                        Box(Modifier.align(Alignment.Center)) {
                            Text(faqState.message ?: stringResource(R.string.unexpected_error))
                        }
                    }

                    FAQState.AiLoading -> {
                        Column(
                            Modifier
                                .align(Alignment.Center)
                                .reactiveShimmer(true),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                null,
                                Modifier
                                    .gradientFill(gradientAnimation(holographicGradient))
                                    .size(24.dp)
                                    .sharedElement(
                                        rememberSharedContentState("saga-loader"),
                                        this@AnimatedContent,
                                    ),
                            )
                            Text(
                                "Consultando o Mestre da Saga...",
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        textAlign = TextAlign.Center,
                                    ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    is FAQState.AiReply -> {
                        AiReplyView(
                            reply = faqState.reply,
                            this@AnimatedContent,
                            onBackClick = { viewModel.clearAiReply() },
                        )
                    }

                    is FAQState.FaqsRetrieved -> {
                        FaqContentView(
                            content = faqState.faqs,
                            query = query,
                            this@AnimatedContent,
                            onQueryChange = viewModel::updateQuery,
                            onAskAi = viewModel::askAi,
                        ) {
                            navHostController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.AiReplyView(
    reply: String,
    animatedContentScope: AnimatedContentScope,
    onBackClick: () -> Unit,
) {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painterResource(R.drawable.ic_spark),
            null,
            Modifier
                .sharedElement(
                    rememberSharedContentState("saga-loader"),
                    animatedContentScope,
                ).gradientFill(Brush.verticalGradient(holographicGradient))
                .size(24.dp),
        )

        Text(
            reply,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
        ) {
            Text("Entendido!")
        }
    }
}

@Composable
private fun SharedTransitionScope.FaqContentView(
    content: FAQContent,
    query: String,
    animatedContentScope: AnimatedContentScope,
    onQueryChange: (String) -> Unit,
    onAskAi: () -> Unit,
    onBackClick: () -> Unit,
) {
    val filteredCategories = remember(content, query) { filterFaqs(content.categories, query) }

    LazyColumn(
        Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            IconButton(
                onClick = { onBackClick() },
                modifier =
                    Modifier
                        .padding(top = 16.dp)
                        .clip(CircleShape)
                        .size(32.dp),
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            ) {
                Icon(
                    painterResource(R.drawable.ic_back_left),
                    null,
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                )
            }
        }

        stickyHeader {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 16.dp),
            ) {
                Text(
                    "FAQ",
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                        ),
                    textAlign = TextAlign.Start,
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Pesquisar dúvidas...") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp)),
                    leadingIcon = {
                        Icon(painterResource(R.drawable.search), null)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(painterResource(R.drawable.round_close_24), null)
                            }
                        }
                    },
                    colors =
                        TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            unfocusedContainerColor =
                                MaterialTheme.colorScheme.surfaceContainer.copy(
                                    alpha = .5f,
                                ),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    singleLine = true,
                )
            }
        }

        if (filteredCategories.isEmpty()) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "Não encontramos o que você procura...",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(.5f),
                    )

                    val brush = Brush.linearGradient(holographicGradient)

                    Button(
                        onClick = onAskAi,
                        shape = RoundedCornerShape(25.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(),
                        modifier =
                            Modifier
                                .border(
                                    1.dp,
                                    brush,
                                    RoundedCornerShape(25.dp),
                                ).fillMaxWidth()
                                .height(50.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.gradientFill(brush),
                        ) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                null,
                                Modifier
                                    .size(24.dp)
                                    .gradientFill(brush)
                                    .sharedElement(
                                        rememberSharedContentState("saga-loader"),
                                        animatedContentScope,
                                    ),
                            )

                            Text(
                                "Perguntar ao Mestre da Saga",
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        } else {
            filteredCategories.forEach { category ->
                item {
                    Text(
                        category.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier =
                            Modifier
                                .alpha(.5f)
                                .padding(8.dp),
                    )
                }

                item {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(15.dp),
                            ),
                    ) {
                        category.items.forEach { item ->
                            FAQCard(faqItem = item, item == category.items.last())
                        }
                    }
                }
            }

            if (query.isNotEmpty()) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TextButton(onClick = onAskAi) {
                            Text("Ainda em dúvida? Pergunte ao Mestre da Saga")
                        }
                    }
                }
            }
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
            ) {
                Text(
                    stringResource(R.string.app_version),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier =
                        Modifier
                            .alpha(.5f)
                            .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun filterFaqs(
    categories: List<FAQCategory>,
    query: String,
): List<FAQCategory> {
    if (query.isEmpty()) return categories
    val filteredCategories = mutableListOf<FAQCategory>()
    for (category in categories) {
        val filteredItems =
            category.items.filter {
                it.question.contains(query, ignoreCase = true) ||
                    it.answer.contains(
                        query,
                        ignoreCase = true,
                    )
            }
        if (filteredItems.isNotEmpty()) {
            filteredCategories.add(category.copy(items = filteredItems))
        }
    }
    return filteredCategories
}
