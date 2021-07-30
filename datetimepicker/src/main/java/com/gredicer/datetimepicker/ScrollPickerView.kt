package com.gredicer.datetimepicker

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.*
import kotlin.math.abs


/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-11
 */
class ScrollPickerView : View, AnimatorUpdateListener {
    /**
     * dp&sp转px的系数
     */
    private var mDensityDP = 0f
    private var mDensitySP = 0f

    /**
     * LayoutParams宽度
     */
    private var mLayoutWidth = 0

    /**
     * LayoutParams高度
     */
    private var mLayoutHeight = 0

    /**
     * 显示行数，仅高度为wrap_content时有效。默认值5
     */
    private var mTextRows = 0

    /**
     * 文本的行高
     */
    private var mRowHeight = 0f

    /**
     * 文本的行距。默认4dp
     */
    private var mRowSpacing = 0f

    /**
     * item的高度，等于mRowHeight+mRowSpacing
     */
    private var mItemHeight = 0f

    /**
     * 字体大小。默认16sp
     */
    private var mTextSize = 0f

    /**
     * 选中项的缩放比例。默认2
     */
    private var mTextRatio = 0f

    /**
     * 文本格式，当宽为wrap_content时用于计算宽度
     */
    private var mTextFormat: String? = null

    /**
     * 中部字体颜色
     */
    private var mTextColor_Center = 0

    /**
     * 外部字体颜色
     */
    private var mTextColor_Outside = 0

    /**
     * 是否开启循环
     */
    private var mLoopEnable = false

    /**
     * 中部item的position
     */
    private var mMiddleItemPostion = 0

    /**
     * 中部item的偏移量，取值范围( -mItenHeight/2F , mItenHeight/2F ]
     */
    private var mMiddleItemOffset = 0f

    /**
     * 绘制区域中点的Y坐标
     */
    private var mCenterY = 0f

    /**
     * 总的累计偏移量，指针上移，position增大，偏移量增加
     */
    private var mTotalOffset = 0f

    /**
     * 文本对齐方式
     */
    private var mGravity = 0

    /**
     * 文本绘制起始点的X坐标
     */
    private var mDrawingOriginX = 0f

    /**
     * 存储每行文本边界值，用于计算文本的高度
     */
    private var mTextBounds: Rect? = null

    /**
     * 记录触摸事件的Y坐标
     */
    private var mStartY = 0f

    /**
     * 触摸移动最小距离
     */
    private var mTouchSlop = 0

    /**
     * 触摸点的ID
     */
    private var mTouchPointerId = 0

    /**
     * 是否触摸移动（手指在屏幕上拖动）
     */
    private var isMoveAction = false

    /**
     * 是否切换了触摸点（多点触摸中的手指切换）
     */
    private var isSwitchTouchPointer = false

    /**
     * 用于记录指定的position
     */
    private var mSpecifyPosition: Int? = null
    private var mMatrix: Matrix? = null

    /**
     * 减速动画
     */
    private var mDecelerateAnimator: DecelerateAnimator? = null

    /**
     * 线性颜色选择器
     */
    private var mLinearShader: LinearGradient? = null

    /**
     * 速度追踪器，结束触摸事件时计算手势速度，用于减速动画
     */
    private var mVelocityTracker: VelocityTracker? = null
    private var mTextPaint: TextPaint? = null
    private var mAdapter: PickAdapter? = null
    private var mItemSelectedListener: OnItemSelectedListener? = null

