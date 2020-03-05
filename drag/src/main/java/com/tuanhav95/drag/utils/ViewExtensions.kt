package com.tuanhav95.drag.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.isGone() = visibility == View.GONE

fun View.isVisible() = visibility == View.VISIBLE

fun View.isInVisible() = visibility == View.INVISIBLE

fun View.resizeAnimation(
        width: Int,
        height: Int,
        duration: Long = 350,
        onEnd: () -> Unit
) {

    val pointList = ArrayList<ValuesHolder>()
    pointList.add(
            ValuesHolder(
                    "width",
                    if (layoutParams.width <= 0 && width > 0) this.width else layoutParams.width,
                    width
            )
    )
    pointList.add(
            ValuesHolder(
                    "height",
                    if (layoutParams.height <= 0 && height > 0) this.height else layoutParams.height,
                    height
            )
    )

    pointList.animation(duration, { keyData, valueAnimator ->
        resize(keyData.get("width") as Int, keyData.get("height") as Int)
    }, {
        onEnd()
    })
}

fun View.translationYAnim(
        value: Float,
        duration: Long,
        onEnd: () -> Unit
) {
    val pointList = ArrayList<ValuesHolder>()
    pointList.add(ValuesHolder("translationY", translationY, value))
    pointList.animation(duration, { keyData, valueAnimator ->
        translationY = keyData.get("translationY") as Float
    }, {
        onEnd()
    })
}

fun View.reHeight(height: Int) {
    val newHeight = if (height >= 0) height else 0

    if (newHeight != layoutParams.height) {
        layoutParams.height = height
        layoutParams = layoutParams
    }
}

fun View.reWidth(width: Int) {
    val newWidth = if (width >= 0) width else 0

    if (newWidth != layoutParams.width) {
        layoutParams.width = width
        layoutParams = layoutParams
    }
}

fun View.resize(width: Int, height: Int) {
    var newWidth = layoutParams.width
    var newHeight = layoutParams.height

    if (width >= 0) newWidth = width
    if (height >= 0) newHeight = height

    if (newWidth != layoutParams.width || newHeight != layoutParams.height) {
        layoutParams.width = width
        layoutParams.height = height
        layoutParams = layoutParams
    }
}

fun ViewGroup.inflate(@LayoutRes l: Int): View {
    return LayoutInflater.from(context).inflate(l, this, false)
}
