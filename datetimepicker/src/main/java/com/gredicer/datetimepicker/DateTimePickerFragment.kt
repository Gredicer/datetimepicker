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
import java.util.*


class DateTimePickerFragment : DialogFragment(), ScrollPickerView.OnItemSelectedListener {
    private var window: Window? = null

    // 当前模式 0-年月日时分 1-年 2-年月 3-年月日 4-时分
    private var mMode: Int = 0

    // 退出状态
    private var exitStatus: Boolean = false

    // 是否设置初始值
    private var hasSetDefault: Boolean = false

    // 初始时间
    private var mDefaultTime = "2000-01-01 00:00:00"


    private var mYearAdapter = DatePickerAdapter(1900, 2200, DecimalFormat("0000"))
    private var mSelectedYear: Int = 0
    private var mMonthAdapter = DatePickerAdapter(1, 12, DecimalFormat("00"))
    private var mSelectedMonth: Int = 0
    private var mDayAdapter = DatePickerAdapter(1, 31, DecimalFormat("00"))
    private var mSelectedDay: Int = 0
    private var mHourAdapter = DatePickerAdapter(0, 23, DecimalFormat("00"))
    private var mSelectedHour: Int = 0
    private var mMinuteAdapter = DatePickerAdapter(0, 59, DecimalFormat("00"))
    private var mSelectedMinute: Int = 0


