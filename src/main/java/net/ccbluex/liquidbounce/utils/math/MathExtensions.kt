@file:JvmName("MathUtils2")

package net.ccbluex.liquidbounce.utils.math

import net.minecraft.util.Vec3
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sqrt

const val DEGREES_TO_RADIANS = 0.017453292519943295

const val RADIANS_TO_DEGREES = 57.29577951308232

/**
 * Provides:
 * ```
 * val (x, y, z) = vec
 */
operator fun Vec3.component1() = xCoord
operator fun Vec3.component2() = yCoord
operator fun Vec3.component3() = zCoord

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

operator fun Vec3.plus(vec: Vec3): Vec3 = add(vec)
operator fun Vec3.minus(vec: Vec3): Vec3 = subtract(vec)
operator fun Vec3.times(number: Double) = Vec3(xCoord * number, yCoord * number, zCoord * number)
operator fun Vec3.div(number: Double) = times(1 / number)
fun Double.toDegreesF() = toDegrees().toFloat()

class RangeIterator(private val range: ClosedFloatingPointRange<Double>, private val step: Double = 0.1): Iterator<Double> {
    private var value = range.start

    override fun hasNext() = value < range.endInclusive

    override fun next(): Double {
        val returned = value
        value = (value + step).coerceAtMost(range.endInclusive)
        return returned
    }
}

infix fun ClosedFloatingPointRange<Double>.step(step: Double) = RangeIterator(this, step)