package com.jocoo.flipclock

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.FontMetrics
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import java.util.*

class PathEffectView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    private var metrics: FontMetrics
    private var mAnimator: ValueAnimator = ValueAnimator.ofFloat(-0f, -180f)
    private val mMatrix: Matrix = Matrix()
    private var mCamera: Camera = Camera()
    private var mTextBounds: Rect
    private val mWidth = 216f
    private var mOriginX = 0f
    private val mOriginY = 300f
    private val mPadding = 30f
    private val mPanelSpace = 30f
    private val mRadius = 50f
    private val mBackColor = Color.DKGRAY
    private val mFontColor = Color.GREEN
    private val mGap = 3f
    private val mAnimationDuration = 900L
    private var mOffsetX = 0f
    private var mOffsetY = 0f
    private var mSecondValue = 0
    private var mMinuteValue = 0
    private var mHourValue = 0
    private val mRectPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/vtks distress.ttf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/LeagueGothic-CondensedRegular.otf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/BrompthonChoconuts-9Yg60.otf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/BarlowCondensed-SemiBold.ttf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/BEBAS-G2.ttf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/Bondi-2.ttf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/Chivo-G.ttf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/Comfortaa-G.ttf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/LeagueSpartan-G.ttf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/Library-3-am-G.ttf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/Oswald-Medium.ttf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/Playlist-G.ttf")
