package com.tuanhav95.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tuanhav95.drag.DragView
import com.tuanhav95.example.fragment.BottomFragment
import com.tuanhav95.example.fragment.TopFragment
import kotlinx.android.synthetic.main.activity_normal.*

class NormalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal)

        draggablePanel.setDraggableListener(object : DragView.DraggableListener {
            override fun onChangeState(state: DragView.State) {
            }

            override fun onChangePercent(percent: Float) {
                alpha.alpha = 1 - percent
            }

        })

        supportFragmentManager.beginTransaction().add(R.id.frameFirst, TopFragment()).commit()
        supportFragmentManager.beginTransaction().add(R.id.frameSecond, BottomFragment()).commit()

        btnMax.setOnClickListener { draggablePanel.maximize() }
        btnMin.setOnClickListener { draggablePanel.minimize() }
        btnClose.setOnClickListener { draggablePanel.close() }

    }
}
