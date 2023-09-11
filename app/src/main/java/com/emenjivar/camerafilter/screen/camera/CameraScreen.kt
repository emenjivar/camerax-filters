package com.emenjivar.camerafilter.screen.camera

import android.Manifest
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emenjivar.camerafilter.R
import com.emenjivar.camerafilter.ext.settingsIntent
import com.emenjivar.camerafilter.ui.components.CustomDialog
import com.emenjivar.camerafilter.ui.components.CustomDialogAction
import com.emenjivar.camerafilter.ui.components.rememberCustomDialogController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {

    val context = LocalContext.current
    val dialogController = rememberCustomDialogController()
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { isGranted ->
            if (!isGranted) {
                dialogController.show()
            } else {
                // TODO: open here the camera
            }
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
                    Text(text = stringResource(R.string.button_take_photo))
                }
            }
        }
    }

    CustomDialog(
        controller = dialogController,
        title = stringResource(R.string.camera_dialog_access_title),
        description = stringResource(R.string.camera_dialog_access_description),
        confirmAction = CustomDialogAction(
            text = stringResource(R.string.button_settings),
            onClick = {
                context.startActivity(context.settingsIntent)
            }
        ),
        dismissAction = CustomDialogAction(
            text = stringResource(R.string.button_close),
            onClick = {}
        )
    )
}

private val bottomControllersPadding = 16.dp