//        it.typeface = Typeface.createFromAsset(context.assets, "fonts/Public-G.ttf")
        it.typeface = Typeface.createFromAsset(context.assets, "fonts/Salt-G.ttf")
        it.textAlign = Paint.Align.CENTER
    }

    init {
        mAnimator.repeatMode = ValueAnimator.RESTART
        mAnimator.interpolator = AccelerateDecelerateInterpolator()
        mAnimator.duration = mAnimationDuration
        mAnimator.addUpdateListener {
            invalidate()
        }
        computeTextSize()
        val nine = "9"
        mTextBounds = Rect()
        mRectPaint.getTextBounds(nine, 0, nine.length, mTextBounds)
        metrics = mRectPaint.fontMetrics
        schedule()
    }

    private fun schedule() {
        post {
            currentTime()
            mAnimator.resume()
            mAnimator.start()
            postDelayed({ schedule() }, 1000)
        }
    }

    private fun currentTime() {
        mSecondValue = Calendar.getInstance().get(Calendar.SECOND)
        mMinuteValue = Calendar.getInstance().get(Calendar.MINUTE)
        mHourValue = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    private fun computeTextSize() {
        var textSize = 5f
        val bounds = Rect()
        while (true) {
            mRectPaint.textSize = textSize
            mRectPaint.getTextBounds("99", 0, 2, bounds)
            if (bounds.width() <= mWidth - mPadding && bounds.height() <= mWidth - mPadding) {
                textSize += 0.3f
            } else {
                break
            }
        }
        mRectPaint.textSize = textSize
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mOriginX = (measuredWidth - mPanelSpace * 2 - mWidth * 3) * 0.5f
    }

    private fun updateHourText(v: Int, twentyFourHour: Boolean = true): String {
        val value = v % if (twentyFourHour) 24 else 12
        return if (value < 10) {
            "0${value}"
        } else {
            "$value"
        }
    }

    private fun updateMinuteOrSecondText(v: Int): String {
        val value = v % 60
        return if (value < 10) {
            "0${value}"
        } else {
            "$value"
        }
    }

    private fun drawFontMetricsLines(canvas: Canvas?, baseLineOffset: Float) {
        canvas?.let {
            it.save()
            mRectPaint.color = Color.RED
            it.drawLine(
                0f,
                baseLineOffset + metrics.descent,
                measuredWidth * 1f,
                baseLineOffset + metrics.descent,
                mRectPaint
            )
            mRectPaint.color = Color.YELLOW
            it.drawLine(
                0f,
                baseLineOffset,
                measuredWidth * 1f,
                baseLineOffset,
                mRectPaint
            )
            mRectPaint.color = Color.BLUE
            it.drawLine(
                0f,
                baseLineOffset + metrics.ascent,
                measuredWidth * 1f,
                baseLineOffset + metrics.ascent,
                mRectPaint
            )
            mRectPaint.color = Color.CYAN
            val bounds = Rect()
            mRectPaint.getTextBounds(updateMinuteOrSecondText(mSecondValue), 0, 2, bounds)
            it.drawLine(
                0f,
                baseLineOffset + bounds.top,
                measuredWidth * 1f,
                baseLineOffset + bounds.top,
                mRectPaint
            )
            it.drawLine(
                0f,
                baseLineOffset + bounds.bottom,
                measuredWidth * 1f,
                baseLineOffset + bounds.bottom,
                mRectPaint
            )
            it.restore()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

//        drawGuideLine(canvas)

        drawClockItem(
            canvas, mOriginX, mOriginY, updateHourText(mHourValue),
            updateMinuteOrSecondText(if (mMinuteValue == 59 && mSecondValue == 59) mHourValue + 1 else mHourValue)
        )
        drawClockItem(
            canvas,
            mOriginX + mWidth + mPanelSpace,
            mOriginY,
            updateMinuteOrSecondText(mMinuteValue),
            updateMinuteOrSecondText(if (mSecondValue == 59) mMinuteValue + 1 else mMinuteValue)
        )
        drawClockItem(
            canvas,
            mOriginX + (mWidth + mPanelSpace) * 2,
            mOriginY,
            updateMinuteOrSecondText(mSecondValue),
            updateMinuteOrSecondText(mSecondValue + 1)
        )
    }

    // draw guide lines
    private fun drawGuideLine(canvas: Canvas?) {
        canvas?.let {
            it.save()
            drawFontMetricsLines(canvas, measuredHeight * 0.5f)
            drawFontMetricsLines(canvas, mOffsetY)
            it.drawText(
                updateMinuteOrSecondText(mSecondValue),
                measuredWidth * 0.5f,
                measuredHeight * 0.5f,
                mRectPaint
            )
            it.restore()
        }
    }

    private fun drawClockItem(
        canvas: Canvas?,
        x: Float,
        y: Float,
        curText: String,
        nextText: String
    ) {
        val animate = curText != nextText
        val offsetX = x + mWidth / 2
        val offsetY = y + mWidth / 2 - mTextBounds.centerY()
        // next top part
        canvas?.let {
            it.save()
            it.clipRect(x, y, x + mWidth, y + mWidth / 2 - mGap)
            mRectPaint.color = mBackColor
            it.drawRoundRect(
                x,
                y,
                x + mWidth,
                y + mWidth,
                mRadius,
                mRadius,
                mRectPaint
            )
            mRectPaint.color = mFontColor
            it.drawText(nextText, offsetX, offsetY, mRectPaint)
            it.restore()
        }

        // current bottom part
        canvas?.let {
            it.save()
            mRectPaint.color = mBackColor
            it.clipRect(x, y + mWidth / 2, x + mWidth, y + mWidth)
            it.drawRoundRect(
                x,
                y,
                x + mWidth,
                y + mWidth,
                mRadius,
                mRadius,
                mRectPaint
            )
            mRectPaint.color = mFontColor
            mRectPaint.textAlign
            it.drawText(curText, offsetX, offsetY, mRectPaint)
            it.restore()
        }

        val angle = mAnimator.animatedValue as Float
        canvas?.let {
            it.save()
            mCamera.save()
            if (angle >= -90f) {
                mCamera.rotateX(angle)
            } else {
                mCamera.rotateX(angle + 180f)
            }
            mCamera.getMatrix(mMatrix)
            mMatrix.postTranslate(x + mWidth / 2, y + mWidth / 2)
            mMatrix.preTranslate(-(x + mWidth / 2), -(y + mWidth / 2))
            mCamera.restore()
            if (animate) {
                it.concat(mMatrix)
            }
            if (angle >= -90f) { // current top flipping part
                it.clipRect(x, y, x + mWidth, y + mWidth / 2 - mGap)
                mRectPaint.color = mBackColor
                it.drawRoundRect(
                    x,
                    y,
                    x + mWidth,
                    y + mWidth,
                    mRadius,
                    mRadius,
                    mRectPaint
                )
                mRectPaint.color = mFontColor
                it.drawText(curText, offsetX, offsetY, mRectPaint)
            } else { // next bottom flipping part
                it.clipRect(x, y + mWidth / 2, x + mWidth, y + mWidth)
                mRectPaint.color = mBackColor
                it.drawRoundRect(
                    x,
                    y,
                    x + mWidth,
                    y + mWidth,
                    mRadius,
                    mRadius,
                    mRectPaint
                )
                mRectPaint.color = mFontColor
                it.drawText(nextText, offsetX, offsetY, mRectPaint)
            }
            it.restore()
        }
    }
}