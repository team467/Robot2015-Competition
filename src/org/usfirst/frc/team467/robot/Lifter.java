package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;
import org.usfirst.frc.team467.robot.DriverStation2015.Speed;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Talon;

public class Lifter
{
    private static final Logger LOGGER = Logger.getLogger(Lifter.class);

    private static Lifter elevator = null;

    private Talon lifterMotorBottom = null;
    private Talon lifterMotorTop = null;
    private DigitalInput topStop = null;
    private DigitalInput topSlow = null;
    private DigitalInput bottomSlow = null;
    private DigitalInput bottomStop = null;

    private LifterZoneTypes currentZone = LifterZoneTypes.SLOW_ZONE_BOTTOM;
    private LifterDirection currentLiftDirection = LifterDirection.STOP;

    private PowerDistroBoard467 board = null;

    public static final double SLOW_SPEED_UP = -0.6;
    public static final double FAST_SPEED_UP = -1.0;

    public static final double SLOW_SPEED_DOWN = -SLOW_SPEED_UP;
    public static final double FAST_SPEED_DOWN = -FAST_SPEED_UP;

    // TODO Replace with practical value
    private static final double MAX_CURRENT_DOWN = 12;
    private static final double MAX_CURRENT_UP = 12;
    
    private final double MAX_RAMP_RATE = 02;

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
        topStop = new DigitalInput(RobotMap.SWITCH_UP_STOP);
        topSlow = new DigitalInput(RobotMap.SWITCH_UP_SLOW);
        bottomSlow = new DigitalInput(RobotMap.SWITCH_DOWN_SLOW);
        bottomStop = new DigitalInput(RobotMap.SWITCH_DOWN_STOP);

        board = PowerDistroBoard467.getInstance();
        currentZone = LifterZoneTypes.SLOW_ZONE_BOTTOM;
    }

    private boolean isJammedTop = false;
    private boolean isJammedBottom = false;
    
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
        double diff = speed - oldSpeed;

        // Adjust newSpeed to never exceed MAX_RAMP_RATE
        if (diff >= MAX_RAMP_RATE)
        {
            speed += MAX_RAMP_RATE;
        }
        else if (diff <= MAX_RAMP_RATE)
        {
            speed -= MAX_RAMP_RATE;
        }

        // Set both lifter motors to the new adjusted speed.
        lifterMotorBottom.set(speed);
        lifterMotorTop.set(speed);
    }
    
    /**
     * Lifter implementation that has no limit switch stops.
     * 
     * @param lifterDirection
     */
    public void driveLifter(LifterDirection lifterDirection, Speed speed)
    {
        if (board.getLifterTopCurrent() > MAX_CURRENT_UP - 1 || board.getLifterBottomCurrent() > MAX_CURRENT_UP - 1)
        {
            LOGGER.debug("LIFT CURRENT: BOTTOM: " + board.getLifterBottomCurrent() + " TOP: " + board.getLifterTopCurrent());
        }
        switch (lifterDirection)
        {
            case UP:
                LOGGER.debug("UP");
                DriverStation2015.getInstance().setLifterLED(DriverStation2015.LED_LIFTER_TOP_STOP, isJammedTop);
                if (!isJammedTop)
                {
                    isJammedTop = (board.getLifterBottomCurrent() > MAX_CURRENT_UP)
                            || (board.getLifterTopCurrent() > MAX_CURRENT_UP);
                    isJammedBottom = false;
                }
                if (isJammedTop)
                {
                    stop();
                }
                else
                {
                    switch (speed)
                    {
                        case FAST:
                            set(FAST_SPEED_UP);
                            break;
                        case SLOW:
                            set(SLOW_SPEED_UP);
                            break;
                    }
                    break;
                }
                break;

            case DOWN:
                LOGGER.debug("DOWN");
                DriverStation2015.getInstance().setLifterLED(DriverStation2015.LED_LIFTER_BOTTOM_STOP, isJammedBottom);
                if (!isJammedBottom)
                {
                    isJammedBottom = (board.getLifterBottomCurrent() > MAX_CURRENT_DOWN)
                            || (board.getLifterTopCurrent() > MAX_CURRENT_DOWN);
                    isJammedTop = false;
                }
                if (isJammedBottom)
                {
                    stop();
                }
                else
                {
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
    }

    /**
     * Updates the logic of the lifter to set current state.
     */
    public void update()
    {

        if (currentZone == LifterZoneTypes.STOP_ZONE_BOTTOM)
        {
            if (!bottomStop.get() && currentLiftDirection == LifterDirection.UP)
                currentZone = LifterZoneTypes.SLOW_ZONE_BOTTOM;
        }
        else if (currentZone == LifterZoneTypes.SLOW_ZONE_BOTTOM)
        {
            if (bottomStop.get() && currentLiftDirection == LifterDirection.DOWN)
                currentZone = LifterZoneTypes.STOP_ZONE_BOTTOM;
            else if (bottomSlow.get() && currentLiftDirection == LifterDirection.UP)
                currentZone = LifterZoneTypes.FAST_ZONE;
        }
        else if (currentZone == LifterZoneTypes.FAST_ZONE)
        {
            if (topSlow.get() && currentLiftDirection == LifterDirection.UP)
                currentZone = LifterZoneTypes.SLOW_ZONE_TOP;
            else if (bottomSlow.get() && currentLiftDirection == LifterDirection.DOWN)
                currentZone = LifterZoneTypes.STOP_ZONE_BOTTOM;
        }
        else if (currentZone == LifterZoneTypes.SLOW_ZONE_TOP)
        {
            if (topStop.get() && currentLiftDirection == LifterDirection.UP)
                currentZone = LifterZoneTypes.STOP_ZONE_TOP;
            else if (topSlow.get() && currentLiftDirection == LifterDirection.DOWN)
                currentZone = LifterZoneTypes.FAST_ZONE;
        }
        else if (currentZone == LifterZoneTypes.STOP_ZONE_TOP)
        {
            if (!topStop.get() && currentLiftDirection == LifterDirection.DOWN)
                currentZone = LifterZoneTypes.SLOW_ZONE_TOP;
        }
    }

}

/**
 * Used for maintaining lifter direction.
 * 
 * @author kyle
 *
 */
enum LifterDirection
{
    UP, DOWN, STOP
}

/**
 * Zone types internal to the lifter
 * 
 * @author kyle
 *
 */
enum LifterZoneTypes
{
    STOP_ZONE_BOTTOM, SLOW_ZONE_BOTTOM, FAST_ZONE, STOP_ZONE_TOP, SLOW_ZONE_TOP
}

/**
 * Types of lifting that can occur
 * 
 * @author kyle
 *
 */
enum LiftTypes
{
    LIFT_UP_SLOW, LIFT_DOWN_SLOW, LIFT_UP_FAST, LIFT_DOWN_FAST, NO_LIFT
}
