package com.roomchatapps.Pmishra.spinwheel

import com.roomchatapps.Pmishra.R

/**
 * Controller and data model for the 13-segment Lucky Spin Wheel.
 */
object SpinWheelController {

    /**
     * Starting angle offset in degrees.
     * seg1.png (Magic Gift) is centered at the top (12 o'clock / 0 degrees) in wheel.png.
     */
    const val START_ANGLE: Float = 0f

    /**
     * Total number of segments on the wheel artwork.
     */
    const val NUM_SEGMENTS: Int = 13

    /**
     * Exact angular size of each segment in degrees.
     */
    val SEGMENT_ANGLE: Float = 360f / NUM_SEGMENTS.toFloat() // 27.6923077 degrees

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
        val drawableRes: Int,
        val svgaPath: String,
        val costCoins: Long
    )

    /**
     * 13 Lucky Segments Pool mapped to seg1.png .. seg13.png.
     */
    val SEGMENTS: List<SpinSegment> = listOf(
        SpinSegment(0, "Magic Gift 🎁", R.drawable.seg1, "gift/magic_gift.svga", 300),
        SpinSegment(1, "Angel Queen Crown 👑", R.drawable.seg2, "gift/angel_queen_crown.svga", 600),
        SpinSegment(2, "Forever Couple 💑", R.drawable.seg3, "gift/forever_couple.svga", 700),
        SpinSegment(3, "Crystal Rose 🌹", R.drawable.seg4, "gift/crystal_rose.svga", 350),
        SpinSegment(4, "Angel Bride 👰", R.drawable.seg5, "gift/angel_bride.svga", 500),
        SpinSegment(5, "Popcorn 🍿", R.drawable.seg6, "gift/popcorn.svga", 40),
        SpinSegment(6, "Baklava 🥮", R.drawable.seg7, "gift/baklava.svga", 80),
        SpinSegment(7, "Glass Glow Rose 🌹", R.drawable.seg8, "gift/glass_glow_rose.svga", 400),
        SpinSegment(8, "Money Stack 💵", R.drawable.seg9, "gift/money.svga", 200),
        SpinSegment(9, "Refrigerator 🧊", R.drawable.seg10, "gift/refrigerator.svga", 500),
        SpinSegment(10, "Party Popper 🎉", R.drawable.seg11, "gift/party_popper.svga", 160),
        SpinSegment(11, "Gold Bar 🪙", R.drawable.seg12, "gift/gold_bar.svga", 250),
        SpinSegment(12, "Magic Sword ⚔️", R.drawable.seg13, "gift/magic_sword.svga", 700)
    )

    /**
     * Calculates the exact final rotation angle so that the winning segment center aligns with
     * the pointer fixed at top center (0 degrees screen angle).
     *
     * @param currentRotation Current rotation angle of wheel.png in degrees.
     * @param winnerIndex Index of winning segment (0 to 12).
     * @param fullRotations Number of complete 360-degree rotations to execute.
     */
    fun calculateTargetRotation(
        currentRotation: Float,
        winnerIndex: Int,
        fullRotations: Int = DEFAULT_FULL_ROTATIONS
    ): Float {
        val safeIndex = winnerIndex.coerceIn(0, NUM_SEGMENTS - 1)
        val winnerCenterAngle = START_ANGLE + (safeIndex * SEGMENT_ANGLE) + (SEGMENT_ANGLE / 2f)
        val desiredWheelAngle = (360f - (winnerCenterAngle % 360f)) % 360f

        val currentAngle = (currentRotation % 360f + 360f) % 360f
        var neededDegrees = (desiredWheelAngle - currentAngle) % 360f
        if (neededDegrees < 0f) {
            neededDegrees += 360f
        }

        return currentRotation + (fullRotations * 360f) + neededDegrees
    }
}