    companion object {
        fun newInstance(): DateTimePickerFragment {
            return DateTimePickerFragment()
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
            dialog!!.setCancelable(true)
            // dialog弹出后会点击屏幕，dialog不消失；点击物理返回键dialog消失
            dialog!!.setCanceledOnTouchOutside(true)

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
        // 初始化退出状态为现在可以退出，不在退出状态
        exitStatus = false

        when (mMode) {
            0 -> {
                initYear()
                initMonth()
                initDay()
                initHour()
                initMinute()
            }
            1 -> {
                initYear()
            }
            2 -> {
                initYear()
                initMonth()
                resetUI(2)
            }
            3 -> {
                initYear()
                initMonth()
                initDay()
                resetUI(3)
            }
            4 -> {
                initHour()
                initMinute()
                resetUI(2)
            }
        }



        if (!hasSetDefault) resetTime()

        btn_back_now.setOnClickListener { resetTime() }

        btn_enter.setOnClickListener {
            listener?.onClickListener(returnTime())
            exitAnimation()
        }
    }


    var listener: OnClickListener? = null

    interface OnClickListener {
        fun onClickListener(selectTime: String)
    }

    override fun onItemSelected(view: View?, position: Int) {
        when (view?.id) {
            R.id.date_picker_year -> {
                mSelectedYear = mYearAdapter.getDate(position)
                // 根据年月计算日期的最大值，并刷新
                mDayAdapter.maxValue = getMonthLastDay(mSelectedYear, mSelectedMonth)
            }
            R.id.date_picker_month -> {
                mSelectedMonth = mMonthAdapter.getDate(position)
                // 根据年月计算日期的最大值，并刷新
                mDayAdapter.maxValue = getMonthLastDay(mSelectedYear, mSelectedMonth)
                date_picker_day.setAdapter(mDayAdapter)
            }
            R.id.date_picker_day -> {
                mSelectedDay = mDayAdapter.getDate(position)
            }
            R.id.date_picker_hour -> {
                mSelectedHour = mHourAdapter.getDate(position)
            }
            R.id.date_picker_minute -> {
                mSelectedMinute = mMinuteAdapter.getDate(position)
            }
            else -> {
            }
        }
        showTime()
    }

    /**
     * 增加年的显示
     * */
    fun mode(mode: Int): DateTimePickerFragment {
        mMode = mode
        return this
    }


    /**
     * 设置初始值
     * */
    fun default(defaultTime: String): DateTimePickerFragment {
        mDefaultTime = defaultTime
        hasSetDefault = true
        return this
    }

    /**
     * 初始化年
     * */
    private fun initYear() {
        year_show.visibility = View.VISIBLE
        date_picker_year.setAdapter(mYearAdapter)
        date_picker_year.setOnItemSelectedListener(this)
        setSelectValue(0, mDefaultTime.substring(0, 4).toInt())
    }

    /**
     * 初始化月
     * */
    private fun initMonth() {
        month_show.visibility = View.VISIBLE
        date_picker_month.setAdapter(mMonthAdapter)
        date_picker_month.setOnItemSelectedListener(this)
        setSelectValue(1, mDefaultTime.substring(5, 7).toInt())
    }

    /**
     * 初始化日
     * */
    private fun initDay() {
        day_show.visibility = View.VISIBLE
        date_picker_day.setAdapter(mDayAdapter)
        date_picker_day.setOnItemSelectedListener(this)
        setSelectValue(2, mDefaultTime.substring(8, 10).toInt())
    }

    /**
     * 初始化时
     * */
    private fun initHour() {
        hour_show.visibility = View.VISIBLE
        date_picker_hour.setAdapter(mHourAdapter)
        date_picker_hour.setOnItemSelectedListener(this)
        setSelectValue(3, mDefaultTime.substring(11, 13).toInt())
    }

    /**
     * 初始化分
     * */
    private fun initMinute() {
        minute_show.visibility = View.VISIBLE
        date_picker_minute.setAdapter(mMinuteAdapter)
        date_picker_minute.setOnItemSelectedListener(this)
        setSelectValue(4, mDefaultTime.substring(14, 16).toInt())
    }

    /**
     * 设置当前选择的值
     * type: 0-年，1-月，2-日，3-时，4-分
     * */
    private fun setSelectValue(type: Int, value: Int) {
        when (type) {
            0 -> {
                date_picker_year.setSelectedPosition(mYearAdapter.indexOf(value))
            }
            1 -> {
                date_picker_month.setSelectedPosition(mMonthAdapter.indexOf(value))
            }
            2 -> {
                date_picker_day.setSelectedPosition(mDayAdapter.indexOf(value))
            }
            3 -> {
                date_picker_hour.setSelectedPosition(mHourAdapter.indexOf(value))
            }
            4 -> {
                date_picker_minute.setSelectedPosition(mMinuteAdapter.indexOf(value))
            }
        }
    }

    /**
     * 重置UI
     * */
    private fun resetUI(showCount: Int) {
        when (showCount) {
            2 -> {
                fl_datetimepicker.setPadding(200, 0, 200, 0)
            }
            3 -> {
                fl_datetimepicker.setPadding(100, 0, 100, 0)
            }
        }
    }

    /**
     * 文字显示当前的时间
     * */
    private fun showTime() {
        var showText = ""
        when (mMode) {
            0 -> {
                showText += "$mSelectedYear 年"
                showText += " ${formatTime(mSelectedMonth)} 月"
                showText += " ${formatTime(mSelectedDay)} 日   "
                showText += "${formatTime(mSelectedHour)} :"
                showText += " ${formatTime(mSelectedMinute)}"
            }
            1 -> {
                showText = "$mSelectedYear 年"
            }
            2 -> {
                showText = "$mSelectedYear 年 ${formatTime(mSelectedMonth)} 月"
            }
            3 -> {
                showText += "$mSelectedYear 年"
                showText += " ${formatTime(mSelectedMonth)} 月"
                showText += " ${formatTime(mSelectedDay)} 日"
            }
            4 -> {
                showText = "${formatTime(mSelectedHour)}:${formatTime(mSelectedMinute)}"
            }
        }
        tv_time_show.text = showText
    }

    /**
     * 返回的时间
     * */
    private fun returnTime(): String {
        var text = ""
        when (mMode) {
            0 -> {
                text += "$mSelectedYear-"
                text += "${formatTime(mSelectedMonth)}-"
                text += "${formatTime(mSelectedDay)} "
                text += "${formatTime(mSelectedHour)}:"
                text += formatTime(mSelectedMinute)
            }
            1 -> {
                text = "$mSelectedYear"
            }
            2 -> {
                text = "$mSelectedYear-${formatTime(mSelectedMonth)}"
            }
            3 -> {
                text = "$mSelectedYear-${formatTime(mSelectedMonth)}-${formatTime(mSelectedDay)}"
            }
            4 -> {
                text = "${formatTime(mSelectedHour)}:${formatTime(mSelectedMinute)}"
            }
        }
        return text
    }

    /**
     * 格式化时间
     **/
    private fun formatTime(value: Int): String {
        return DecimalFormat("00").format(value)
    }


    /**
     * 重置到现在的时间
     * */
    private fun resetTime() {
        val calendar = Calendar.getInstance()
        // 年
        val year = calendar.get(Calendar.YEAR)
        // 月
        val month = calendar.get(Calendar.MONTH) + 1
        // 日
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        // 小时
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        // 分钟
        val minute = calendar.get(Calendar.MINUTE)
        mDayAdapter.maxValue = getMonthLastDay(year, month)
        setSelectValue(0, year)
        setSelectValue(1, month)
        setSelectValue(2, day)
        setSelectValue(3, hour)
        setSelectValue(4, minute)
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


    /**
     * 得到指定月的天数
     */
    private fun getMonthLastDay(year: Int, month: Int): Int {
        val a = Calendar.getInstance()
        a[Calendar.YEAR] = year
        a[Calendar.MONTH] = month - 1
        a[Calendar.DATE] = 1 //把日期设置为当月第一天
        a.roll(Calendar.DATE, -1) //日期回滚一天，也就是最后一天
        return a[Calendar.DATE]
    }

}