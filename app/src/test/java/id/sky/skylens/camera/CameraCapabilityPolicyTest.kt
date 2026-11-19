package id.sky.skylens.camera

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraCapabilityPolicyTest {
    @Test
    fun reports120FpsWhenConstrainedHighSpeedRangeReaches120() {
        assertTrue(supports120Fps(listOf(120..120, 30..240)))
    }

    @Test
    fun doesNotInfer120FpsFromOrdinaryOrLowerSpeedRanges() {
        assertFalse(supports120Fps(listOf(30..30, 30..60, 60..60)))
    }

    @Test
    fun doesNotReport120WhenOnly240FpsIsSupported() {
        assertFalse(supports120Fps(listOf(240..240, 240..480)))
    }

    @Test
    fun reports120WhenItFallsInsideAHighSpeedRange() {
        assertTrue(supports120Fps(listOf(60..120, 120..240)))
    }

    @Test
    fun emptyHighSpeedRangeListMeansNotReported() {
        assertFalse(supports120Fps(emptyList()))
    }
}
