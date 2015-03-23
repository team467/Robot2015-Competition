package org.usfirst.frc.team467.robot;

/**
 * Utility class to simply limit the rate of change of a control variable.
 */
public class RateLimiter
{   
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
