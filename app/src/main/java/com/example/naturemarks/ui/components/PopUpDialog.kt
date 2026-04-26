package com.example.naturemarks.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun PopUpDialog(
    title: String,
    text: String,
    imageId: Int,
    contentDescription: String = "",
    textConfirm: String,
    onConfirmation: () -> Unit,
    textDismiss: String = "",
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
    onDismissRequest = { },
    shape = RoundedCornerShape(20.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
    },
    text = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(imageId),
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(120.dp)
                    .padding(vertical = 8.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    },
    confirmButton = {
        Button(
            onClick = {
                onConfirmation()
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(textConfirm)
        }
    },
        dismissButton = {
            if (textDismiss.isNotEmpty()) {
                Button(
                    onClick = {
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(textDismiss)
                }
            }
        }
    )
}