package com.example.naturemarks.ui.screens.markdetails

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naturemarks.app.NatureMarksApplication
import com.example.naturemarks.data.postmark.PostmarkRepository
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.naturemarks.R
import com.example.naturemarks.data.memory.MemoryRepository
import com.example.naturemarks.ui.model.PostmarkUiModel
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.android.awaitFrame

@Composable
fun MarkDetailsScreen (
    markId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as NatureMarksApplication

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val viewModel: MarkDetailsViewModel = viewModel(
        factory = MarkDetailsViewModelFactory(
            PostmarkRepository(app.database, MemoryRepository(app.database)),
            markId
        )
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showMap by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is MarkDetailsViewModel.MarkDetailsEvent.SharePhoto -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, event.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(
                        Intent.createChooser(intent, context.getString(R.string.share_photo))
                    )
                }
            }
        }
    }

    LaunchedEffect(state.isDbLoading) {
        if (!state.isDbLoading && !showMap) {
            awaitFrame()
            showMap = true
        }
    }

    if (state.isDbLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        state.postmark?.let { mark ->
            if (!isLandscape) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Text(
                        text = mark.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PhotoView(
                            photoUri = state.photo,
                            sharePhoto = viewModel::onSharePhoto
                        )

                        NotesView(
                            notes = state.notes,
                            edit = state.edit,
                            onEdit = { viewModel.editNotes() },
                            update = { viewModel.updateNotes(it) },
                            onSave = { viewModel.saveNotes(it) }
                        )
                    }

                    if (showMap) {
                        MapView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            coordinates = LatLng(mark.location.latitude, mark.location.longitude),
                            postmark = mark
                        )
                    }
                    else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues())
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = mark.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        PhotoView(
                            photoUri = state.photo,
                            sharePhoto = viewModel::onSharePhoto
                        )

                        NotesView(
                            notes = state.notes,
                            edit = state.edit,
                            onEdit = { viewModel.editNotes() },
                            update = { viewModel.updateNotes(it) },
                            onSave = { viewModel.saveNotes(it) }
                        )
                    }
                    if (showMap) {
                        MapView(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            coordinates = LatLng(mark.location.latitude, mark.location.longitude),
                            postmark = mark
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoView(
    photoUri: Uri?,
    sharePhoto: () -> Unit
) {
    if (photoUri != null) {
        Card(
            modifier = Modifier.size(400.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                )
                IconButton(
                    onClick = { sharePhoto() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share photo"
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_photo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NotesView(
    notes: String,
    edit: Boolean = false,
    onEdit: () -> Unit = {},
    update: (String) -> Unit = {},
    onSave: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically )
            {
                Text( text = stringResource(R.string.notes), style = MaterialTheme.typography.titleMedium )
                if (!edit) {
                    IconButton(onClick = { onEdit() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Edit Notes"
                        )
                    }
                }
            }
            if (edit) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { update(it) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { onSave(notes) },
                    modifier = Modifier.align(Alignment.End)
                ) { Text(stringResource(R.string.save))}
            }
            else {
                Text(
                    text = notes.ifEmpty { stringResource(R.string.empty_notes) },
                    style = MaterialTheme.typography.bodyLarge )
            }
        }
    }
}

@Composable
fun MapView(
    modifier: Modifier,
    coordinates: LatLng,
    postmark: PostmarkUiModel
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(coordinates, 10f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = rememberUpdatedMarkerState(position = coordinates),
            title = postmark.name,
            snippet = postmark.description
        )
    }
}