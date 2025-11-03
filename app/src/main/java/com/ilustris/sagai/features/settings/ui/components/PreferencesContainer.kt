package com.ilustris.sagai.features.settings.ui.components

import android.Manifest
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PreferencesContainer(
    title: String,
    description: String,
    isActivated: Boolean,
    modifier: Modifier = Modifier,
    onClickSwitch: (Boolean) -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(8.dp)
                    .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                description,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Light,
                    ),
                modifier = Modifier.alpha(.7f),
            )
        }
        Switch(
            checked = isActivated,
            colors =
                SwitchDefaults.colors().copy(
                    uncheckedBorderColor = Color.Transparent,
                ),
            modifier = Modifier.scale(.6f),
            onCheckedChange = {
                onClickSwitch(it)
            },
        )
    }
}

@Preview
@Composable
fun PreferencesContainerPreview() {
    PreferencesContainer(
        title = "Title",
        description = "Description",
        isActivated = true,
    )
}
