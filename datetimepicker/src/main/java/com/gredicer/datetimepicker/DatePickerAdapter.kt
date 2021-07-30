package com.gredicer.datetimepicker

import java.text.DecimalFormat

/**
 * 日期选择适配器
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-17
 */
class DatePickerAdapter @JvmOverloads constructor(
    var minValue: Int,
    var maxValue: Int,
    private val mDecimalFormat: DecimalFormat? = null
) :
    PickAdapter {
    override val count: Int
        get() = maxValue - minValue + 1

    override fun getItem(position: Int): String? {
        return if (position in 0 until count) {
            if (mDecimalFormat == null) {
                (minValue + position).toString()
            } else {
                mDecimalFormat.format((minValue + position).toLong())
            }
        } else null
    }

    fun getDate(position: Int): Int {
        return if (position in 0 until count) {
            minValue + position
        } else 0
    }

    fun indexOf(valueString: String): Int {
        val value: Int = try {
            valueString.toInt()
        } catch (e: NumberFormatException) {
            return -1
        }
        return indexOf(value)
    }

    fun indexOf(value: Int): Int {
        return if (value < minValue || value > maxValue) {
            -1
        } else value - minValue
    }

}
