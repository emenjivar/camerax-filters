package com.emenjivar.camerafilter.screen.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat

abstract class BaseAnalyzer(
    private val onDrawImage: (
        filtered: Bitmap,
        original: Bitmap
    ) -> Unit
) : ImageAnalysis.Analyzer {

    abstract fun performAnalysis(image: ImageProxy)

    override fun analyze(image: ImageProxy) {
        performAnalysis(image)
    }

    fun rotateBitmap(mat: Mat): Bitmap {
        val outputMat = Mat()
        Core.rotate(mat, outputMat, Core.ROTATE_90_CLOCKWISE)

        val outputBitmap = Bitmap.createBitmap(
            outputMat.width(),
            outputMat.height(),
            Bitmap.Config.ARGB_8888
        )

        Utils.matToBitmap(outputMat, outputBitmap)
        return outputBitmap
    }
}