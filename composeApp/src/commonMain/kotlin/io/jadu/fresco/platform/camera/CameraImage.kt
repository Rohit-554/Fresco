package io.jadu.fresco.platform.camera

/**
 * @param bytes raw JPEG image bytes
 * @param rotationDegrees clockwise rotation needed to display upright (0, 90, 180, 270)
 */
data class CameraImage(val bytes: ByteArray, val rotationDegrees: Int = 0) {
    override fun equals(other: Any?) =
        other is CameraImage && bytes.contentEquals(other.bytes) && rotationDegrees == other.rotationDegrees

    override fun hashCode() = 31 * bytes.contentHashCode() + rotationDegrees
}
