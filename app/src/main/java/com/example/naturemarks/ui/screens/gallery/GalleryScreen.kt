package com.example.naturemarks.ui.screens.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naturemarks.R
import com.example.naturemarks.app.NatureMarksApplication
import com.example.naturemarks.data.memory.MemoryRepository
import com.example.naturemarks.data.postmark.PostmarkRepository
import com.example.naturemarks.ui.model.PostmarkUiModel
import com.example.naturemarks.util.MarkImageHelper

@Composable
fun GalleryScreen (
    onMarkClicked: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as NatureMarksApplication

    val viewModel: GalleryViewModel = viewModel(
        factory = GalleryViewModelFactory(
            PostmarkRepository(app.database, MemoryRepository(app.database))
        )
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        GalleryTopBar(
            isGrouped = state.groupedByCity,
            onToggle = { viewModel.toggleGroupByCity() }
        )
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
         else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.groupedByCity) {
                    val groups = viewModel.getGroupedMarks()
                    groups.forEach { group ->
                        stickyHeader {
                            CityHeader(city = group.city)
                        }
                        items(group.items) { mark ->
                            MarkItem(
                                mark = mark,
                                onMarkClicked = onMarkClicked
                            )
                        }
                    }
                } else {
                    items(state.marks) { mark ->
                        MarkItem(
                            mark = mark,
                            onMarkClicked = onMarkClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryTopBar(
    isGrouped: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.toggle_group_by_city),
        )
        Switch(
            checked = isGrouped,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
fun CityHeader(city: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 4.dp
    ) {
        Text(
            text = city,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun MarkItem(
    mark: PostmarkUiModel,
    onMarkClicked: (String) -> Unit
){
    val markImage = MarkImageHelper.getMarkImage(mark.imageId)

    Card(
        modifier = Modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = { onMarkClicked(mark.imageId) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(markImage),
                contentDescription = mark.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(6.dp)
            )
            Text(
                text = mark.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}

