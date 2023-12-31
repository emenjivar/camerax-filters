package com.emenjivar.camerafilter.screen.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
class CustomImageAnalyzer(
    private val onDrawImage: (
        filtered: Bitmap,
        originalLowQuality: Bitmap?
    ) -> Unit
) : ImageAnalysis.Analyzer {

    private val originalMat = Mat()

    override fun analyze(image: ImageProxy) {
        // Convert cameraX image to openCV mat

        val bitmap = image.toBitmap()
        Utils.bitmapToMat(bitmap, originalMat)

        val thumbnailMat = Mat()

        val thumbnailBitmap = image.toBitmap()
        Utils.bitmapToMat(thumbnailBitmap, thumbnailMat)

        // Convert to grayscale
        val grayMat = Mat()
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_RGBA2GRAY)

        // Rotate the image
        val rotatedGrayMat = Mat()
        Core.rotate(grayMat, rotatedGrayMat, Core.ROTATE_90_CLOCKWISE)

        val rotatedThumbnailMat = Mat()
        Core.rotate(thumbnailMat, rotatedThumbnailMat, Core.ROTATE_90_CLOCKWISE)

        val outputBitmap = Bitmap.createBitmap(
            rotatedGrayMat.width(),
            rotatedGrayMat.height(),
            Bitmap.Config.ARGB_8888
        )

        val outputThumbnailBitmap = Bitmap.createBitmap(
            rotatedThumbnailMat.width(),
            rotatedThumbnailMat.height(),
            Bitmap.Config.ARGB_8888
        )

        // Put final mats on output bitmaps
        Utils.matToBitmap(rotatedGrayMat, outputBitmap)
        Utils.matToBitmap(rotatedThumbnailMat, outputThumbnailBitmap)

        image.close()
        originalMat.release()
        onDrawImage(outputBitmap, outputThumbnailBitmap)
    }
}
