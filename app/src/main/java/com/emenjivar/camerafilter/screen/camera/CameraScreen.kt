package com.emenjivar.camerafilter.screen.camera

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val dialogController = rememberCustomDialogController()
    val lifecycleOwner = LocalLifecycleOwner.current
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = remember(lensFacing) {
        CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
    }
    val executor = remember {
        ContextCompat.getMainExecutor(context)
    }
    var imageWithFilter by remember {
        mutableStateOf<Bitmap?>(null)
    }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(executor, CustomImageAnalyzer(
                    onDrawImage = { bitmap -> imageWithFilter = bitmap }
                ))
            }
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    val torchState = camera?.torchState()
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
            camera = startCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                cameraSelector = cameraSelector,
                preview = preview,
                previewView = previewView,
                imageCapture = imageCapture,
                imageAnalysis = imageAnalysis
            )
        }
    }

    CameraScreenLayout(
        torchState = torchState?.value,
        onToggleTorch = { enable ->
            camera?.cameraControl?.enableTorch(enable)
        },
        onFlipCamera = {
            lensFacing = flip(lensFacing)
        },
        onTakePhoto = {},
        rawCameraPreview = { modifier ->
            AndroidView(
                modifier = modifier,
                factory = { previewView }
            )
        },
        filterCameraPreview = { modifier ->
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { view ->
                    imageWithFilter?.let { safeImage ->
                        view.setImageBitmap(safeImage)
                    }
                },
                modifier = modifier
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

private fun flip(lensFacing: Int) = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
    CameraSelector.LENS_FACING_BACK
} else {
    CameraSelector.LENS_FACING_FRONT
}

@Composable
private fun Camera.torchState() =
    this.cameraInfo.torchState.observeAsState(initial = TorchState.OFF)

private suspend fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraSelector: CameraSelector,
    preview: Preview,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    imageAnalysis: ImageAnalysis
): Camera {
    val cameraProvider = context.getCameraProvider()
    cameraProvider.unbindAll()
    val camera = cameraProvider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        imageAnalysis,
        imageCapture
    )
    preview.setSurfaceProvider(previewView.surfaceProvider)
    return camera
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val provider = cameraProviderFuture.get()
        cameraProviderFuture.addListener({
            continuation.resume(provider)
        }, ContextCompat.getMainExecutor(this))
    }
