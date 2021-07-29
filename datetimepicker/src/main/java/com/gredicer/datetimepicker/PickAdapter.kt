package com.gredicer.datetimepicker

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-17
 */
interface PickAdapter {
    /**
     * 返回数据总个数
     */
    val count: Int

    /**
     * 返回一条对应index的数据
     */
    fun getItem(position: Int): String?
}