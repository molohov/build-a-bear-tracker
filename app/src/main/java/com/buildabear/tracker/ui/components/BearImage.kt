package com.buildabear.tracker.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.buildabear.tracker.domain.model.Bear
import java.io.File

@Composable
fun BearImage(
    bear: Bear,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val model = when {
        bear.localImagePath != null -> File(bear.localImagePath)
        bear.imageUrls.isNotEmpty() -> bear.imageUrls.first()
        else -> null
    }

    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = bear.name,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        Surface(modifier = modifier) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().size(48.dp),
            )
        }
    }
}
