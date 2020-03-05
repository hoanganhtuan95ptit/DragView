package com.tuanhav95.drag

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.RelativeLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.tuanhav95.drag.utils.*
import com.tuanhav95.drag.widget.DragBehavior
import com.tuanhav95.drag.widget.DragLayout
import kotlinx.android.synthetic.main.layout_draggable_panel.view.*
import kotlin.math.abs

open class DragView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {

        private var scaledTouchSlop = 0

        private fun getScaledTouchSlop(context: Context): Int {
            if (scaledTouchSlop == 0) {
                scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
            }
            return scaledTouchSlop
        }

        private fun isViewUnder(view: View?, ev: MotionEvent): Boolean {
            return if (view == null) {
                false
            } else {
                ev.x >= view.left && ev.x < view.right && ev.y >= view.top && ev.y < view.bottom
            }
        }

        private fun calculateDistance(event: MotionEvent, downY: Float): Int {
            return abs(event.rawY - downY).toInt()
        }
    }

    var frameInitializing = false// toàn bộ giao diện đã được khởi tạo hay chưa

    var finalState: State? = null// trạng thái của Draggable đang hướng đến
    var finalHeight = 0// chiều tao của view first đang hướng đến khi max

    var firstViewMove = false// view first đang được di chuyển

    var velocityY = 0f
    var velocityTracker: VelocityTracker? = null

    var mCurrentState: State? = null
    var mCurrentPercent = -1f
    var mCurrentMarginTop = -1

    var mHeightWhenMax = 0
    var mHeightWhenMaxDefault = 0

    var mHeightWhenMiddle = 0
    var mHeightWhenMiddleDefault = 0
    var mPercentWhenMiddle = 0f

    var mHeightWhenMin = 0
    var mHeightWhenMinDefault = 0

    var mMarginTopWhenMin = 0
    var mMarginEdgeWhenMin = 0
    var mMarginBottomWhenMin = 0

    var mDraggableListener: DraggableListener? = null

    init {

        visibility = View.INVISIBLE

        inflate(context, R.layout.layout_draggable_panel, this)

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragView)
            mHeightWhenMax = typedArray.getDimensionPixelSize(R.styleable.DragView_height_when_max, 200.toPx())

            mPercentWhenMiddle = typedArray.getFloat(R.styleable.DragView_percent_when_middle, 0.9f)

            mHeightWhenMin = typedArray.getDimensionPixelSize(R.styleable.DragView_height_when_min, 80.toPx())

            mMarginEdgeWhenMin = typedArray.getDimensionPixelSize(R.styleable.DragView_margin_edge_when_min, 8.toPx())

            mMarginBottomWhenMin = typedArray.getDimensionPixelSize(R.styleable.DragView_margin_bottom_when_min, 8.toPx())

            finalState = State.values()[typedArray.getInt(R.styleable.DragView_state, 3)]

            typedArray.recycle()
        } else {
            mHeightWhenMax = 200.toPx()

            mPercentWhenMiddle = 0.9f

            mHeightWhenMin = 80.toPx()

            mMarginEdgeWhenMin = 8.toPx()

            mMarginBottomWhenMin = 8.toPx()

            finalState = State.CLOSE
        }

        finalHeight = mHeightWhenMax
        mHeightWhenMaxDefault = mHeightWhenMax
        mHeightWhenMinDefault = mHeightWhenMin

        frameDrag.onTouchListener = object : DragLayout.OnTouchListener {

            private var downY = 0f
            private var deltaY = 0

            private var firstViewDown = false

            override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
                when (ev!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        firstViewDown = isViewUnder(frameFirst, ev)
                        downY = ev.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        checkFrameFirstMove(ev)
                    }
                }
                return firstViewMove
            }

            override fun onTouchEvent(ev: MotionEvent?): Boolean {
                val motionY = ev!!.rawY.toInt()
                when (ev.action) {
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        firstViewMove = false
                        firstViewDown = false
                        handleUp()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        checkFrameFirstMove(ev)
                        if (firstViewMove) {
                            handleMove(motionY)
                        }
                    }
                }
                return firstViewDown
            }

            private fun checkFrameFirstMove(ev: MotionEvent) {
                if (firstViewMove) return
                val calculateDiff: Int = calculateDistance(ev, downY)
                val scaledTouchSlop: Int = getScaledTouchSlop(getContext())
                if (calculateDiff > scaledTouchSlop && firstViewDown) {
                    deltaY = ev.rawY.toInt() - (frameDrag.layoutParams as LayoutParams).topMargin
                    firstViewMove = true
                }
            }

            private fun handleUp() {
                val moveToMin = if (abs(velocityY) < 200) {
                    mCurrentMarginTop > mMarginTopWhenMin - mCurrentMarginTop
                } else {
                    velocityY >= 0
                }

                if (moveToMin) {
                    maxToMinAnim { minimize() }
                } else {
                    minToMaxAnim { maximize() }
                }
            }

            private fun handleMove(motionY: Int) {
                setMarginTop(motionY - deltaY)
            }

        }

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                initFrame()
            }
        })

        appbarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            private var verticalOffsetOld = 0

            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (frameInitializing && mCurrentPercent == 0f && !firstViewMove) {
                    val offset = abs(verticalOffset)
                    val delta = offset - verticalOffsetOld
                    verticalOffsetOld = offset

                    mHeightWhenMin += delta
                    mHeightWhenMiddle += delta
                }
            }
        })
    }

    /**
     * khởi tạo giao diện
     */
    open fun initFrame() {
        frameInitializing = true

        val params = frameFirst.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = DragBehavior(frameSecond)
        frameFirst.layoutParams = params

        mMarginTopWhenMin = height - mHeightWhenMin - mMarginBottomWhenMin

        mHeightWhenMax = finalHeight
        mHeightWhenMaxDefault = (width * 9 / 16f).toInt()

        mHeightWhenMiddle = (height - mPercentWhenMiddle * mMarginBottomWhenMin - mPercentWhenMiddle * mMarginTopWhenMin).toInt()
        mHeightWhenMiddleDefault = mHeightWhenMiddle

        /**
         * close
         */
        translationY = (mHeightWhenMinDefault + mMarginBottomWhenMin).toFloat()
        setMarginTop(mMarginTopWhenMin)
        gone()

        when (finalState) {
            State.MAX -> {
                maximize()
            }
            State.MIN -> {
                minimize()
            }
            else -> {
                close()
            }
        }
    }

    open fun getFrameDrag(): ViewGroup {
        return frameDrag
    }

    open fun getFrameFirst(): ViewGroup {
        return frameFirst
    }

    open fun getFrameSecond(): ViewGroup {
        return frameSecond
    }

    open fun setDraggableListener(draggableListener: DraggableListener) {
        mDraggableListener = draggableListener
    }

    /**
     * thiết lập chiều cao first view
     */
    open fun setHeightMax(height: Int) {
        finalHeight = height
        if (frameInitializing && finalState == mCurrentState) {// nếu view đã được khởi tạo và đã không drag thì sẽ mở rộng
            maximize()
        }
    }

    /**
     * mở rộng view
     */
    open fun maximize() {
        finalState = State.MAX
        if (!frameInitializing) {
            return
        }
        when (mCurrentState) {
            State.MAX -> {
                appbarLayout.resizeAnimation(-1, finalHeight, 300) {
                    mHeightWhenMax = finalHeight
                    appbarLayout.setExpanded(true, true)
                    updateState()
                }
            }
            State.MIN -> {
                minToMaxAnim { maximize() }
            }
            else -> {
                visible()
                closeToMinAnim { maximize() }
            }
        }
    }

    /**
     * thu nhỏ view
     */
    open fun minimize() {
        finalState = State.MIN
        if (!frameInitializing) {
            return
        }
        when (mCurrentState) {
            State.MAX -> {
                maxToMinAnim { minimize() }
            }
            State.MIN -> {
                visible()
                updateState()
            }
            else -> {
                visible()
                closeToMinAnim { minimize() }
            }
        }
    }

    /**
     * đóng view
     */
    open fun close() {
        finalState = State.CLOSE
        if (!frameInitializing) {
            return
        }
        when (mCurrentState) {
            State.MAX -> {
                maxToMinAnim { close() }
            }
            State.MIN -> {
                minToCloseAnim { close() }
            }
            else -> {
                gone()
                updateState()
            }
        }
    }

    @SuppressLint("Recycle")
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain()
                } else {
                    velocityTracker!!.clear()
                }
                velocityTracker!!.addMovement(ev)
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker!!.computeCurrentVelocity(1000)
                velocityY = velocityTracker!!.yVelocity
                velocityTracker!!.recycle()
                velocityTracker = null
            }
            MotionEvent.ACTION_CANCEL -> {
                velocityY = velocityTracker!!.yVelocity
                velocityTracker!!.recycle()
                velocityTracker = null
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker!!.addMovement(ev)
                velocityTracker!!.computeCurrentVelocity(1000)
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun setMarginTop(top: Int) {
        val marginTop = when {
            top < 0 -> 0
            top > mMarginTopWhenMin -> mMarginTopWhenMin
            else -> top
        }

        if (marginTop == mCurrentMarginTop) return
        mCurrentMarginTop = marginTop

        val percent: Float = mCurrentMarginTop * 1f / mMarginTopWhenMin
        setPercent(percent)
    }

    private fun setPercent(percent: Float) {
        if (mCurrentPercent == percent || percent > 1 || percent < 0) return
        mCurrentPercent = percent

        refresh()
    }

    /**
     * cập nhật toàn bộ giao diện
     */
    open fun refresh() {

        updateState()

        mDraggableListener?.onChangePercent(mCurrentPercent)

        val layoutParams = frameDrag.layoutParams as LayoutParams
        layoutParams.topMargin = (mMarginTopWhenMin * mCurrentPercent).toInt()
        layoutParams.leftMargin = (mMarginEdgeWhenMin * mCurrentPercent).toInt()
        layoutParams.rightMargin = (mMarginEdgeWhenMin * mCurrentPercent).toInt()
        layoutParams.bottomMargin = (mMarginBottomWhenMin * mCurrentPercent).toInt()
        frameDrag.layoutParams = layoutParams

        val toolBarHeight = when {
            firstViewMove -> {
                (mHeightWhenMaxDefault - (mHeightWhenMaxDefault - mHeightWhenMinDefault) * mCurrentPercent).toInt()
            }
            mCurrentPercent == 0f -> {
                mHeightWhenMaxDefault
            }
            else -> {
                mHeightWhenMinDefault
            }
        }

        toolbar.reHeight(toolBarHeight)

        refreshFrameFirst()
    }

    /**
     * cập nhật giao diện First View
     */
    open fun refreshFrameFirst() {
        val frameFistHeight = if (mCurrentPercent < mPercentWhenMiddle) {
            (mHeightWhenMax - (mHeightWhenMax - mHeightWhenMiddle) * mCurrentPercent / mPercentWhenMiddle)
        } else {
            (mHeightWhenMiddle - (mHeightWhenMiddle - mHeightWhenMin) * (mCurrentPercent - mPercentWhenMiddle) / (1 - mPercentWhenMiddle))
        }
        appbarLayout.reHeight(frameFistHeight.toInt())
    }

    private fun minToMaxAnim(onEnd: () -> Unit) {
        finalState = State.MAX
        springYAnim(0f, onEnd)
    }

    private fun maxToMinAnim(onEnd: () -> Unit) {
        finalState = State.MIN
        springYAnim(mMarginTopWhenMin.toFloat(), onEnd)
    }

    private fun minToCloseAnim(onEnd: () -> Unit) {
        finalState = State.CLOSE
        translationYAnim((mHeightWhenMinDefault + mMarginBottomWhenMin).toFloat()) {
            onEnd()
        }
    }

    private fun closeToMinAnim(onEnd: () -> Unit) {
        finalState = State.MIN
        translationYAnim((0).toFloat()) {
            onEnd()
        }
    }

    private fun View.translationYAnim(value: Float, onEnd: () -> Unit) {
        translationYAnim(value, 300) {
            updateState()
            onEnd()
        }
    }

    private fun springYAnim(endValue: Float, onEnd: () -> Unit) {
        velocityY.springAnimation(0.toFloat(), mMarginTopWhenMin.toFloat(), mCurrentMarginTop.toFloat(), endValue, { value: Float ->
            setMarginTop(value.toInt())
        }, {
            updateState()
            onEnd()
        })
    }

    private fun updateState() {
        if (!firstViewMove) {
            val state = if (mCurrentPercent == 0f) {
                State.MAX
            } else if (mCurrentPercent == 1f && translationY == 0f) {
                State.MIN
            } else if (mCurrentPercent == 1f && translationY > 0f) {
                State.CLOSE
            } else {
                null
            }

            if (state != null && mCurrentState != state) {
                mCurrentState = state
                mDraggableListener?.onChangeState(mCurrentState!!)
            }
        }
    }

    interface DraggableListener {
        fun onChangeState(state: State) {}
        fun onChangePercent(percent: Float) {}
    }

    enum class State {
        MAX,
        MIN,
        CLOSE
    }
}