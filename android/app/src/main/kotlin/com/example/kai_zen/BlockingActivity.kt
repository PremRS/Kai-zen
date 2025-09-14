package com.example.kaizen

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class BlockingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "This app is blocked now."
        tv.textSize = 22f
        setContentView(tv)
    }
}
