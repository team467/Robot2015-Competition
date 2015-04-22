package org.usfirst.frc.team467.robot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class DriveTest
{
    private static final double MAX_DELTA = 0.0001;

    static {
        Logging.initForTest();
    }
    
    @Test
    public void wrapAroundDifferenceTest()
    {
        assertEquals(0.0, WheelPod.wrapAroundDifference(0.0, 0.0), MAX_DELTA);
        assertEquals(0.0, WheelPod.wrapAroundDifference(Math.PI / 2, Math.PI / 2), MAX_DELTA);

        assertNotEquals(0.0, WheelPod.wrapAroundDifference(Math.PI / 2, Math.PI + Math.PI / 2), MAX_DELTA);
        assertEquals(0.0, WheelPod.wrapAroundDifference(Math.PI / 2, 2 * Math.PI + Math.PI / 2), MAX_DELTA);
        assertNotEquals(0.0, WheelPod.wrapAroundDifference(Math.PI / 2, 3 * Math.PI + Math.PI / 2), MAX_DELTA);
        assertEquals(0.0, WheelPod.wrapAroundDifference(Math.PI / 2, 4 * Math.PI + Math.PI / 2), MAX_DELTA);
        assertNotEquals(0.0, WheelPod.wrapAroundDifference(Math.PI / 2, 5 * Math.PI + Math.PI / 2), MAX_DELTA);
    }
}
