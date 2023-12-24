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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.emenjivar.camerafilter.R
import com.emenjivar.camerafilter.ext.settingsIntent
import com.emenjivar.camerafilter.ui.components.CustomDialog
import com.emenjivar.camerafilter.ui.components.CustomDialogAction
import com.emenjivar.camerafilter.ui.components.rememberCustomDialogController
import com.emenjivar.camerafilter.ui.widget.FilterBubble
import com.emenjivar.camerafilter.ui.widget.bubbleSize
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraScreen() {
    // Compose variables
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val lifecycleOwner = LocalLifecycleOwner.current

    // Controllers
    val dialogController = rememberCustomDialogController()
    val listState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { isGranted ->
            if (!isGranted) {
                dialogController.show()
            }
        }
    )

    // Remembered values
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
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
    var enableGrayFilter by remember { mutableStateOf(false) }

    // Calculated
    val extraScrollSpace = remember {
        (screenWidth / 2).dp - bubbleSize / 2 - bottomControlsSpacedBy / 2
    }
    val maxOffsetPx = with(LocalDensity.current) {
        (bubbleSize + bottomControlsSpacedBy).toPx()
    }
    val middle = remember { maxOffsetPx / 2f }
    var selectedItem by remember { mutableIntStateOf(DEFAULT_FILTER_INDEX) }

    val preview = Preview.Builder().build()

    // States
    val torchState = camera?.torchState()

    // Launched
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

    // Calculate the nearest index to the center
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .map { data ->
                val (index, offset) = data
                if (offset >= 0 && offset <= middle) {
                    index
                } else {
                    index + 1
                }
            }.distinctUntilChanged()
            .onEach { selected ->
                selectedItem = selected
            }.launchIn(this)
    }

    CameraScreenLayout(
        torchState = torchState?.value,
        isFilterEnabled = enableGrayFilter,
        onToggleTorch = { enable ->
            camera?.cameraControl?.enableTorch(enable)
        },
        onFlipCamera = {
            lensFacing = flip(lensFacing)
        },
        onTakePhoto = {
            enableGrayFilter = !enableGrayFilter
        },
        rawCameraPreview = { modifier ->
            AndroidView(
                modifier = modifier,
                factory = { previewView }
            )
        },
        filterCameraPreview = { modifier ->
            if (enableGrayFilter) {
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
        },
        bottomControllers = { modifier ->
            Column(modifier = modifier) {
                Text(
                    text = "Selected: $selectedItem",
                    color = Color.White,
                    modifier = Modifier.background(Color.Black)
                )
                LazyRow(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Black
                                )
                            )
                        ),
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(bottomControlsSpacedBy),
                    contentPadding = PaddingValues(horizontal = extraScrollSpace)
                ) {
                    item {
                        imageWithFilter?.let { safeGrayImage ->
                            FilterBubble(
                                modifier = Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { enableGrayFilter = false }
                                ),
                                image = safeGrayImage,
                                text = "Normal"
                            )
                        }
                    }

                    item {
                        imageWithFilter?.let { safeGrayImage ->
                            FilterBubble(
                                modifier = Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { enableGrayFilter = true }
                                ),
                                image = safeGrayImage,
                                text = "Gray"
                            )
                        }
                    }

                    item {
                        imageWithFilter?.let { safeGrayImage ->
                            FilterBubble(
                                image = safeGrayImage,
                                text = "Sepia"
                            )
                        }
                    }

                    repeat(55) {
                        item {
                            imageWithFilter?.let { safeGrayImage ->
                                FilterBubble(
                                    image = safeGrayImage,
                                    text = "Vintage"
                                )
                            }
                        }
                    }
                }
            }
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

private val bottomControlsSpacedBy = 4.dp
private const val DEFAULT_FILTER_INDEX = 0

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
