package com.buildabear.tracker.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.buildabear.tracker.domain.model.CollectionStatusType

@Composable
fun StatusChip(status: CollectionStatusType) {
    if (status == CollectionStatusType.UNSET) return
    val (label, color) = when (status) {
        CollectionStatusType.OWNED -> "Owned" to Color(0xFF2E7D32)
        CollectionStatusType.WANT -> "Want" to Color(0xFF1565C0)
        CollectionStatusType.DONT_WANT -> "Don't want" to Color(0xFFC62828)
        CollectionStatusType.UNSET -> return
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = color.copy(alpha = 0.4f),
        ),
    )
}

@Composable
fun SourceBadge(isCustom: Boolean) {
    if (!isCustom) return
    AssistChip(
        onClick = {},
        label = { Text("Custom") },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    )
}
