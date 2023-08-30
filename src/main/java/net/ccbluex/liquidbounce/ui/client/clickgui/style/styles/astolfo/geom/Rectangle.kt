package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.geom

import kotlin.Float.Companion.NaN

class Rectangle(var x: Float = NaN, var y: Float = NaN, var width: Float = NaN, var height: Float = NaN) {
  constructor(rect: Rectangle) : this(rect.x, rect.y, rect.width, rect.height)

  fun contains(point: Point) = point.x in x..x + width && point.y in y..y + height
  fun contains(x: Float, y: Float) = contains(Point(x, y))
  fun contains(x: Int, y: Int) = contains(Point(x.toFloat(), y.toFloat()))
}