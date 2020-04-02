@file:Suppress("unused")

package dev.zieger.utils.log.console

enum class AsciiControlCharacters(val character: Char) : (Int) -> Unit {
    NULL(0),
    SOH(1), // Start of Header
    STX(2), // Start of Text
    TX(3), // End of Text
    OT(4), // End of Trans.
    ENQ(5), // Enquiry
    ACK(6), // Acknowledgement
    BEL(7), // Bell
    BS(8), // Backspace
    HT(9), // Horizontal
    LF(10), // Line feed
    VT(11), // Vertical Tab
    FF(12), // Form feed
    CR(13), // Carriage return (Deletes everything of the current line)
    SO(14), // Shift Out
    SI(15), // Shift In
    DLE(16), // Data link escape
    DC1(17), // Device control 1
    DC2(18), // Device control 2
    DC3(19), // Device control 3
    DC4(20), // Device control 4
    NAK(21), // Negative acknowledge
    SYN(22), // Synchronous idle
    TB(23), // End of trans. block
    CAN(24), // Cancel
    EM(25), // End of medium
    SUB(26), // Substitute
    ESC(27), // Escape
    FS(28), // File separator
    GS(29), // Group separator
    RS(30), // Record separator
    US(31), // Unit separator
    DEL(127), // Delete
    RI(144); // reverse line feed

    constructor(character: Int) : this(character.toChar())

    companion object {

        fun valueForCharacter(c: Char) = values().find { it.character == c }
    }

    override fun invoke(num: Int) = repeat(num) { print(character) }

    override fun toString() = "$name[${character.toByte().toHex(false)}]"
}

fun Byte.toHex(print0x: Boolean = true): String {
    val lsChar = toInt() and 0x0F
    val msChar = toInt() and 0xF0 shr 4
    return "${if (print0x) "0x" else ""}${msChar.toHexChar()}${lsChar.toHexChar()}"
}

fun Int.toHexChar() = when (this) {
    in 0..9 -> "$this"
    10 -> "A"
    11 -> "B"
    12 -> "C"
    13 -> "D"
    14 -> "E"
    15 -> "F"
    else -> ""
}