    interface OnItemSelectedListener {
        /**
         * 选中时的回调
         */
        fun onItemSelected(view: View?, position: Int)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attributeSet: AttributeSet?) {
        mDensityDP = context.resources.displayMetrics.density //DP密度
        mDensitySP = context.resources.displayMetrics.scaledDensity //SP密度
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ScrollPickerView)
        mTextRows = typedArray.getInteger(R.styleable.ScrollPickerView_scrollpicker_rows, 5)
        mTextSize = typedArray.getDimension(
            R.styleable.ScrollPickerView_scrollpicker_textSize,
            16 * mDensitySP
        )
        mTextRatio = typedArray.getFloat(R.styleable.ScrollPickerView_scrollpicker_textRatio, 2f)
        mRowSpacing = typedArray.getDimension(R.styleable.ScrollPickerView_scrollpicker_spacing, 0f)
        mTextFormat = typedArray.getString(R.styleable.ScrollPickerView_scrollpicker_textFormat)
        mTextColor_Center = typedArray.getColor(
            R.styleable.ScrollPickerView_scrollpicker_textColor_center,
            -0x2277de
        )
        mTextColor_Outside = typedArray.getColor(
            R.styleable.ScrollPickerView_scrollpicker_textColor_outside,
            -0x2267
        )
        mLoopEnable = typedArray.getBoolean(R.styleable.ScrollPickerView_scrollpicker_loop, true)
        mGravity =
            typedArray.getInt(R.styleable.ScrollPickerView_scrollpicker_gravity, GRAVITY_LEFT)
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        typedArray.recycle()

