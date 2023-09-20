package com.emenjivar.camerafilter.screen.camera

import android.Manifest
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.emenjivar.camerafilter.R
import com.emenjivar.camerafilter.ext.settingsIntent
import com.emenjivar.camerafilter.ui.components.CustomDialog
import com.emenjivar.camerafilter.ui.components.CustomDialogAction
import com.emenjivar.camerafilter.ui.components.rememberCustomDialogController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val dialogController = rememberCustomDialogController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val lensFacing = remember { CameraSelector.LENS_FACING_BACK }
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = remember {
        CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
    }

    val permissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { isGranted ->
            if (!isGranted) {
                dialogController.show()
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    LaunchedEffect(permissionState.status, lensFacing) {
        if (permissionState.status.isGranted) {
            startCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                cameraSelector = cameraSelector,
                preview = preview,
                previewView = previewView,
                imageCapture = imageCapture
            )
        }
    }

    CameraScreenLayout(
        onTakePhoto = {},
        cameraContent = {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { previewView }
            )
        }
    )

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

private suspend fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraSelector: CameraSelector,
    preview: Preview,
    previewView: PreviewView,
    imageCapture: ImageCapture
) {
    val cameraProvider = context.getCameraProvider()
    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        imageCapture
    )
    preview.setSurfaceProvider(previewView.surfaceProvider)
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val provider = cameraProviderFuture.get()
        cameraProviderFuture.addListener({
            continuation.resume(provider)
        }, ContextCompat.getMainExecutor(this))
    }
