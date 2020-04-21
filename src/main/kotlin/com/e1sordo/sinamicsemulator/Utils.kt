package com.e1sordo.sinamicsemulator

import org.apache.tomcat.util.codec.binary.Base64
import java.nio.ByteBuffer
import java.util.regex.Pattern
import kotlin.experimental.and

val LEFT_TWO_BYTES_ROTATING = 2 shl 15
val INDEX0_U32_BINARY = 64512
val HEX_PATTERN = Pattern.compile("[0-9a-fA-F]+")

fun packData(value: String, dataType: String): String {
    val buffer = ByteArray(4)
    if (dataType == "Float") {
        packFloat(buffer, value.toFloat())
    } else {
        packUnsignedInt(buffer, value)
    }
    return Base64.encodeBase64String(buffer)
}

fun packFloat(bufferWrapper: ByteArray?, value: Float) {
    ByteBuffer.wrap(bufferWrapper).putFloat(value)
}

fun packUnsignedInt(bufferWrapper: ByteArray?, value: Any?) {
    var value = value
    if (value is String) {
        val stringValue = value
        if (stringValue.contains(".")) { // Array consists of BICO param #, "." and index
            val numberDotIndex = stringValue.split('.').toTypedArray()
            value = LEFT_TWO_BYTES_ROTATING * numberDotIndex[0].toInt() + numberDotIndex[1].toInt() + INDEX0_U32_BINARY
        } else if (HEX_PATTERN.matcher(stringValue).matches()) {
            value = stringValue.toInt()
        }
    } else if (value is Number
            && value.toInt().toDouble() == value.toDouble() // check the number is integer
            && (value.toInt() == 0 || value.toInt() == 1)) {
        value = LEFT_TWO_BYTES_ROTATING * value.toInt()
    } else { // TODO create a new specific runtime Exception
        throw RuntimeException("incorrect data type")
    }
    ByteBuffer.wrap(bufferWrapper).putInt((value as Int?)!!)
}




fun unpackData(value: String, dataType: String): String {
    val buffer = Base64.decodeBase64(value)
    return if (dataType == "Float") {
        unpackFloat(buffer)
    } else {
        unpackUnsignedInt(buffer)
    }
}

fun unpackUnsignedInt(bufferWrapper: ByteArray?): String {
    val firstHalf = ByteArray(2)
    System.arraycopy(bufferWrapper, 0, firstHalf, 0, 2)
    val lastHalf = ByteArray(2)
    System.arraycopy(bufferWrapper, 2, lastHalf, 0, 2)
    val firstHalfShort = ByteBuffer.wrap(firstHalf).short
    val unsignedLastHalf: Short = ByteBuffer.wrap(lastHalf).short and 0xffff.toShort()
    val lastHalfShort: Int = unsignedLastHalf - INDEX0_U32_BINARY
    if (firstHalfShort.toInt() == 0) return unsignedLastHalf.toString()
    return if (unsignedLastHalf < INDEX0_U32_BINARY) firstHalfShort.toString() else "$firstHalfShort.$lastHalfShort"
}

fun unpackFloat(bufferWrapper: ByteArray?): String {
    return ByteBuffer.wrap(bufferWrapper).float.toString()
}

