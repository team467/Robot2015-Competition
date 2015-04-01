package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;
import org.usfirst.frc.team467.robot.DriverStation2015.Speed;

import edu.wpi.first.wpilibj.Talon;

public class Lifter
{
    private static final Logger LOGGER = Logger.getLogger(Lifter.class);

    private static Lifter elevator = null;

    private Talon lifterMotorBottom = null;
    private Talon lifterMotorTop = null;
    
    private PowerDistroBoard467 board = null;

    public static final double SLOW_SPEED_UP = -0.6;
    public static final double FAST_SPEED_UP = -1.0;

    public static final double SLOW_SPEED_DOWN = -SLOW_SPEED_UP;
    public static final double FAST_SPEED_DOWN = -FAST_SPEED_UP;

    private static final double MAX_CURRENT_DOWN = 20;
    private static final double MAX_CURRENT_UP = 20;
    
    private double MAX_RAMP_RATE = 0.1;

    /**
     * Gets the singleton instance of the elevator
     * 
     * @return
     */
    public static Lifter getInstance()
    {
        if (elevator == null)
        {
            elevator = new Lifter();
        }
        return elevator;
    }

    /**
     * Private constructor
     */
    private Lifter()
    {
        lifterMotorBottom = new Talon(RobotMap.LIFTER_MOTOR_CHANNEL_BOTTOM);
        lifterMotorTop = new Talon(RobotMap.LIFTER_MOTOR_CHANNEL_TOP);

        board = PowerDistroBoard467.getInstance();
    }

    private boolean isJammedTop = false;
    private boolean isJammedBottom = false;
    
    /**
     * Stop driving lifter motors
     * 
     */
    public void stop()
    {
        set(0);
    }
    
    /**
     * Sets the speed of the lifter motors; <br>
     * stops current spike on sudden speed change.
     * 
     * @param speed
     */
    public void set(double speed)
    {
        double oldSpeed = lifterMotorTop.get();
        
//        MAX_RAMP_RATE = station.getLifterRampRate();
        
        speed = RateLimiter.limit(oldSpeed, speed, MAX_RAMP_RATE);

        lifterMotorBottom.set(speed);
        lifterMotorTop.set(speed);
    }
    
    public void driveLifter(LifterDirection lifterDirection)
    {
        driveLifter(lifterDirection, Speed.FAST);
    }
    
    /**
     * Lifter implementation that has no limit switch stops.
     * 
     * @param lifterDirection
     */
    public void driveLifter(LifterDirection lifterDirection, Speed speed)
    {
        boolean topCurrentStop = (board.getLifterTopCurrent() > MAX_CURRENT_UP - 1);
        boolean bottomCurrentStop = (board.getLifterBottomCurrent() > MAX_CURRENT_UP - 1);
        if (topCurrentStop || bottomCurrentStop)
        {
            LOGGER.debug("LIFT CURRENT: BOTTOM: " + board.getLifterBottomCurrent() + " TOP: " + board.getLifterTopCurrent());
        }
        
        switch (lifterDirection)
        {
            case UP:
                LOGGER.debug("UP");
                
                isJammedTop = isJammedTop || (board.getLifterBottomCurrent() > MAX_CURRENT_UP) || (board.getLifterTopCurrent() > MAX_CURRENT_UP);
                
                if (isJammedTop)
                {
                    stop();
                }
                else
                {
                    isJammedBottom = false;
                    
                    switch (speed)
                    {
                        case FAST:
                            set(FAST_SPEED_UP);
                            break;
                        case SLOW:
                            set(SLOW_SPEED_UP);
                            break;
                    }
                }
                break;

            case DOWN:
                LOGGER.debug("DOWN");
                
                isJammedBottom = isJammedBottom || (board.getLifterBottomCurrent() > MAX_CURRENT_DOWN) || (board.getLifterTopCurrent() > MAX_CURRENT_DOWN);
               
                if (isJammedBottom)
                {
                    stop();
                }
                else
                {
                    isJammedTop = false;

                    switch (speed)
                    {
                        case FAST:
                            set(FAST_SPEED_DOWN);
                            break;
                        case SLOW:
                            set(SLOW_SPEED_DOWN);
                            break;
                    }
                    break;
                }

            default:
                stop();
                isJammedTop = false;
                isJammedBottom = false;
                break;
        }
        
        DriverStation2015.getInstance().setLifterLED(DriverStation2015.LED_LIFTER_TOP_STOP, isJammedTop);
        DriverStation2015.getInstance().setLifterLED(DriverStation2015.LED_LIFTER_BOTTOM_STOP, isJammedBottom);
    }

}

/**
 * Used for maintaining lifter direction.
 *
 */
enum LifterDirection
{
    UP, DOWN, STOP
}

/**
 * Zone types internal to the lifter
 *
 */
enum LifterZone
{
    STOP_ZONE_BOTTOM, SLOW_ZONE_BOTTOM, FAST_ZONE, STOP_ZONE_TOP, SLOW_ZONE_TOP
}

