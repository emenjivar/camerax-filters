package com.emenjivar.camerafilter.screen.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emenjivar.camerafilter.R
import com.emenjivar.camerafilter.ui.theme.RealTimeCameraFilterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreenLayout(
    modifier: Modifier = Modifier,
    onToggleTorch: (Boolean) -> Unit,
    onFlipCamera: () -> Unit,
    onTakePhoto: () -> Unit,
    cameraContent: @Composable () -> Unit
) {
    var isTorchEnabled by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            cameraContent()
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomControllersPadding),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onTakePhoto
                ) {
                    Text(text = stringResource(R.string.button_take_photo))
                }

                Button(onClick = {
                    onToggleTorch(!isTorchEnabled)
                    isTorchEnabled = !isTorchEnabled
                }) {
                    Text(text = "Torch $isTorchEnabled")
                }

                Button(onClick = onFlipCamera) {
                    Text(text = "Flip")
                }
            }
        }
    }
}

private val bottomControllersPadding = 16.dp

@Composable
@Preview
private fun CameraScreenLayoutPreview() {
    RealTimeCameraFilterTheme {
        CameraScreenLayout(
            cameraContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Camera should be displayed here")
                }
            },
            onToggleTorch = {},
            onFlipCamera = {},
            onTakePhoto = {}
        )
    }
}
