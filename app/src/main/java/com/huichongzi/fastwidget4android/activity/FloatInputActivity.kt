package com.huichongzi.fastwidget4android.activity

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.huichongzi.fastwidget4android.R
import com.huichongzi.fastwidget4android.widget.FloatInputView

class FloatInputActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.float_input_act)
        findViewById<TextView>(R.id.input_tv).setOnClickListener {
            findViewById<FloatInputView>(R.id.float_input_view).showInput()
        }
    }
}