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

    fun showWinner(segment: SpinWheelController.SpinSegment, onClaim: (() -> Unit)? = null) {
        ivWinnerSegmentImage.setImageResource(segment.drawableRes)

        if (segment.rewardCoins > 0) {
            tvWinnerTitle.text = "🎉 CONGRATULATIONS! 🎉"
            tvWinnerSubtitle.text = "YOU WON A LUCKY REWARD!"
            tvWinnerName.text = "Won ${segment.name}!"
            btnWinnerOk.text = "CLAIM REWARD"
        } else {
            tvWinnerTitle.text = "🍀 BETTER LUCK NEXT TIME!"
            tvWinnerSubtitle.text = "NO COINS WON THIS SPIN"
            tvWinnerName.text = segment.name
            btnWinnerOk.text = "TRY AGAIN"
        }

        btnWinnerOk.setOnClickListener {
            dismiss()
            onClaim?.invoke()
        }

        if (!isShowing) {
            show()
        }
    }
}
