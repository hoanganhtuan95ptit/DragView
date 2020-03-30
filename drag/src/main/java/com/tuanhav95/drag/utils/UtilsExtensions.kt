package com.tuanhav95.drag.utils

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.res.Resources
import android.util.TypedValue
import android.view.animation.LinearInterpolator
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

fun Float.springAnimation(minValue: Float,
                          maxValue: Float,
                          startValue: Float,
                          endValue: Float,
                          onUpdate: (Float) -> Unit,
                          onEnd: () -> Unit) {

    println("" +
            minValue + "   " +
            maxValue + "   " +
            startValue + "   " +
            endValue + "   " )
    val springX = SpringForce(endValue)
    springX.dampingRatio = 0.7f
    springX.stiffness = 300f
    val springAnimation = SpringAnimation(FloatValueHolder())
    springAnimation.setStartVelocity(this)
            .setMinValue(minValue)
            .setMaxValue(maxValue)
            .setStartValue(startValue)
            .setSpring(springX)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .addUpdateListener { dynamicAnimation: DynamicAnimation<*>, value: Float, _: Float ->
                onUpdate(value)
                if (value == endValue) {
                    dynamicAnimation.cancel()
                }
            }
            .addEndListener { _: DynamicAnimation<*>?, _: Boolean, _: Float, _: Float ->
                onEnd()
            }
            .start()
}

fun ArrayList<ValuesHolder>.animation(
        duration: Long,
        onUpdate: (HashMap<String, Any>, ValueAnimator) -> Unit,
        onEnd: () -> Unit
) {
    val array = arrayOfNulls<ValuesHolder>(size)
    toArray(array)
    array.animation(duration, onUpdate, onEnd)
}

fun Array<ValuesHolder?>.animation(
        duration: Long,
        onUpdate: (HashMap<String, Any>, ValueAnimator) -> Unit,
        onEnd: () -> Unit
) {
    animation(duration, LinearInterpolator(), onUpdate, onEnd)
}

fun ArrayList<ValuesHolder>.animation(
        duration: Long,
        timeInterpolator: TimeInterpolator,
        onUpdate: (HashMap<String, Any>, ValueAnimator) -> Unit,
        onEnd: () -> Unit
) {
    val array = arrayOfNulls<ValuesHolder>(size)
    toArray(array)
    array.animation(duration, timeInterpolator, onUpdate, onEnd)
}

fun Array<ValuesHolder?>.animation(
        duration: Long,
        timeInterpolator: TimeInterpolator,
        onUpdate: (HashMap<String, Any>, ValueAnimator) -> Unit,
        onEnd: () -> Unit
) {
    val keys = ArrayList<String>()
    val tos = ArrayList<Any>()
    for (valuesHolder in this) {
        keys.add(valuesHolder!!.key)
        tos.add(valuesHolder.to)
    }

    val animator = ValueAnimator.ofPropertyValuesHolder(*this.to())

    animator.addUpdateListener {
        val keyData = HashMap<String, Any>()
        val currentData = ArrayList<Any>()

        for (key in keys) {
            val data = it.getAnimatedValue(key)
            keyData[key] = data
            currentData.add(data)
        }

        onUpdate(keyData, it)

        var equals = true
        for (i in tos.indices) {
            if (tos[i] != currentData[i]) equals = false
        }
        if (equals) {
            it.cancel()
        }
    }

    animator.withEndAction(Runnable {
        onEnd()
    })
    animator.interpolator = timeInterpolator
    animator.duration = duration
    animator.start()
}

fun Array<ValuesHolder?>.to(): Array<PropertyValuesHolder> {
    return Array(size) { i -> this[i]!!.to() }
}

class ValuesHolder(val key: String, val from: Any, val to: Any) {

    fun to(): PropertyValuesHolder {
        return if (from is Int && to is Int) {
            PropertyValuesHolder.ofInt(key, from, to)
        } else {
            PropertyValuesHolder.ofFloat(key, (from as Float), (to as Float))
        }
    }
}

fun Animator.withEndAction(runnable: Runnable) {
    addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            animation?.removeAllListeners()
            runnable.run()
        }

    })
}

fun Int.toPx(): Int {
    return this.toFloat().toPx()
}

fun Float.toPx(): Int {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
    ).toInt()
}
