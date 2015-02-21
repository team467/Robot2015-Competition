package org.usfirst.frc.team467.robot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class DriveTest
{
    private static final double MAX_DELTA = 0.0001;

    @Test
    public void wrapAroundDifferenceTest()
    {
        assertEquals(0.0, Drive.wrapAroundDifference(0.0, 0.0), MAX_DELTA);
        assertEquals(0.0, Drive.wrapAroundDifference(Math.PI / 2, Math.PI / 2), MAX_DELTA);

        assertNotEquals(0.0, Drive.wrapAroundDifference(Math.PI / 2, Math.PI + Math.PI / 2), MAX_DELTA);
        assertEquals(0.0, Drive.wrapAroundDifference(Math.PI / 2, 2 * Math.PI + Math.PI / 2), MAX_DELTA);
        assertNotEquals(0.0, Drive.wrapAroundDifference(Math.PI / 2, 3 * Math.PI + Math.PI / 2), MAX_DELTA);
        assertEquals(0.0, Drive.wrapAroundDifference(Math.PI / 2, 4 * Math.PI + Math.PI / 2), MAX_DELTA);
        assertNotEquals(0.0, Drive.wrapAroundDifference(Math.PI / 2, 5 * Math.PI + Math.PI / 2), MAX_DELTA);
    }
    
    @Test
    public void testFormat()
    {
        // All zero
        assertEquals(
            "FL(+00.00,+00.00,+0.00) FR(+00.00,+00.00,+0.00) BL(+00.00,+00.00,+0.00) BR(+00.00,+00.00,+0.00)",
             Drive.formatTelemetry(
                     0, 0, 0,
                     0, 0, 0,
                     0, 0, 0,
                     0, 0, 0
            ));

        // All positives
        assertEquals(
            "FL(+11.55,+22.66,+1.00) FR(+01.50,+01.50,+0.50) BL(+00.50,+00.50,+0.05) BR(+01.50,+01.50,+0.50)",
            Drive.formatTelemetry(
                    11.55, 22.66, 1.0,
                    1.5,   1.5,   0.5,
                    0.5,   0.5,   0.05,
                    1.5,   1.5,   0.5
            ));

        // All negatives
        assertEquals(
             "FL(-11.55,-22.66,-1.00) FR(-01.50,-01.50,-0.50) BL(-00.50,-00.50,-0.05) BR(-01.50,-01.50,-0.50)",
             Drive.formatTelemetry(
                     -11.55, -22.66, -1.0,
                     -1.5, -1.5, -0.5,
                     -0.5, -0.5, -0.05,
                     -1.5, -1.5, -0.5
            ));
}
}
