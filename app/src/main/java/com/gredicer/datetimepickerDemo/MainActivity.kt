package com.gredicer.datetimepickerDemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gredicer.datetimepicker.DateTimePickerFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val a = DateTimePickerFragment
            .newInstance()
            .year().month().day().hour().minute()
            .default("2010-05-11 22:33:ss")
        a.listener = object : DateTimePickerFragment.OnClickListener {
            override fun onClickListener(selectTime: String) {
                Log.d("gredicer1", selectTime)
            }
        }

        btn1.setOnClickListener {
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) //设置日期格式
            a.show(this.supportFragmentManager, null)
        }

    }
}