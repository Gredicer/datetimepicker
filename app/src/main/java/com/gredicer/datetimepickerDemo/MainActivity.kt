package com.gredicer.datetimepickerDemo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import com.gredicer.datetimepicker.DateTimePickerFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.hypot

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn1.setOnClickListener {
            val dateTimePickerFragment = DateTimePickerFragment.newInstance()
            dateTimePickerFragment.show(
                this.supportFragmentManager,
                DateTimePickerFragment.DateTimePickerTAG
            )

        }
    }
}