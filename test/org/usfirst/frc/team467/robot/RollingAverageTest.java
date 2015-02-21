package org.usfirst.frc.team467.robot;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RollingAverageTest
{    
    private final double MAX_DELTA = 0.0001;
    
    @Test
    public void averageTest()
    {
        RollingAverage rolling = new RollingAverage(5);

        for (int i = 0; i <= 5; i++)
        {
            rolling.add(i);
        }
        assertEquals(3.0, rolling.getAverage(), MAX_DELTA);
        
        rolling.add(10.0);
        rolling.add(14.0);
        rolling.add(12.0);
        rolling.add(11.0);
        rolling.add(13.0);
        assertEquals(12.0, rolling.getAverage(), MAX_DELTA);

        rolling.add(20.0);
        assertEquals(14.0, rolling.getAverage(), MAX_DELTA);
    }

}
