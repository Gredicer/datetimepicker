package com.gredicer.datetimepicker

import android.app.Dialog
import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewConfiguration

/**
 * 提供返回事件，外部点击事件
 */
class OutsideClickDialog(context: Context, themeResId: Int) : Dialog(context, themeResId) {

    private val mCancelable = true

    var onBackClickListener: (() -> Boolean)? = null
    var onOutsideClickListener: (() -> Boolean)? = null

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val consume = onBackClickListener?.invoke()
            if (consume == true) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mCancelable && isShowing &&
            (event.action == MotionEvent.ACTION_UP && isOutOfBounds(context, event) ||
                    event.action == MotionEvent.ACTION_OUTSIDE)
        ) {
            val consume = onOutsideClickListener?.invoke()
            if (consume == true) {
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isOutOfBounds(context: Context, event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        val slop = ViewConfiguration.get(context).scaledWindowTouchSlop
        val decorView = window?.decorView
        return (x < -slop || y < -slop
                || x > decorView?.width!! + slop
                || y > decorView.height + slop)
    }
}