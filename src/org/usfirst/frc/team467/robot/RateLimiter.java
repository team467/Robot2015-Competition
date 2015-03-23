package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

public class RateLimiter
{
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(RateLimiter.class);
    
    /**
     * Limit rate at which speed can change to maxRampRate
     *
     * @param prevSpeed
     * @param newSpeed - Desired speed
     * @param maxRampRate
     * @return Updated speed
     */
    public static double limit(double prevSpeed, double newSpeed, double maxRampRate)
    {
        maxRampRate = Math.abs(maxRampRate);
        if (Math.abs(newSpeed - prevSpeed) > maxRampRate)
        {
            if (newSpeed > prevSpeed)
            {
                newSpeed = prevSpeed + maxRampRate;
            }
            else
            {
                newSpeed = prevSpeed - maxRampRate;
            }
        }
        
        return newSpeed;
    }
}
