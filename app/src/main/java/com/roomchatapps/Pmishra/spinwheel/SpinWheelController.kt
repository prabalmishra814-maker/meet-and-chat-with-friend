package com.roomchatapps.Pmishra.spinwheel

import com.roomchatapps.Pmishra.R

/**
 * Controller and data model for the 12-segment Lucky Spin Wheel.
 */
object SpinWheelController {

    /**
     * Initial rotation offset in degrees.
     * Segments are aligned with Segment 0 centered at top center (12 o'clock / 0 degrees).
     */
    const val START_ANGLE: Float = 0f // LUCKY WHEEL FIX

    /**
     * Total number of segments on the wheel artwork (12 segments).
     */
    const val NUM_SEGMENTS: Int = 12

    /**
     * Exact angular size of each segment in degrees (360 / 12 = 30 degrees).
     */
    val SEGMENT_ANGLE: Float = 360f / NUM_SEGMENTS.toFloat()

    /**
     * Default spin animation duration in milliseconds.
     */
    const val SPIN_DURATION_MS: Long = 4500L

    /**
     * Default number of full rotations before stopping on the winner.
     */
    const val DEFAULT_FULL_ROTATIONS: Int = 6

    data class SpinSegment(
        val index: Int,
        val name: String,
        val rewardCoins: Long,
        val drawableRes: Int = R.drawable.coin,
        val svgaPath: String = "",
        val costCoins: Long = 0L
    )

    /**
     * 12 Lucky Segments mapped directly to the wheel artwork clockwise starting from top center.
     */
    val SEGMENTS: List<SpinSegment> = listOf(
        SpinSegment(0, "Battle Luck next time", 0L, R.drawable.coin),
        SpinSegment(1, "700,000 Coins", 700000L, R.drawable.coin),
        SpinSegment(2, "200,000 Coins", 200000L, R.drawable.coin),
        SpinSegment(3, "100,000 Coins", 100000L, R.drawable.coin),
        SpinSegment(4, "Battle Luck next time", 0L, R.drawable.coin),
        SpinSegment(5, "500,000 Coins", 500000L, R.drawable.coin),
        SpinSegment(6, "200,000 Coins", 200000L, R.drawable.coin),
        SpinSegment(7, "2,000,000 Coins", 2000000L, R.drawable.coin),
        SpinSegment(8, "300,000 Coins", 300000L, R.drawable.coin),
        SpinSegment(9, "100,000 Coins", 100000L, R.drawable.coin),
        SpinSegment(10, "Battle Luck next time", 0L, R.drawable.coin),
        SpinSegment(11, "100,000 Coins", 100000L, R.drawable.coin)
    )

    /**
     * Calculates the exact final rotation angle so that the winning segment center aligns with
     * the pointer fixed at top center (0 degrees screen angle).
     */
    fun calculateTargetRotation(
        currentRotation: Float,
        winnerIndex: Int,
        fullRotations: Int = DEFAULT_FULL_ROTATIONS
    ): Float { // LUCKY WHEEL FIX
        val safeIndex = winnerIndex.coerceIn(0, NUM_SEGMENTS - 1) // LUCKY WHEEL FIX
        // Segment center angle relative to unrotated wheel artwork (Segment 0 is centered at 0°):
        val segmentCenterAngle = (safeIndex * SEGMENT_ANGLE) % 360f // LUCKY WHEEL FIX

        // Desired wheel rotation R mod 360 such that (segmentCenterAngle + R) % 360 == 0
        val desiredModulo = (360f - segmentCenterAngle) % 360f // LUCKY WHEEL FIX

        val currentModulo = (currentRotation % 360f + 360f) % 360f // LUCKY WHEEL FIX
        var addDegrees = (desiredModulo - currentModulo) % 360f // LUCKY WHEEL FIX
        if (addDegrees <= 0f) { // LUCKY WHEEL FIX
            addDegrees += 360f // LUCKY WHEEL FIX
        } // LUCKY WHEEL FIX

        return currentRotation + (fullRotations * 360f) + addDegrees // LUCKY WHEEL FIX
    }
}

