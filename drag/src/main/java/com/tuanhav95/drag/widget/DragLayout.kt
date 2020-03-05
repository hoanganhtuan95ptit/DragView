package com.tuanhav95.drag.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout

class DragLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    var onTouchListener: OnTouchListener? = null

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (onTouchListener != null) onTouchListener!!.onInterceptTouchEvent(ev) else super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (onTouchListener != null) onTouchListener!!.onTouchEvent(event) else super.onTouchEvent(event)
    }

    interface OnTouchListener {
        fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
            return false
        }

        fun onTouchEvent(ev: MotionEvent?): Boolean {
            return false
        }
    }
}