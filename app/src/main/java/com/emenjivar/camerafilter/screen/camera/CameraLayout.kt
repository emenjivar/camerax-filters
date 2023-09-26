package com.emenjivar.camerafilter.screen.camera

import android.annotation.SuppressLint
import androidx.camera.core.TorchState
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.emenjivar.camerafilter.ui.widget.RoundedButton

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreenLayout(
    torchState: Int?,
    modifier: Modifier = Modifier,
    onToggleTorch: (Boolean) -> Unit,
    onFlipCamera: () -> Unit,
    onTakePhoto: () -> Unit,
    cameraContent: @Composable () -> Unit
) {
    var isTorchEnabled by remember(torchState) {
        mutableStateOf(torchState == TorchState.ON)
    }
    val torchIcon = remember(isTorchEnabled) {
        if (isTorchEnabled) {
            R.drawable.ic_flash_on
        } else {
            R.drawable.ic_flash_off
        }
    }

    Scaffold(
        modifier = modifier,
        contentColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    RoundedButton(
                        iconRes = R.drawable.ic_flip_camera,
                        onClick = onFlipCamera
                    )
                },
                actions = {
                    RoundedButton(
                        iconRes = torchIcon,
                        onClick = {
                            onToggleTorch(!isTorchEnabled)
                            isTorchEnabled = !isTorchEnabled
                        }
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) {
        Box(
            modifier = Modifier
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
            }
        }
    }
}

private val bottomControllersPadding = 16.dp
private val roundedButtonSize = 50.dp

@Composable
@Preview
private fun CameraScreenLayoutTorchOnPreview() {
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
            torchState = TorchState.ON,
            onToggleTorch = {},
            onFlipCamera = {},
            onTakePhoto = {}
        )
    }
}

@Composable
@Preview
private fun CameraScreenLayoutTorchOffPreview() {
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
            torchState = TorchState.OFF,
            onToggleTorch = {},
            onFlipCamera = {},
            onTakePhoto = {}
        )
    }
}
