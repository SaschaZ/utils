package de.gapps.ctt.plotter.meta.output.color

sealed class PlotColor(
    open val raw: String,
    open val rawInit: String? = null
) {

    override fun equals(other: Any?) = (other as? PlotColor)?.raw == raw
    override fun hashCode() = raw.hashCode()
    override fun toString() = "${this::class.java.simpleName}($raw)"

    open class SimpleColor(raw: String) : PlotColor(raw)
    data class ByArgb(val argb: Long) : SimpleColor("rgb \"#$argb\"")
    data class ByColorName(val colorName: String) : SimpleColor("rgb \"$colorName\"")

    open class Palette<T>(vararg valueColorPair: Pair<T, SimpleColor>) :
        PlotColor(
            "palette",
            "set palette defined (${valueColorPair.joinToString(", ") { "${it.first} \"${it.second}\"" }}); " +
                    "set cbrange [${valueColorPair.mapNotNull { (it.first as? Number)?.toDouble() }.min()}:" +
                    "${valueColorPair.mapNotNull { (it.first as? Number)?.toDouble() }.max()}]; " +
                    "unset colorbox"
        )

    open class BinaryPalette(primaryColor: SimpleColor, secondaryColor: SimpleColor) :
        Palette<Int>(-1 to primaryColor, 1 to secondaryColor) {

        constructor(primaryColor: SimpleColors, secondaryColor: SimpleColors) :
                this(primaryColor.argb, secondaryColor.argb)
    }
}