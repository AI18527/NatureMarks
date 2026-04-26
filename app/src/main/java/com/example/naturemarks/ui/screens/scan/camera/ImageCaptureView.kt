package com.example.naturemarks.ui.screens.scan.camera

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.naturemarks.R

@Composable
fun ImageCaptureView(
    markId: Int,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    var captureTrigger by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraView(
            mode = CameraMode.CAPTURE,
            captureTrigger = captureTrigger,
            onPhotoCaptured = { bitmap ->
                captureTrigger = false
                onPhotoCaptured(bitmap) }
        )
        Image(
            painter = painterResource(markId),
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            Text(
                text = stringResource(R.string.take_photo) + "" + stringResource(R.string.camera_emoji),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        FloatingActionButton(
            onClick = {
                captureTrigger = true
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
        }
    }
}