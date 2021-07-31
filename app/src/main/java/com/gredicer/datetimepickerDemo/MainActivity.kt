package com.gredicer.datetimepickerDemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gredicer.datetimepicker.DateTimePickerFragment
import com.yanzhenjie.sofia.Sofia
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Sofia.with(this)
            .statusBarDarkFont() // 状态栏深色字体。
            .invasionStatusBar() //内容入侵状态栏
            .navigationBarBackgroundAlpha(0) // 导航栏背景透明度。
            .statusBarBackgroundAlpha(0) // 状态栏背景透明度。
            .invasionNavigationBar() // 内容入侵导航栏。

        val dialog = DateTimePickerFragment.newInstance()
        btn1.setOnClickListener {
            dialog.mode(0)
            dialog.show(this.supportFragmentManager, null)
        }

        btn2.setOnClickListener {
            dialog.mode(1)
            dialog.show(this.supportFragmentManager, null)
        }

        btn3.setOnClickListener {
            dialog.mode(2)
            dialog.show(this.supportFragmentManager, null)
        }

        btn4.setOnClickListener {
            dialog.mode(3)
            dialog.show(this.supportFragmentManager, null)
        }

        btn5.setOnClickListener {
            dialog.mode(4)
            dialog.show(this.supportFragmentManager, null)
        }


        btn6.setOnClickListener {
            dialog.mode(0).default("2010-10-10 11:11:11")
            dialog.show(this.supportFragmentManager, null)
        }


        dialog.listener = object : DateTimePickerFragment.OnClickListener {
            override fun onClickListener(selectTime: String) {
                Toast.makeText(applicationContext, selectTime, Toast.LENGTH_SHORT).show()
            }
        }

    }
}