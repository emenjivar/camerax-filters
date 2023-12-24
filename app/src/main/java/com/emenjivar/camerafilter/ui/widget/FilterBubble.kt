package com.emenjivar.camerafilter.ui.widget

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun FilterBubble(
    image: Bitmap,
    text: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (text != null) {
            Text(
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .padding(vertical = 3.dp, horizontal = 5.dp),
                text = text,
                color = Color.White,
                fontSize = 12.sp
            )
        }
        Box(
            modifier = Modifier
                .size(bubbleSize)
                .clip(CircleShape)
                .border(width = bubbleBorderSize, color = Color.Gray, shape = CircleShape)
                .background(color = Color.White, shape = CircleShape)
        ) {
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { view ->
                    view.setImageBitmap(image)
                }
            )
        }
    }
}

val bubbleSize = 70.dp
private val bubbleBorderSize = 2.dp