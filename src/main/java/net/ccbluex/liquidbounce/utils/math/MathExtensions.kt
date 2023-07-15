@file:JvmName("MathUtils2")

package net.ccbluex.liquidbounce.utils.math

import java.math.RoundingMode
import kotlin.math.*

const val DEGREES_TO_RADIANS = 0.017453292519943295

const val RADIANS_TO_DEGREES = 57.29577951308232

/**
 * Rounds double with [x] number of decimals
 */
fun Double.round(x: Int): Double {
    require(x >= 0) { "The value of decimal places must be absolute" }

    return this.toBigDecimal().setScale(x, RoundingMode.HALF_UP).toDouble()
}

/**
 * Converts double to radians
 */
fun Double.toRadians() = this * DEGREES_TO_RADIANS

/**
 * Converts double to degrees
 */
fun Double.toDegrees() = this * RADIANS_TO_DEGREES

/**
 * Calculates Gaussian value in one dimension
 *
 * [Assignment information](https://en.wikipedia.org/wiki/Gaussian_blur)
 */
fun gaussian(x: Int, sigma: Float): Float {
    val s = sigma * sigma * 2

    return (1f / (sqrt(PI.toFloat() * s))) * exp(-(x * x) / s)
}
fun Float.toRadians() = this * 0.017453292f
fun Float.toRadiansD() = toRadians().toDouble()