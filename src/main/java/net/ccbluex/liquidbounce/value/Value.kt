/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.flux.Translate
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.extensions.setAlpha
import net.minecraft.client.gui.FontRenderer
import java.awt.Color
import java.util.*

abstract class Value<T>(val name: String,var value: T, var canDisplay: () -> Boolean) {
    var textHovered: Boolean = false
    fun set(newValue: T) {
        if (newValue == value) return

        val oldValue = get()

        try {
            onChange(oldValue, newValue)
            changeValue(newValue)
            onChanged(oldValue, newValue)
            LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.valuesConfig)
        } catch (e: Exception) {
            ClientUtils.getLogger().error("[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]")
        }
    }

    fun get() = value

    open fun changeValue(value: T) {
        this.value = value
    }

    val displayable: Boolean
        get() = displayableFunc()

    private var displayableFunc: () -> Boolean = { true }

    abstract fun toJson(): JsonElement?
    abstract fun fromJson(element: JsonElement)

    protected open fun onChange(oldValue: T, newValue: T) {}
    protected open fun onChanged(oldValue: T, newValue: T) {}

}

/**
 * Bool value represents a value with a boolean
 */
open class BoolValue(name: String, value: Boolean, displayable: () -> Boolean) : Value<Boolean>(name, value, displayable) {

    constructor(name: String, value: Boolean): this(name, value, { true } )
    var hide = false

    fun toggle() {
        value = !value
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asBoolean || element.asString.equals("true", ignoreCase = true)
    }

}

/**
 * Integer value represents a value with a integer
 */
open class IntegerValue(name: String, value: Int, val minimum: Int = 0, val maximum: Int = Integer.MAX_VALUE, val suffix: String, displayable: () -> Boolean)
    : Value<Int>(name, value, displayable) {
    val translate = Translate(0F, 0F)

    constructor(name: String, value: Int, minimum: Int, maximum: Int, displayable: () -> Boolean): this(name, value, minimum, maximum, "", displayable)
    constructor(name: String, value: Int, minimum: Int, maximum: Int, suffix: String): this(name, value, minimum, maximum, suffix, { true } )
    constructor(name: String, value: Int, minimum: Int, maximum: Int): this(name, value, minimum, maximum, { true } )

    fun set(newValue: Number) {
        set(newValue.toInt())
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asInt
    }

}

/**
 * Float value represents a value with a float
 */
open class FloatValue(name: String, value: Float, val minimum: Float = 0F, val maximum: Float = Float.MAX_VALUE, val suffix: String, displayable: () -> Boolean)
    : Value<Float>(name, value, displayable) {
    val translate = Translate(0F, 0F)

    constructor(name: String, value: Float, minimum: Float, maximum: Float, displayable: () -> Boolean): this(name, value, minimum, maximum, "", displayable)
    constructor(name: String, value: Float, minimum: Float, maximum: Float, suffix: String): this(name, value, minimum, maximum, suffix, { true } )
    constructor(name: String, value: Float, minimum: Float, maximum: Float): this(name, value, minimum, maximum, { true } )

    fun set(newValue: Number) {
        set(newValue.toFloat())
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asFloat
    }

}

/**
 * Text value represents a value with a string
 */
open class TextValue(name: String, value: String, displayable: () -> Boolean) : Value<String>(name, value, displayable) {

    constructor(name: String, value: String): this(name, value, { true } )

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asString
    }
    fun append(o: Any): TextValue {
        set(get() + o)
        return this
    }
}

open class ColorValue(name: String, value: Int, displayable: () -> Boolean) : Value<Int>(name, value, displayable) {

    constructor(name: String, value: Int): this(name, value, { true } )

    private val Expanded = false

    open fun isExpanded(): Boolean {
        return this.Expanded
    }

    fun getExpanded(): Boolean {
        return Expanded
    }

    fun setExpanded(set: Boolean): Boolean {
        return Expanded
    }
    fun set(newValue: Number) {
        set(newValue.toInt())
    }
    open fun getValue(): Int {
        return super.get()
    }

