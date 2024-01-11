package com.emenjivar.camerafilter.screen.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

class SepiaAnalyzer(
    private val onDrawImage: (filtered: Bitmap, original: Bitmap) -> Unit
) : BaseAnalyzer(onDrawImage) {
    override fun performAnalysis(image: ImageProxy) {
        val originalMat = Mat()
        val bitmap = image.toBitmap()
        Utils.bitmapToMat(bitmap, originalMat)

        // Convert to gray
        val grayMat = Mat()
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY)

        // Convert gray to multi-channel
        val grayMultiChannel = Mat(originalMat.size(), CvType.CV_8UC3)
        Imgproc.cvtColor(grayMat, grayMultiChannel, Imgproc.COLOR_GRAY2BGR)

        // Convert grayscale to float
        val alpha = MatOfFloat(0.393f, 0.769f, 0.189f, 0f)
        val beta = MatOfFloat(0.349f, 0.686f, 0.168f, 0f)
        val gamma = MatOfFloat(0.272f, 0.534f, 0.131f, 0f)

        val transformedMat = Mat(grayMultiChannel.size(), grayMultiChannel.type())
        Core.transform(grayMultiChannel, transformedMat, alpha)
        Core.transform(grayMultiChannel, grayMultiChannel, beta)
        Core.transform(grayMultiChannel, grayMultiChannel, gamma)

        // Convert back to 8-bit
        Core.convertScaleAbs(transformedMat, transformedMat, 255.0, 0.0)

        // Create a solid color image
        val solidColor = Mat(originalMat.size(), CvType.CV_8UC2)
        solidColor.setTo(Scalar(153.0, 204.0, 255.0))

        // Combine sepia and solid color images
        val sepiaColor = Mat()
        Core.addWeighted(solidColor, 1.0, originalMat, 1.0, 0.0, sepiaColor)

        val sepiaRotatedBitmap = rotateBitmap(sepiaColor)
        val rotatedOriginalBitmap = rotateBitmap(sepiaColor)

        grayMat.release()
        solidColor.release()

        image.close()
        onDrawImage(sepiaRotatedBitmap, rotatedOriginalBitmap)
    }
}