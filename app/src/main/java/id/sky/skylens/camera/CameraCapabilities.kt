package id.sky.skylens.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Range
import android.util.Size

/** Real Camera2 metadata for the selected rear camera; never infers support from a UI label. */
data class CameraCapabilitySummary(
    val cameraId: String,
    val largestJpeg: Size?,
    val fpsRanges: List<Range<Int>>,
    val highSpeedFpsRanges: List<Range<Int>>,
    val supportsRaw: Boolean,
    val supportsManualSensor: Boolean,
    val supportsOis: Boolean,
    val supportsVideoStabilization: Boolean,
    val supports120Fps: Boolean,
    val hardwareLevel: Int?
)

object CameraCapabilities {
    fun scanRearCamera(context: Context): CameraCapabilitySummary? {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return manager.cameraIdList.mapNotNull { cameraId ->
            val characteristics = manager.getCameraCharacteristics(cameraId)
            if (characteristics.get(CameraCharacteristics.LENS_FACING) != CameraCharacteristics.LENS_FACING_BACK) {
                return@mapNotNull null
            }

            val streamMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val jpegSizes = streamMap?.getOutputSizes(ImageFormat.JPEG).orEmpty().toList()
            val largestJpeg = jpegSizes.maxByOrNull { it.width.toLong() * it.height.toLong() }
            val fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
                .orEmpty().toList()
            val highSpeedRanges = streamMap?.highSpeedVideoFpsRanges?.toList().orEmpty()
            val capabilities = (
                characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: intArrayOf()
            ).toSet()
            val oisModes = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                ?: intArrayOf()
            val stabilizationModes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
                ?: intArrayOf()

            CameraCapabilitySummary(
                cameraId = cameraId,
                largestJpeg = largestJpeg,
                fpsRanges = fpsRanges,
                highSpeedFpsRanges = highSpeedRanges,
                supportsRaw = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW),
                supportsManualSensor = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR),
                supportsOis = oisModes.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON),
                supportsVideoStabilization = stabilizationModes.contains(CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON),
                supports120Fps = supports120Fps(highSpeedRanges.map { it.lower..it.upper }),
                hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            )
        }.maxByOrNull { summary ->
            summary.largestJpeg?.let { it.width.toLong() * it.height.toLong() } ?: 0L
        }
    }

}

/** Only Camera2 constrained high-speed ranges qualify; ordinary AE FPS ranges do not. */
internal fun supports120Fps(highSpeedRanges: List<IntRange>): Boolean =
    highSpeedRanges.any { it.first <= 120 && it.last >= 120 }
