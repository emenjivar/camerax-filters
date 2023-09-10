package com.emenjivar.camerafilter.screen.camera

import android.Manifest
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {

    val permissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { isGranted ->
            Log.wtf("CameraScreen", "camera permission granted: $isGranted")
        }
    )

    LaunchedEffect(Unit) {
        if (permissionState.status.isGranted) {
            // Start here the camera
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // TODO: Put here the camera

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomControllersPadding),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { /*TODO: implement that event */ }
                ) {
                    Text(text = "Take photo")
                }
            }
        }
    }
}

private val bottomControllersPadding = 16.dp