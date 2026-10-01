package com.roomchatapps.Pmishra.spinwheel

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.roomchatapps.Pmishra.R

/**
 * Custom glass-styled popup dialog displayed when the spin wheel stops.
 */
class SpinWinnerDialog(context: Context) : Dialog(context) {

    private val tvWinnerTitle: TextView
    private val tvWinnerSubtitle: TextView
    private val ivWinnerSegmentImage: ImageView
    private val tvWinnerName: TextView
    private val btnWinnerOk: MaterialButton

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_spin_winner_popup, null, false)
        setContentView(view)

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
        setCancelable(true)

        tvWinnerTitle = view.findViewById(R.id.tvWinnerTitle)
        tvWinnerSubtitle = view.findViewById(R.id.tvWinnerSubtitle)
        ivWinnerSegmentImage = view.findViewById(R.id.ivWinnerSegmentImage)
        tvWinnerName = view.findViewById(R.id.tvWinnerName)
        btnWinnerOk = view.findViewById(R.id.btnWinnerOk)

        btnWinnerOk.setOnClickListener {
            dismiss()
        }
    }

    fun showWinner(segment: SpinWheelController.SpinSegment, onClaim: (() -> Unit)? = null) { // LUCKY WHEEL FIX
        ivWinnerSegmentImage.setImageResource(segment.drawableRes) // LUCKY WHEEL FIX

        if (segment.rewardCoins > 0) { // LUCKY WHEEL FIX
            tvWinnerTitle.text = "🎉 CONGRATULATIONS! 🎉" // LUCKY WHEEL FIX
            tvWinnerSubtitle.text = "YOU WON A LUCKY REWARD!" // LUCKY WHEEL FIX
            tvWinnerName.text = "Won ${segment.name}!" // LUCKY WHEEL FIX
            btnWinnerOk.text = "CLAIM REWARD" // LUCKY WHEEL FIX
        } else {
            tvWinnerTitle.text = "🍀 BETTER LUCK NEXT TIME!" // LUCKY WHEEL FIX
            tvWinnerSubtitle.text = "NO COINS WON THIS SPIN" // LUCKY WHEEL FIX
            tvWinnerName.text = segment.name // LUCKY WHEEL FIX
            btnWinnerOk.text = "TRY AGAIN" // LUCKY WHEEL FIX
        }

        btnWinnerOk.setOnClickListener { // LUCKY WHEEL FIX
            dismiss() // LUCKY WHEEL FIX
            onClaim?.invoke() // LUCKY WHEEL FIX
        }

        if (!isShowing) { // LUCKY WHEEL FIX
            show() // LUCKY WHEEL FIX
        }
    }
}

