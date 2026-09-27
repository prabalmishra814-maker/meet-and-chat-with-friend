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
 * Custom neon-styled popup dialog displayed when the spin wheel stops.
 */
class SpinWinnerDialog(context: Context) : Dialog(context) {

    private val ivWinnerSegmentImage: ImageView
    private val tvWinnerName: TextView
    private val btnWinnerOk: MaterialButton

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_spin_winner_popup, null, false)
        setContentView(view)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)

        ivWinnerSegmentImage = view.findViewById(R.id.ivWinnerSegmentImage)
        tvWinnerName = view.findViewById(R.id.tvWinnerName)
        btnWinnerOk = view.findViewById(R.id.btnWinnerOk)

        btnWinnerOk.setOnClickListener {
            dismiss()
        }
    }

    fun showWinner(segment: SpinWheelController.SpinSegment, onClaim: (() -> Unit)? = null) {
        ivWinnerSegmentImage.setImageResource(segment.drawableRes)
        tvWinnerName.text = segment.name

        btnWinnerOk.setOnClickListener {
            dismiss()
            onClaim?.invoke()
        }

        if (!isShowing) {
            show()
        }
    }
}
