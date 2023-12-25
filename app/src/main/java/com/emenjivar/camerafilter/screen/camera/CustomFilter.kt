package com.emenjivar.camerafilter.screen.camera

import androidx.annotation.StringRes
import com.emenjivar.camerafilter.R

enum class CustomFilter(@StringRes val string: Int) {
    NORMAL(R.string.filter_type_normal),
    GRAY(R.string.filter_type_gray),
    SEPIA(R.string.filter_type_sepia),
    VINTAGE(R.string.filer_type_vintage)
}