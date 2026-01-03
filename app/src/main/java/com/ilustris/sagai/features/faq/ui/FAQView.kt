package com.ilustris.sagai.features.faq.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.R
import com.ilustris.sagai.features.faq.data.model.FAQCategory

@Composable
fun FAQView(viewModel: FAQViewModel = hiltViewModel()) {
    val state by viewModel.faqState.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.search),
                    contentDescription = null,
                )
            },
            trailingIcon = {
                if (searchQuery.text.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = TextFieldValue("") }) {
                        Icon(
                            painterResource(id = R.drawable.round_close_24),
                            contentDescription = null,
                        )
                    }
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val faqState = state) {
            is FAQState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            is FAQState.FaqsRetrieved -> {
                val filteredFaqs = filterFaqs(faqState.faqs.items, searchQuery.text)
                if (filteredFaqs.isEmpty()) {
                    Button(onClick = { /* TODO: Implement AI Assistant */ }) {
                        Text("Ask the Saga Master")
                    }
                } else {
                    LazyColumn {
                        filteredFaqs.forEach { category ->
                            item {
                                Text(category.title, style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(category.items) { faqItem ->
                                FAQCard(faqItem = faqItem)
                            }
                        }
                    }
                }
            }

            is FAQState.FaqsError -> {
                Text(
                    text = faqState.message ?: stringResource(R.string.unexpected_error),
                    style = MaterialTheme.typography.labelMedium,
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
