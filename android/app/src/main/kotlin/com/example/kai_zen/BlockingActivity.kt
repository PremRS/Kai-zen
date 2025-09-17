package com.example.kai_zen

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class BlockingActivity : Activity() {
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make it fullscreen and stay on top
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        val layout: LinearLayout = LinearLayout(this)
        layout.setOrientation(LinearLayout.VERTICAL)
        layout.setPadding(50, 200, 50, 50)

        val message: TextView = TextView(this)
        message.setText("⚠️ This app is blocked during your focus time!")
        message.setTextSize(20f)
        message.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER)

        val exitButton: Button = Button(this)
        exitButton.setText("Return to Home")
        exitButton.setOnClickListener({ v ->
            finishAffinity() // Close BlockingActivity and return to home
        })

        layout.addView(message)
        layout.addView(exitButton)
        setContentView(layout)
    }

    public override fun onBackPressed() {
        // Disable back button to enforce blocking
    }
}
