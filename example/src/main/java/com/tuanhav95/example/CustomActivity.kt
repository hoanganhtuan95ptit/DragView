package com.tuanhav95.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tuanhav95.drag.DragView
import com.tuanhav95.drag.utils.toPx
import com.tuanhav95.example.fragment.BottomFragment
import com.tuanhav95.example.fragment.TopFragment
import kotlinx.android.synthetic.main.activity_custom.*
import kotlinx.android.synthetic.main.layout_bottom.*
import kotlin.math.max
import kotlin.math.min

class CustomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)

        dragView.setDragListener(object : DragView.DragListener {
            override fun onChangeState(state: DragView.State) {
            }

            override fun onChangePercent(percent: Float) {
                alpha.alpha = 1 - percent
                shadow.alpha = percent
            }

        })

        supportFragmentManager.beginTransaction().add(R.id.frameTop, TopFragment()).commit()
        supportFragmentManager.beginTransaction().add(R.id.frameBottom, BottomFragment()).commit()

        btnMax.setOnClickListener { dragView.maximize() }
        btnMin.setOnClickListener { dragView.minimize() }
        btnClose.setOnClickListener { dragView.close() }

        btnSetHeightMax.setOnClickListener {
            var heightMax = 0
            if (etHeightMax.text.isNotEmpty()) {
                heightMax = etHeightMax.text.toString().toInt()
            }
            heightMax = max(heightMax, 200)
            heightMax = min(heightMax, 400)

            dragView.setHeightMax(heightMax.toPx(), true)
        }
    }
}
