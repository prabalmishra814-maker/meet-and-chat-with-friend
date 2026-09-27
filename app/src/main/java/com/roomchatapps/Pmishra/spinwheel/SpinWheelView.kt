package com.roomchatapps.Pmishra.spinwheel

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.roomchatapps.Pmishra.R

/**
 * Custom 9-layer FrameLayout for the Lucky Spin Wheel UI.
 *
 * Layer order (back to front):
 * 1. bg.png
 * 2. shadow.png
 * 3. stand.png
 * 4. wheel.png (ONLY layer that rotates)
 * 5. outer_ring.png
 * 6. divider.png
 * 7. center.png
 * 8. pointer.png
 * 9. spin_button.png
 */
class SpinWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    fun interface OnSpinCompleteListener {
        fun onSpinComplete(winner: SpinWheelController.SpinSegment)
    }

    fun interface OnSpinButtonClickListener {
        fun onSpinButtonClick()
    }

    val ivBg: ImageView = ImageView(context)
    val ivShadow: ImageView = ImageView(context)
    val ivStand: ImageView = ImageView(context)
    val ivWheel: ImageView = ImageView(context)
    val ivOuterRing: ImageView = ImageView(context)
    val ivDivider: ImageView = ImageView(context)
    val ivCenter: ImageView = ImageView(context)
    val ivPointer: ImageView = ImageView(context)
    val ivSpinButton: ImageView = ImageView(context)

    private var currentRotation: Float = 0f
    private var isSpinningState: Boolean = false

    private var spinButtonClickListener: OnSpinButtonClickListener? = null

    init {
        setupLayers()
        setupSpinButtonTouchEffect()
    }

    fun setOnSpinButtonClickListener(listener: OnSpinButtonClickListener?) {
        this.spinButtonClickListener = listener
    }

    private fun setupLayers() {
        // 1. Background Aura
        ivBg.setImageResource(R.drawable.bg)
        ivBg.scaleType = ImageView.ScaleType.FIT_CENTER
        addView(ivBg, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER))

        // 2. Floor Shadow
        ivShadow.setImageResource(R.drawable.shadow)
        ivShadow.scaleType = ImageView.ScaleType.FIT_CENTER
        addView(ivShadow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL))

        // 3. Stand / Pedestal
        ivStand.setImageResource(R.drawable.stand)
        ivStand.scaleType = ImageView.ScaleType.FIT_CENTER
        addView(ivStand, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL))

        // 4. Rotating Wheel Artwork
        ivWheel.setImageResource(R.drawable.wheel)
        ivWheel.scaleType = ImageView.ScaleType.FIT_CENTER
        addView(ivWheel, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER))

        // 5. Fixed Outer Ring
        ivOuterRing.setImageResource(R.drawable.outer_ring)
        ivOuterRing.scaleType = ImageView.ScaleType.FIT_CENTER
        addView(ivOuterRing, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER))

        // 6. Fixed Divider Frame
        ivDivider.setImageResource(R.drawable.divider)
        ivDivider.scaleType = ImageView.ScaleType.FIT_CENTER
        addView(ivDivider, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL))

        // 7. Fixed Star Center Hub
        ivCenter.setImageResource(R.drawable.center)
        ivCenter.scaleType = ImageView.ScaleType.FIT_CENTER
        addView(ivCenter, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER))

        // 8. Fixed Pointer Arrow
        ivPointer.setImageResource(R.drawable.pointer)
        ivPointer.scaleType = ImageView.ScaleType.FIT_CENTER
        addView(ivPointer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL))

        // 9. Spin Button
        ivSpinButton.setImageResource(R.drawable.spin_button)
        ivSpinButton.scaleType = ImageView.ScaleType.FIT_CENTER
        ivSpinButton.isClickable = true
        ivSpinButton.isFocusable = true
        addView(ivSpinButton, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val w = right - left
        val h = bottom - top
        if (w <= 0 || h <= 0) return

        val size = minOf(w, h)
        val wheelDiameter = (size * 0.72f).toInt()

        val centerX = w / 2
        val centerY = h / 2

        // 1. bg
        val bgSize = (size * 0.98f).toInt()
        ivBg.layout(centerX - bgSize / 2, centerY - bgSize / 2, centerX + bgSize / 2, centerY + bgSize / 2)

        // 2. shadow
        val shadowW = (size * 0.85f).toInt()
        val shadowH = (size * 0.22f).toInt()
        val shadowTop = h - shadowH
        ivShadow.layout(centerX - shadowW / 2, shadowTop, centerX + shadowW / 2, h)

        // 3. stand
        val standW = (size * 0.82f).toInt()
        val standH = (size * 0.38f).toInt()
        val standTop = h - standH - (size * 0.02f).toInt()
        ivStand.layout(centerX - standW / 2, standTop, centerX + standW / 2, standTop + standH)

        // 4. wheel
        ivWheel.layout(centerX - wheelDiameter / 2, centerY - wheelDiameter / 2, centerX + wheelDiameter / 2, centerY + wheelDiameter / 2)
        ivWheel.pivotX = wheelDiameter / 2f
        ivWheel.pivotY = wheelDiameter / 2f

        // 5. outer_ring
        val ringSize = (wheelDiameter * 1.05f).toInt()
        ivOuterRing.layout(centerX - ringSize / 2, centerY - ringSize / 2, centerX + ringSize / 2, centerY + ringSize / 2)

        // 6. divider
        val dividerW = (wheelDiameter * 0.38f).toInt()
        val dividerH = (wheelDiameter * 0.40f).toInt()
        val dividerTop = centerY - wheelDiameter / 2 + (wheelDiameter * 0.01f).toInt()
        ivDivider.layout(centerX - dividerW / 2, dividerTop, centerX + dividerW / 2, dividerTop + dividerH)

        // 7. center
        val centerSize = (wheelDiameter * 0.26f).toInt()
        ivCenter.layout(centerX - centerSize / 2, centerY - centerSize / 2, centerX + centerSize / 2, centerY + centerSize / 2)

        // 8. pointer
        val pointerW = (wheelDiameter * 0.22f).toInt()
        val pointerH = (wheelDiameter * 0.22f).toInt()
        val pointerTop = centerY - wheelDiameter / 2 - (wheelDiameter * 0.05f).toInt()
        ivPointer.layout(centerX - pointerW / 2, pointerTop, centerX + pointerW / 2, pointerTop + pointerH)

        // 9. spin_button
        val btnW = (wheelDiameter * 0.46f).toInt()
        val btnH = (wheelDiameter * 0.18f).toInt()
        val btnTop = centerY - btnH / 2
        ivSpinButton.layout(centerX - btnW / 2, btnTop, centerX + btnW / 2, btnTop + btnH)
    }

    private fun setupSpinButtonTouchEffect() {
        ivSpinButton.setOnTouchListener { view, event ->
            if (isSpinningState) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animateScale(view, 0.94f)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    animateScale(view, 1.0f)
                }
            }
            false
        }

        ivSpinButton.setOnClickListener {
            if (!isSpinningState) {
                spinButtonClickListener?.onSpinButtonClick()
            }
        }
    }

    private fun animateScale(view: View, targetScale: Float) {
        val pvhX = PropertyValuesHolder.ofFloat(SCALE_X, targetScale)
        val pvhY = PropertyValuesHolder.ofFloat(SCALE_Y, targetScale)
        ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY).apply {
            duration = 120L
            start()
        }
    }

    /**
     * Smoothly rotates wheel.png so that the winner segment lands under pointer.png.
     */
    fun spinToSegment(
        winnerIndex: Int,
        durationMs: Long,
        fullRotations: Int,
        listener: OnSpinCompleteListener?
    ) {
        if (isSpinningState) return
        isSpinningState = true
        ivSpinButton.isEnabled = false

        val safeIndex = winnerIndex.coerceIn(0, SpinWheelController.NUM_SEGMENTS - 1)
        val targetRotation = SpinWheelController.calculateTargetRotation(
            currentRotation,
            safeIndex,
            fullRotations
        )

        val rotateAnimator = ObjectAnimator.ofFloat(ivWheel,
            ROTATION, currentRotation, targetRotation).apply {
            duration = durationMs
            interpolator = DecelerateInterpolator(2.2f)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentRotation = targetRotation
                    isSpinningState = false
                    ivSpinButton.isEnabled = true
                    val winner = SpinWheelController.SEGMENTS[safeIndex]
                    listener?.onSpinComplete(winner)
                }
            })
        }

        rotateAnimator.start()
    }

    fun spinToSegment(winnerIndex: Int, listener: OnSpinCompleteListener?) {
        spinToSegment(
            winnerIndex,
            SpinWheelController.SPIN_DURATION_MS,
            SpinWheelController.DEFAULT_FULL_ROTATIONS,
            listener
        )
    }

    fun isSpinning(): Boolean = isSpinningState
}
