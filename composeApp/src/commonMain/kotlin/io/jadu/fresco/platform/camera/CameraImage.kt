package io.jadu.fresco.platform.camera

data class CameraImage(val bytes: ByteArray) {
    override fun equals(other: Any?) = other is CameraImage && bytes.contentEquals(other.bytes)
    override fun hashCode() = bytes.contentHashCode()
}