    open fun getHSB(): FloatArray? {
        val hsbValues = FloatArray(3)
        var saturation = 0.0f
        var brightness = 0.0f
        var hue = 0.0f
        var cMax: Int = Math.max(this.getValue() ushr 16 and 0xFF, this.getValue() ushr 8 and 0xFF)
        if (this.getValue() and 0xFF > cMax) {
            cMax = this.getValue() and 0xFF
        }
        var cMin: Int = Math.min(this.getValue() ushr 16 and 0xFF, this.getValue() ushr 8 and 0xFF)
        if (this.getValue() and 0xFF < cMin) {
            cMin = this.getValue() and 0xFF
        }
        brightness = cMax / 255.0f
        saturation = if (cMax != 0) (cMax - cMin) / cMax.toFloat() else 0.0f
        if (saturation == 0.0f) {
            hue = 0.0f
        } else {
            val redC: Float = (cMax - (this.getValue() ushr 16 and 0xFF)) / (cMax - cMin).toFloat()
            val greenC: Float = (cMax - (this.getValue() ushr 8 and 0xFF)) / (cMax - cMin).toFloat()
            val blueC: Float = (cMax - (this.getValue() and 0xFF)) / (cMax - cMin).toFloat()
            hue = (if (this.getValue() ushr 16 and 0xFF == cMax) blueC - greenC else if (this.getValue() ushr 8 and 0xFF == cMax) 2.0f + redC - blueC else 4.0f + greenC - redC) / 6.0f
            if (hue < 0) {
                ++hue
            }
        }
        hsbValues[0] = hue
        hsbValues[1] = saturation
        hsbValues[2] = brightness
        return hsbValues
    }

    override fun toJson(): JsonElement? {
        val valueObject = JsonObject()
        valueObject.addProperty("red", value)
        valueObject.addProperty("green", value)
        valueObject.addProperty("blue", value)
        valueObject.addProperty("alpha", value)
        return valueObject
    }

    override fun fromJson(element: JsonElement) {
        if(element.isJsonPrimitive)
            value = element.asInt
    }

}
/**
 * Font value represents a value with a font
 */
class FontValue(valueName: String, value: FontRenderer, displayable: () -> Boolean) : Value<FontRenderer>(valueName, value, displayable) {

    var openList = false

    constructor(valueName: String, value: FontRenderer) : this(valueName, value, { true })

    override fun toJson(): JsonElement? {
        val fontDetails = Fonts.getFontDetails(value) ?: return null
        val valueObject = JsonObject()
        valueObject.addProperty("fontName", fontDetails[0] as String)
        valueObject.addProperty("fontSize", fontDetails[1] as Int)
        return valueObject
    }

    override fun fromJson(element: JsonElement) {
        if (!element.isJsonObject) return
        val valueObject = element.asJsonObject
        value = Fonts.getFontRenderer(valueObject["fontName"].asString, valueObject["fontSize"].asInt)
    }
}

/**
 * Block value represents a value with a block
 */
class BlockValue(name: String, value: Int, displayable: () -> Boolean) : IntegerValue(name, value, 1, 197, displayable) {
    var openList = false

    constructor(name: String, value: Int) : this(name, value, { true })
}

/**
 * List value represents a selectable list of values
 */
open class ListValue(name: String, val values: Array<String>, value: String, displayable: () -> Boolean) : Value<String>(name, value, displayable) {

    constructor(name: String, values: Array<String>, value: String): this(name, values, value, { true } )

    open fun getModes(): List<String?>? {
        return values.toList()
    }
    @JvmField
    var openList = false
    fun getModeListNumber(mode: String) = values.indexOf(mode)
    init {
        this.value = value
    }

    init {
        this.value = value
    }

    open fun getModeGet(i: Int): String? {
        return values[i]
    }

    operator fun contains(string: String?): Boolean {
        return Arrays.stream(values).anyMatch { s: String -> s.equals(string, ignoreCase = true) }
    }

    fun indexOf(mode: String): Int {
        for (i in values.indices) {
            if (values[i].equals(mode, true)) return i
        }
        return 0
    }

    fun isMode(string: String): Boolean {
        return this.value.equals(string, ignoreCase = true)
    }

    override fun changeValue(value: String) {
        for (element in values) {
            if (element.equals(value, ignoreCase = true)) {
                this.value = element
                break
            }
        }
    }

    fun nextValue() {
        var index = values.indexOf(value) + 1
        if (index > values.size - 1) index = 0
        value = values[index]
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) changeValue(element.asString)
    }
}