        //初始化画笔工具
        initTextPaint()
        //计算行高
        measureTextHeight()
        mMatrix = Matrix() //用户记录偏移量并设置给颜色渐变工具
        mTextBounds = Rect() //用于计算每行文本边界区域
        //减速动画
        mDecelerateAnimator = DecelerateAnimator(context)
        mDecelerateAnimator!!.addUpdateListener(this)
    }

    /**
     * 初始化画笔工具
     */
    private fun initTextPaint() {
        mTextPaint = TextPaint()
        //防抖动
        mTextPaint!!.isDither = true
        //抗锯齿
        mTextPaint!!.isAntiAlias = true
        //不要文本缓存
        mTextPaint!!.isLinearText = true
        //设置亚像素
        mTextPaint!!.isSubpixelText = true
        //字体加粗
        mTextPaint!!.isFakeBoldText = true

        //设置字体大小
        mTextPaint!!.textSize = mTextSize
        //等宽字体
        mTextPaint!!.typeface = Typeface.MONOSPACE
        when (mGravity) {
            GRAVITY_LEFT -> {
                mTextPaint!!.textAlign = Paint.Align.LEFT
            }
            GRAVITY_CENTER -> {
                mTextPaint!!.textAlign = Paint.Align.CENTER
            }
            GRAVITY_RIGHT -> {
                mTextPaint!!.textAlign = Paint.Align.RIGHT
            }
        }
    }

    /**
     * 计算行高
     */
    private fun measureTextHeight() {
        val fontMetrics = mTextPaint!!.fontMetrics
        //确定行高
        mRowHeight =
            abs(fontMetrics.descent - fontMetrics.ascent) * if (mTextRatio > 1) mTextRatio else 1f
        //行距不得小于负行高的一半
        if (mRowSpacing < -mRowHeight / 2f) {
            mRowSpacing = -mRowHeight / 2f
        }
        mItemHeight = mRowHeight + mRowSpacing
    }

    fun setOnItemSelectedListener(itemSelectedListener: OnItemSelectedListener?) {
        mItemSelectedListener = itemSelectedListener
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams) {
        mLayoutWidth = params.width
        mLayoutHeight = params.height
        super.setLayoutParams(params)
    }

    /**
     * 计算PickerView的高宽，会多次调用，包括隐藏导航键也会调用
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT && widthMode != MeasureSpec.EXACTLY) { //宽为WRAP
            widthSize = if (mTextFormat != null) {
                Math.ceil((mTextPaint!!.measureText(mTextFormat) * if (mTextRatio > 1) mTextRatio else 1f).toDouble())
                    .toInt() + paddingLeft + paddingRight
            } else {
                paddingLeft + paddingRight
            }
        }
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT && heightMode != MeasureSpec.EXACTLY) { //高为WRAP
            heightSize =
                Math.ceil((mRowHeight * mTextRows + mRowSpacing * (mTextRows - mTextRows % 2)).toDouble())
                    .toInt() + paddingTop + paddingBottom
        }
        setMeasuredDimension(
            resolveSize(widthSize, widthMeasureSpec),
            resolveSize(heightSize, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measureOriginal() //计算中心位置、绘制起点
        setPaintShader() //设置颜色线性渐变
    }

    /**
     * 计算中心位置、绘制起点
     */
    private fun measureOriginal() {
        //计算绘制区域高度
        val drawHeight = height - paddingTop - paddingBottom
        //计算中心的Y值
        mCenterY = drawHeight / 2f + paddingTop
        when (mGravity) {
            GRAVITY_LEFT -> {
                mDrawingOriginX = paddingLeft.toFloat()
            }
            GRAVITY_CENTER -> {
                mDrawingOriginX = (width + paddingLeft - paddingRight) / 2f
            }
            GRAVITY_RIGHT -> {
                mDrawingOriginX = (width - paddingRight).toFloat()
            }
        }
    }

    /**
     * 设置颜色线性渐变
     */
    private fun setPaintShader() {
        mLinearShader = LinearGradient(
            0f,
            mCenterY - (0.5f * mRowHeight + mItemHeight),
            0f,
            mCenterY + (0.5f * mRowHeight + mItemHeight),
            intArrayOf(mTextColor_Outside, mTextColor_Center, mTextColor_Outside),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        mTextPaint!!.shader = mLinearShader
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mAdapter == null) {
            return super.onTouchEvent(event)
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        val actionIndex = event.actionIndex
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isSwitchTouchPointer = false
                //当前有减速动画未结束，则取消该动画，并直接进入滑动状态
                if (mDecelerateAnimator!!.isStarted) {
                    isMoveAction = true
                    mDecelerateAnimator!!.cancel()
                } else {
                    isMoveAction = false
                }
                //记录偏移坐标
                mStartY = event.getY(actionIndex)
                //记录当前控制指针ID
                mTouchPointerId = event.getPointerId(actionIndex)
            }
            MotionEvent.ACTION_POINTER_UP -> {

                //如果抬起的指针是当前控制指针，则进行切换
                if (event.getPointerId(actionIndex) == mTouchPointerId) {
                    mVelocityTracker!!.clear()
                    //从列表中选择一个指针（非当前抬起的指针）作为下一个控制指针
                    var index = 0
                    while (index < event.pointerCount) {
                        if (index != actionIndex) {
                            //重置偏移坐标
                            mStartY = event.getY(index)
                            //重置触摸ID
                            mTouchPointerId = event.getPointerId(index)
                            //标记进行过手指切换
                            isSwitchTouchPointer = true
                            break
                        }
                        index++
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {

                //只响应当前控制指针的移动操作
                var index = 0
                while (index < event.pointerCount) {
                    if (event.getPointerId(index) == mTouchPointerId) {
                        //计算偏移量，指针上移偏移量为正
                        val offset = mStartY - event.getY(index)
                        if (isMoveAction) {
                            //已是滑动状态，累加偏移量，记录偏移坐标，请求重绘
                            mTotalOffset += offset
                            mStartY = event.getY(index)
                            super.invalidate()
                        } else if (Math.abs(offset) >= mTouchSlop) {
                            //进入滑动状态，重置偏移坐标，标记当前为滑动状态
                            mStartY = event.getY(index)
                            isMoveAction = true
                        }
                        break
                    }
                    index++
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                //计算偏移量，指针上移偏移量为正
                val offset = mStartY - event.getY(actionIndex)
                if (isMoveAction) {
                    isMoveAction = false
                    //计算手势速度
                    mVelocityTracker!!.computeCurrentVelocity(1500)
                    val velocityY = -mVelocityTracker!!.getYVelocity(mTouchPointerId)
                    //累加偏移量
                    mTotalOffset += offset
                    //开启减速动画
                    startDecelerateAnimator(mTotalOffset, velocityY, 0f, mItemHeight)
                } else if (!isSwitchTouchPointer && Math.abs(offset) < mTouchSlop) {
                    //计算触摸点相对于中心位置的偏移距离
                    val distance = event.getY(actionIndex) - mCenterY
                    //开启减速动画
                    startDecelerateAnimator(mTotalOffset, 0f, distance, mItemHeight)
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker!!.recycle()
                    mVelocityTracker = null
                }
            }
        }
        return true
    }

    /**
     * 开始减速动画
     *
     * @param startValue 初始位移值
     * @param velocity   初始速度
     * @param distance   移动距离
     * @param modulus    距离的模
     */
    private fun startDecelerateAnimator(
        startValue: Float,
        velocity: Float,
        distance: Float,
        modulus: Float
    ) {
        val minValue = -1f
        val maxValue: Float = if (mLoopEnable) -1f else (mAdapter!!.count - 1) * mItemHeight + 1
        if (distance != 0f) {
            mDecelerateAnimator!!.startAnimator_Distance(
                startValue,
                minValue,
                maxValue,
                distance,
                modulus
            )
        } else {
            mDecelerateAnimator!!.startAnimator_Velocity(
                startValue,
                minValue,
                maxValue,
                velocity,
                modulus
            )
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        mTotalOffset = animation.animatedValue as Float
        super.invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (!isInEditMode && mAdapter == null) {
            return
        }
        val measuredWidth = width
        val measuredHeight = height
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom

        //根据padding限定绘制区域
        canvas.clipRect(
            paddingLeft,
            paddingTop,
            measuredWidth - paddingRight,
            measuredHeight - paddingBottom
        )

        //计算中部item的position及偏移量
        calculateMiddleItem()
        //绘制上半部分的item
        var curPosition = mMiddleItemPostion - 1
        var curOffset = mCenterY + mMiddleItemOffset - mRowHeight / 2f - mItemHeight
        while (curOffset > paddingTop - mRowHeight) {
            //绘制文本
            drawText(canvas, curPosition, curOffset)
            curOffset -= mItemHeight
            curPosition--
        }

        //绘制中部及下半部分的item
        curPosition = mMiddleItemPostion
        curOffset = mCenterY + mMiddleItemOffset - mRowHeight / 2f
        while (curOffset < measuredHeight - paddingBottom) {
            //绘制文本
            drawText(canvas, curPosition, curOffset)
            //下一个
            curOffset += mItemHeight
            curPosition++
        }
        //动画结束，进行选中回调
        if (!isMoveAction && !mDecelerateAnimator!!.isStarted && mItemSelectedListener != null) {
            //回调监听
            mItemSelectedListener!!.onItemSelected(this, mMiddleItemPostion)
        }
    }

    /**
     * 根据总偏移量计算中部item的偏移量及position
     * 偏移量的取值范围为(-mItenHeight/2F , mItenHeight/2F]
     */
    private fun calculateMiddleItem() {
        //计算偏移了多少个完整item
        var count =
            if (mSpecifyPosition != null) mSpecifyPosition!! else (mTotalOffset / mItemHeight).toInt()
        if (mSpecifyPosition != null) {
            if (mDecelerateAnimator!!.isStarted) {
                mDecelerateAnimator!!.cancel()
            }
            mTotalOffset = mSpecifyPosition!! * mItemHeight
            mMiddleItemOffset = 0f
            mSpecifyPosition = null
        } else {
            //对偏移量取余，注意这里不用取余运算符，因为可能造成严重错误！
            val offsetRem = mTotalOffset - mItemHeight * count //取值范围( -mItenHeight , mItenHeight )
            mMiddleItemOffset = if (offsetRem >= mItemHeight / 2f) {
                count++
                mItemHeight - offsetRem
            } else if (offsetRem >= -mItemHeight / 2f) {
                -offsetRem
            } else {
                count--
                -mItemHeight - offsetRem
            }
        }
        //对position取模
        mMiddleItemPostion = getRealPosition(count)
        //如果停止触摸且动画结束，对最终值和偏移量进行校正
        if (!isMoveAction && !mDecelerateAnimator!!.isStarted) {
            if (mMiddleItemPostion < 0 || mAdapter == null || mAdapter!!.count < 1) {
                mMiddleItemPostion = 0
            } else if (mMiddleItemPostion >= mAdapter!!.count) {
                mMiddleItemPostion = mAdapter!!.count - 1
            }
            mTotalOffset = mMiddleItemPostion * mItemHeight
        }
    }

    /**
     * 绘制文本
     */
    private fun drawText(canvas: Canvas, position: Int, offsetY: Float) {
        //对position取模
        var position = position
        position = getRealPosition(position)
        //position未越界
        if (isInEditMode || position >= 0 && position < mAdapter!!.count) {
            //获取文本
            val text = getDrawingText(position)
            if (text != null) {
                canvas.save()
                //平移画布
                canvas.translate(0f, offsetY)
                //操作线性颜色渐变
                mMatrix!!.setTranslate(0f, -offsetY)
                mLinearShader!!.setLocalMatrix(mMatrix)
                //计算缩放比例
                val scaling = getScaling(offsetY)
                canvas.scale(scaling, scaling, mDrawingOriginX, mRowHeight / 2f)
                //获取文本尺寸
                mTextPaint!!.getTextBounds(text, 0, text.length, mTextBounds)
                //根据文本尺寸计算基线位置
                val baseLineY = (mRowHeight - mTextBounds!!.top - mTextBounds!!.bottom) / 2f
                //绘制文本
                canvas.drawText(text, mDrawingOriginX, baseLineY, mTextPaint!!)
                canvas.restore()
            }
        }
    }

    /**
     * 循环模式下对position取模
     */
    private fun getRealPosition(position: Int): Int {
        var position = position
        if (mLoopEnable && mAdapter != null && mAdapter!!.count > 0) {
            position %= mAdapter!!.count
            if (position < 0) {
                position += mAdapter!!.count
            }
        }
        return position
    }

    /**
     * 根据获取要绘制的文本内容
     */
    private fun getDrawingText(position: Int): String? {
        if (isInEditMode) {
            return if (mTextFormat != null) mTextFormat else ("item$position").toString()
        }
        return if (position >= 0 && position < mAdapter!!.count) {
            mAdapter!!.getItem(position)
        } else null
    }

    /**
     * 根据偏移量计算缩放比例
     */
    private fun getScaling(offsetY: Float): Float {
        val abs = Math.abs(offsetY + mRowHeight / 2f - mCenterY)
        return if (abs < mItemHeight) {
            (1 - abs / mItemHeight) * (mTextRatio - 1f) + 1f
        } else {
            1f
        }
    }

    /**
     * 设置适配器
     */
    fun setAdapter(adapter: PickAdapter?) {
        mAdapter = adapter
        super.invalidate()
    }

    /**
     * 设置当前选中项
     */
    fun setSelectedPosition(position: Int) {
        if (mAdapter == null) return
        if (position < 0 || position >= mAdapter!!.count) {
            throw ArrayIndexOutOfBoundsException()
        }
        if (mDecelerateAnimator!!.isStarted) {
            mDecelerateAnimator!!.cancel()
        }
        // 如果在onMeasure之前设置选中项，mItemHeight为0，无法得到正确偏移量，因此这里不能直接计算mTotalOffset
        mSpecifyPosition = position
        super.invalidate()
    }

    /**
     * 获取当前选中项
     */
    fun getSelectedPosition(): Int {
        return if (isMoveAction || mAdapter == null || mDecelerateAnimator!!.isStarted) {
            -1
        } else mMiddleItemPostion
    }


    /**
     * 设置文本对齐方式，计算文本绘制起始点的X坐标
     */
    fun setGravity(gravity: Int) {
        when (gravity) {
            GRAVITY_LEFT -> {
                mTextPaint!!.textAlign = Paint.Align.LEFT
                mDrawingOriginX = paddingLeft.toFloat()
            }
            GRAVITY_CENTER -> {
                mTextPaint!!.textAlign = Paint.Align.CENTER
                mDrawingOriginX = (width + paddingLeft - paddingRight) / 2f
            }
            GRAVITY_RIGHT -> {
                mTextPaint!!.textAlign = Paint.Align.RIGHT
                mDrawingOriginX = (width - paddingRight).toFloat()
            }
            else -> return
        }
        mGravity = gravity
        super.invalidate()
    }

    fun isLoopEnable(): Boolean {
        return mLoopEnable
    }

    fun setLoopEnable(enable: Boolean) {
        if (mLoopEnable != enable) {
            mLoopEnable = enable
            //循环将关闭且正在减速动画
            if (!mLoopEnable && mDecelerateAnimator!!.isStarted && mAdapter != null) {
                //停止减速动画，并指定position以确保item对齐
                mDecelerateAnimator!!.cancel()
                //防止position越界
                mSpecifyPosition =
                    if (mMiddleItemPostion < 0) 0 else if (mMiddleItemPostion >= mAdapter!!.count) mAdapter!!.count - 1 else mMiddleItemPostion
            }
            super.invalidate()
        }
    }

    /**
     * 设置文本显示的行数，仅当高为WRAP_CONTENT时有效
     */
    fun setTextRows(rows: Int) {
        if (mTextRows != rows) {
            mTextRows = rows
            if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                super.requestLayout()
            }
        }
    }

    /**
     * 设置文本字体大小，单位px
     *
     * @param textSize 必须大于0
     */
    fun setTextSize(textSize: Float) {
        if (textSize > 0 && mTextSize != textSize) {
            mTextSize = textSize
            mTextPaint!!.textSize = mTextSize
            measureTextHeight()
            reInvalidate()
        }
    }

    /**
     * 设置文本行间距，单位px
     */
    fun setRowSpacing(rowSpacing: Float) {
        if (mRowSpacing != rowSpacing) {
            mRowSpacing = rowSpacing
            measureTextHeight()
            reInvalidate()
        }
    }

    /**
     * 设置放大倍数
     */
    fun setTextRatio(textRatio: Float) {
        if (mTextRatio != textRatio) {
            mTextRatio = textRatio
            measureTextHeight()
            reInvalidate()
        }
    }

    /**
     * 设置中部字体颜色
     */
    fun setCenterTextColor(color: Int) {
        if (mTextColor_Center != color) {
            mTextColor_Center = color
            setPaintShader() //设置颜色线性渐变
            invalidate()
        }
    }

    /**
     * 设置外部字体颜色
     */
    fun setOutsideTextColor(color: Int) {
        if (mTextColor_Outside != color) {
            mTextColor_Outside = color
            setPaintShader() //设置颜色线性渐变
            invalidate()
        }
    }

    private fun reInvalidate() {
        if (mDecelerateAnimator!!.isStarted) {
            mDecelerateAnimator!!.cancel()
        }
        mSpecifyPosition = mMiddleItemPostion
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout()
        } else {
            super.invalidate()
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return true
    }

    companion object {
        /**
         * 文本对齐方式，居左
         */
        const val GRAVITY_LEFT = 3

        /**
         * 文本对齐方式，居右
         */
        const val GRAVITY_RIGHT = 5

        /**
         * 文本对齐方式，居中
         */
        const val GRAVITY_CENTER = 17
    }
}
