package com.gredicer.datetimepicker

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.app.Dialog
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_datetime_picker.*
import java.text.DecimalFormat


class DateTimePickerFragment() : DialogFragment(), ScrollPickerView.OnItemSelectedListener {
    var window: Window? = null
    var exitStatus: Boolean = false


    private var mYearAdapter = DatePickerAdapter(1800, 2200, DecimalFormat("0000"))
    private var mSelectedYear: Int = 0
    private var mMonthAdapter = DatePickerAdapter(0, 12, DecimalFormat("00"))
    private var mSelectedMonth: Int = 0
    private var mDayAdapter = DatePickerAdapter(0, 31, DecimalFormat("00"))
    private var mSelectedDay: Int = 0
    private var mHourAdapter = DatePickerAdapter(0, 23, DecimalFormat("00"))
    private var mSelectedHour: Int = 0
    private var mMinuteAdapter = DatePickerAdapter(0, 59, DecimalFormat("00"))
    private var mSelectedMinute: Int = 0


    companion object {
        const val DateTimePickerTAG = "DateTimePickerFragment"

        fun newInstance(): DateTimePickerFragment {
            return DateTimePickerFragment();
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_datetime_picker, container, false)
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null && dialog!!.window != null) {
            window = dialog!!.window!!
            val params = window!!.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            // 显示在页面的底部
            params.gravity = Gravity.BOTTOM
            window!!.attributes = params
            window!!.setBackgroundDrawableResource(R.drawable.shape_dialog_corners)
            // dialog弹出后会点击屏幕或物理返回键，dialog不消失
            dialog!!.setCancelable(true);
            // dialog弹出后会点击屏幕，dialog不消失；点击物理返回键dialog消失
            dialog!!.setCanceledOnTouchOutside(true);

            enterAnimation()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mOutsideClickDialog = OutsideClickDialog(requireContext(), theme)
        // 监听外部点击
        mOutsideClickDialog.onOutsideClickListener = {
            exitAnimation()
            true
        }
        // 监听返回点击
        mOutsideClickDialog.onBackClickListener = {
            exitAnimation()
            true
        }
        return mOutsideClickDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        date_picker_year.setAdapter(mYearAdapter)
        date_picker_year.setOnItemSelectedListener(this)
        date_picker_month.setAdapter(mMonthAdapter)
        date_picker_month.setOnItemSelectedListener(this)
        date_picker_day.setAdapter(mDayAdapter)
        date_picker_day.setOnItemSelectedListener(this)
        date_picker_hour.setAdapter(mHourAdapter)
        date_picker_hour.setOnItemSelectedListener(this)
        date_picker_minute.setAdapter(mMinuteAdapter)
        date_picker_minute.setOnItemSelectedListener(this)

        btn_enter.setOnClickListener {
            exitAnimation()
        }

    }


    var listener: OnClickListener? = null

    interface OnClickListener {
        fun onClickListener(choose: Int)
    }

    override fun onItemSelected(view: View?, position: Int) {
        mSelectedYear = mYearAdapter.getDate(position)
        mSelectedMonth = mMonthAdapter.getDate(position)
        mSelectedDay = mDayAdapter.getDate(position)
        mSelectedHour = mHourAdapter.getDate(position)
        mSelectedMinute = mMinuteAdapter.getDate(position)
    }

    /**
     * 进入动画
     * */
    private fun enterAnimation() {
        val holder1 = PropertyValuesHolder.ofFloat("scaleX", 1f, 1f)
        val holder2 = PropertyValuesHolder.ofFloat("scaleY", 0f, 1f)
        val deCoverView = window!!.decorView
        deCoverView.pivotY = getScreenHeight(context as Activity).toFloat() / 2
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(deCoverView, holder1, holder2)
        scaleDown.interpolator = OvershootInterpolator(0.7f)
        scaleDown.duration = 200
        scaleDown.start()
    }

    /**
     * 退出动画
     * */
    private fun exitAnimation() {
        if (exitStatus) return
        exitStatus = true

        val params = window!!.attributes
        params.dimAmount = 0.1f
        window!!.attributes = params

        val a = getScreenHeight(context as Activity).toFloat() / 2
        val holder1 = PropertyValuesHolder.ofFloat("scaleX", 1f, 1f)
        val holder2 = PropertyValuesHolder.ofFloat("translationY", 0f, a)
        val deCoverView = window!!.decorView
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(deCoverView, holder1, holder2)
        scaleDown.interpolator = DecelerateInterpolator()
        scaleDown.duration = 200
        scaleDown.start()
        scaleDown.doOnEnd {
            dismiss()
        }

    }

    /**
     * 获取屏幕的宽度
     * */
    private fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    /**
     * 获取屏幕的高度
     * */
    private fun getScreenHeight(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }


}