package com.emenjivar.camerafilter.screen.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
class GrayScaleAnalyzer(
    private val onDrawImage: (
        filtered: Bitmap,
        original: Bitmap?
    ) -> Unit
) : BaseAnalyzer(onDrawImage) {

    private val originalMat = Mat()

    override fun performAnalysis(image: ImageProxy) {
        // Convert cameraX image to openCV mat
        val bitmap = image.toBitmap()
        Utils.bitmapToMat(bitmap, originalMat)

        // Convert to grayscale
        val grayMat = Mat()
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_RGBA2GRAY)

        // Rotate the image
        val outputGrayBitmap = rotateBitmap(grayMat)
        val outputOriginalBitmap = rotateBitmap(originalMat)

        image.close()
        originalMat.release()
        onDrawImage(outputGrayBitmap, outputOriginalBitmap)
    }
}
