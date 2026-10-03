package com.example.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.example.data.model.CircleSettings
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

@SuppressLint("ViewConstructor")
class CircleHomeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface Listener {
        fun onSingleTap()
        fun onDoubleTap()
        fun onLongPress()
        fun onPositionChanged(x: Int, y: Int)
    }

    var listener: Listener? = null

    // Settings
    var settings: CircleSettings = CircleSettings()
        set(value) {
            field = value
            updatePaints()
            resetIdleTimer()
            invalidate()
        }

    // WindowManager interaction
    var windowManager: WindowManager? = null
    var layoutParams: WindowManager.LayoutParams? = null

    // Paints
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(60, 0, 0, 0)
    }

    // Touch and Drag tracking
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    // Gesture detection
    private val handler = Handler(Looper.getMainLooper())
    private var isPressedDown = false
    private var scaleFactor = 1.0f
    private var lastTapTime = 0L
    private val doubleTapTimeout = 280L
    private val longPressTimeout = 500L
    private var pendingSingleTapRunnable: Runnable? = null
    private var longPressRunnable: Runnable? = null
    private var hasTriggeredLongPress = false

    // Idle fade animation
    private val idleTimeoutMs = 3000L
    private var isIdle = false
    private var currentAlpha = 0.85f
    private var alphaAnimator: ValueAnimator? = null
    private val idleFadeRunnable = Runnable {
        if (settings.autoFade && !isPressedDown) {
            animateAlphaTo(settings.opacityIdle)
            isIdle = true
        }
    }

    // Screen bounds for snapping
    private val screenWidth: Int
        get() = resources.displayMetrics.widthPixels
    private val screenHeight: Int
        get() = resources.displayMetrics.heightPixels

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        isClickable = true
        isFocusable = false
        updatePaints()
        currentAlpha = settings.opacityActive
        alpha = currentAlpha
        resetIdleTimer()
    }

    private fun updatePaints() {
        val baseColor = settings.colorHex.toInt()
        val r = Color.red(baseColor)
        val g = Color.green(baseColor)
        val b = Color.blue(baseColor)

        when (settings.circleStyle) {
            CircleSettings.STYLE_SOLID -> {
                fillPaint.color = Color.rgb(r, g, b)
                strokePaint.color = Color.argb(120, (r * 0.8f).toInt(), (g * 0.8f).toInt(), (b * 0.8f).toInt())
                strokePaint.strokeWidth = 2f * resources.displayMetrics.density
            }
            CircleSettings.STYLE_RING -> {
                fillPaint.color = Color.argb(45, r, g, b)
                strokePaint.color = Color.rgb(r, g, b)
                strokePaint.strokeWidth = 3.5f * resources.displayMetrics.density
            }
            CircleSettings.STYLE_DOT -> {
                fillPaint.color = Color.argb(55, r, g, b)
                strokePaint.color = Color.rgb(r, g, b)
                strokePaint.strokeWidth = 3f * resources.displayMetrics.density
                innerPaint.color = Color.rgb(r, g, b)
            }
            CircleSettings.STYLE_DOUBLE_RING -> {
                fillPaint.color = Color.argb(40, r, g, b)
                strokePaint.color = Color.rgb(r, g, b)
                strokePaint.strokeWidth = 2.5f * resources.displayMetrics.density
                innerPaint.color = Color.rgb(r, g, b)
                innerPaint.style = Paint.Style.STROKE
                innerPaint.strokeWidth = 1.8f * resources.displayMetrics.density
            }
        }
    }

    private fun resetIdleTimer() {
        handler.removeCallbacks(idleFadeRunnable)
        if (isIdle) {
            animateAlphaTo(settings.opacityActive)
            isIdle = false
        }
        if (settings.autoFade) {
            handler.postDelayed(idleFadeRunnable, idleTimeoutMs)
        }
    }

    private fun animateAlphaTo(targetAlpha: Float) {
        alphaAnimator?.cancel()
        alphaAnimator = ValueAnimator.ofFloat(this.alpha, targetAlpha).apply {
            duration = 350
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val animatedValue = animator.animatedValue as Float
                this@CircleHomeView.alpha = animatedValue
            }
            start()
        }
    }

    private fun triggerHaptic(strong: Boolean = false) {
        if (!settings.hapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (strong) {
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(18, 120)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (strong) 35L else 18L)
            }
        } catch (_: Exception) {
        }
    }

    private fun animateScale(targetScale: Float) {
        val anim = ValueAnimator.ofFloat(scaleFactor, targetScale)
        anim.duration = 140
        anim.interpolator = if (targetScale > 1.0f) OvershootInterpolator() else DecelerateInterpolator()
        anim.addUpdateListener {
            scaleFactor = it.animatedValue as Float
            invalidate()
        }
        anim.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        resetIdleTimer()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPressedDown = true
                hasTriggeredLongPress = false
                isDragging = false
                animateScale(0.88f)
                triggerHaptic(false)

                initialX = layoutParams?.x ?: 0
                initialY = layoutParams?.y ?: 0
                initialTouchX = event.rawX
                initialTouchY = event.rawY

                // Setup Long press detection
                longPressRunnable = Runnable {
                    if (isPressedDown && !isDragging) {
                        hasTriggeredLongPress = true
                        triggerHaptic(true)
                        animateScale(1.15f)
                        listener?.onLongPress()
                    }
                }
                handler.postDelayed(longPressRunnable!!, longPressTimeout)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialTouchX
                val deltaY = event.rawY - initialTouchY
                val distance = hypot(deltaX, deltaY)

                if (!isDragging && distance > touchSlop) {
                    isDragging = true
                    longPressRunnable?.let { handler.removeCallbacks(it) }
                    pendingSingleTapRunnable?.let { handler.removeCallbacks(it) }
                }

                if (isDragging && layoutParams != null && windowManager != null) {
                    layoutParams?.x = (initialX + deltaX).toInt()
                    layoutParams?.y = (initialY + deltaY).toInt()

                    try {
                        windowManager?.updateViewLayout(this, layoutParams)
                    } catch (_: Exception) {
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPressedDown = false
                animateScale(1.0f)
                longPressRunnable?.let { handler.removeCallbacks(it) }

                if (isDragging) {
                    // Drag ended: snap to edge if enabled
                    if (settings.snapToEdge) {
                        snapToNearestEdge()
                    } else {
                        layoutParams?.let { lp ->
                            listener?.onPositionChanged(lp.x, lp.y)
                        }
                    }
                } else if (!hasTriggeredLongPress) {
                    // Process Tap
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < doubleTapTimeout && settings.doubleTapAction != CircleSettings.ACTION_NONE) {
                        // Double tap detected
                        pendingSingleTapRunnable?.let { handler.removeCallbacks(it) }
                        triggerHaptic(true)
                        listener?.onDoubleTap()
                        lastTapTime = 0L
                    } else {
                        // Potential Single Tap
                        lastTapTime = currentTime
                        if (settings.doubleTapAction == CircleSettings.ACTION_NONE) {
                            // Instant execution without waiting for double tap
                            triggerHaptic(false)
                            listener?.onSingleTap()
                        } else {
                            // Wait for possible double tap
                            pendingSingleTapRunnable = Runnable {
                                triggerHaptic(false)
                                listener?.onSingleTap()
                            }
                            handler.postDelayed(pendingSingleTapRunnable!!, doubleTapTimeout)
                        }
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun snapToNearestEdge() {
        val currentLp = layoutParams ?: return
        val wm = windowManager ?: return

        val viewWidth = width.takeIf { it > 0 } ?: (settings.sizeDp * resources.displayMetrics.density).toInt()
        val centerX = currentLp.x + viewWidth / 2
        val targetX = if (centerX < screenWidth / 2) 16 else (screenWidth - viewWidth - 16)

        val startX = currentLp.x
        val snapAnimator = ValueAnimator.ofInt(startX, targetX)
        snapAnimator.duration = 220
        snapAnimator.interpolator = DecelerateInterpolator()
        snapAnimator.addUpdateListener { anim ->
            currentLp.x = anim.animatedValue as Int
            try {
                wm.updateViewLayout(this, currentLp)
            } catch (_: Exception) {
            }
        }
        snapAnimator.start()
        listener?.onPositionChanged(targetX, currentLp.y)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val baseRadius = min(w, h) / 2f - (4f * resources.displayMetrics.density)
        val radius = baseRadius * scaleFactor

        // Draw soft drop shadow for elevation
        canvas.drawCircle(cx, cy + (2f * resources.displayMetrics.density), radius + 1f, shadowPaint)

        when (settings.circleStyle) {
            CircleSettings.STYLE_SOLID -> {
                canvas.drawCircle(cx, cy, radius, fillPaint)
                canvas.drawCircle(cx, cy, radius, strokePaint)
            }
            CircleSettings.STYLE_RING -> {
                canvas.drawCircle(cx, cy, radius, fillPaint)
                canvas.drawCircle(cx, cy, radius - (strokePaint.strokeWidth / 2f), strokePaint)
            }
            CircleSettings.STYLE_DOT -> {
                canvas.drawCircle(cx, cy, radius, fillPaint)
                canvas.drawCircle(cx, cy, radius - (strokePaint.strokeWidth / 2f), strokePaint)
                // Center dot
                val dotRadius = radius * 0.38f
                canvas.drawCircle(cx, cy, dotRadius, innerPaint)
            }
            CircleSettings.STYLE_DOUBLE_RING -> {
                canvas.drawCircle(cx, cy, radius, fillPaint)
                canvas.drawCircle(cx, cy, radius - (strokePaint.strokeWidth / 2f), strokePaint)
                // Inner concentric ring
                val innerRadius = radius * 0.55f
                canvas.drawCircle(cx, cy, innerRadius, innerPaint)
            }
        }
    }
}
