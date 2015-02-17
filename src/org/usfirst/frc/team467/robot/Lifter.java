package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Talon;

public class Lifter
{
    private static final Logger LOGGER = Logger.getLogger(Lifter.class);

    private static Lifter elevator = null;

    private Talon lifterMotor = null;
    private DigitalInput topStop = null;
    private DigitalInput topSlow = null;
    private DigitalInput bottomSlow = null;
    private DigitalInput bottomStop = null;

    private LifterZoneTypes currentZone = LifterZoneTypes.SLOW_ZONE_BOTTOM;
    private LifterDirection currentLiftDirection = LifterDirection.STOP;

    private PowerDistroBoard467 board = null;

    public static final double SLOW_SPEED_UP = -0.4;
    public static final double FAST_SPEED_UP = -1.0;

    public static final double SLOW_SPEED_DOWN = -SLOW_SPEED_UP;
    public static final double FAST_SPEED_DOWN = -FAST_SPEED_UP;

    // TODO Replace with practical value
    private static final double MAX_CURRENT_DOWN = 15;
    private static final double MAX_CURRENT_UP = 25;

    /**
     * Gets the singleton instance of the elevator
     * 
     * @return
     */
    public static Lifter getInstance()
    {
        if (elevator == null)
            elevator = new Lifter();
        return elevator;
    }

    /**
     * Private constructor
     */
    private Lifter()
    {
        lifterMotor = new Talon(RobotMap.LIFTER_MOTOR_CHANNEL);
//        topStop = new DigitalInput(RobotMap.SWITCH_UP_STOP);
//        topSlow = new DigitalInput(RobotMap.SWITCH_UP_SLOW);
//        bottomSlow = new DigitalInput(RobotMap.SWITCH_DOWN_SLOW);
//        bottomStop = new DigitalInput(RobotMap.SWITCH_DOWN_STOP);

        board = PowerDistroBoard467.getInstance();        
        currentZone = LifterZoneTypes.SLOW_ZONE_BOTTOM;
    }

    /**
     * Lifter implementation that has no turbo speed or limit switch stops.
     * 
     * @param lifterDirection
     */
    public void basicDriveLifter(LifterDirection lifterDirection, boolean turbo)
    {
        LOGGER.debug("LIFT CURRENT: " + board.getLifterCurrent());
        switch (lifterDirection)
        {
            case UP:
                LOGGER.debug("UP");
                lifterMotor.set((turbo) ? FAST_SPEED_UP : SLOW_SPEED_UP);
                break;

            case DOWN:
                LOGGER.debug("DOWN");
                lifterMotor.set((turbo) ? FAST_SPEED_DOWN : SLOW_SPEED_DOWN);
                break;

            default:
                lifterMotor.set(0);
                break;
        }
    }

    private boolean isJammedTop = false;
    private boolean isJammedBottom = false;
    
    /**
     * Lifter implementation that has no limit switch stops.
     * 
     * @param lifterDirection
     */
    public void driveLifter(LifterDirection lifterDirection, boolean turbo)
    {
        LOGGER.debug("LIFT CURRENT: " + board.getLifterCurrent());
        switch (lifterDirection)
        {
            case UP:
                LOGGER.debug("UP");    
                DriverStation2015.getInstance().setLifterLED(DriverStation2015.LED_LIFTER_TOP_STOP, isJammedTop);
                if (!isJammedTop)
                {
                    isJammedTop = (board.getLifterCurrent() > MAX_CURRENT_UP);
                    isJammedBottom = false;
                    lifterMotor.set((turbo) ? FAST_SPEED_UP : SLOW_SPEED_UP);
                }
                else
                {
                    lifterMotor.set(0);
                }
                break;

            case DOWN:
                LOGGER.debug("DOWN");
                DriverStation2015.getInstance().setLifterLED(DriverStation2015.LED_LIFTER_BOTTOM_STOP, isJammedBottom);
                if (!isJammedBottom)
                {
                    isJammedBottom = (board.getLifterCurrent() > MAX_CURRENT_DOWN);
                    isJammedTop = false;
                    lifterMotor.set((turbo) ? FAST_SPEED_DOWN : SLOW_SPEED_DOWN);
                }
                else
                {
                    lifterMotor.set(0);
                }
                break;

            default:
                lifterMotor.set(0);
                isJammedTop = false;
                isJammedBottom = false;
                break;
        }
    }

    /**
     * Sets the lift direction and updates the
     * 
     * @param lifterDirection
     * @param turbo
     */
    public void setLiftNew(LifterDirection lifterDirection, boolean turbo)
    {
        switch (currentZone)
        {
            case STOP_ZONE_TOP:
                switch (lifterDirection)
                {
                    case DOWN:
                        if (!topStop.get())
                            currentZone = LifterZoneTypes.SLOW_ZONE_TOP;
                        lifterMotor.set(SLOW_SPEED_DOWN);
                        break;

                    default:// no drive
                        lifterMotor.set(0);
                        break;
                }
                break;

            case SLOW_ZONE_TOP:
                switch (lifterDirection)
                {
                    case UP:
                        if (topStop.get())
                            currentZone = LifterZoneTypes.STOP_ZONE_TOP;
                        lifterMotor.set(SLOW_SPEED_UP);
                        break;

                    case DOWN:
                        if (topSlow.get())
                            currentZone = LifterZoneTypes.FAST_ZONE;
                        lifterMotor.set((turbo) ? FAST_SPEED_DOWN : SLOW_SPEED_DOWN);
                        break;

                    default: // no drive
                        lifterMotor.set(0);
                        break;
                }
                break;

            case FAST_ZONE:

                if (topSlow.get() && currentLiftDirection == LifterDirection.UP)
                    currentZone = LifterZoneTypes.SLOW_ZONE_TOP;
                else if (bottomSlow.get() && currentLiftDirection == LifterDirection.DOWN)
                    currentZone = LifterZoneTypes.STOP_ZONE_BOTTOM;
                switch (lifterDirection)
                {

                    case UP:
                        if (topSlow.get())
                            currentZone = LifterZoneTypes.SLOW_ZONE_TOP;
                        lifterMotor.set((turbo) ? FAST_SPEED_UP : SLOW_SPEED_UP);
                        break;

                    case DOWN:
                        if (bottomSlow.get())
                            currentZone = LifterZoneTypes.SLOW_ZONE_BOTTOM;
                        lifterMotor.set((turbo) ? FAST_SPEED_DOWN : SLOW_SPEED_DOWN);
                        break;

                    default:// no drive
                        lifterMotor.set(0);
                        break;
                }
                break;

            case SLOW_ZONE_BOTTOM:
                if (bottomStop.get() && currentLiftDirection == LifterDirection.DOWN)
                    currentZone = LifterZoneTypes.STOP_ZONE_BOTTOM;
                else if (bottomSlow.get() && currentLiftDirection == LifterDirection.UP)
                    currentZone = LifterZoneTypes.FAST_ZONE;
                switch (lifterDirection)
                {
                    case UP:
                        if (bottomSlow.get())
                            currentZone = LifterZoneTypes.FAST_ZONE;
                        lifterMotor.set((turbo) ? FAST_SPEED_UP : SLOW_SPEED_UP);
                        break;

                    case DOWN:
                        if (bottomStop.get())
                            currentZone = LifterZoneTypes.STOP_ZONE_BOTTOM;
                        lifterMotor.set(SLOW_SPEED_DOWN);
                        break;

                    default: // no drive
                        lifterMotor.set(0);
                        break;
                }
                break;

            case STOP_ZONE_BOTTOM:
                if (!bottomStop.get() && currentLiftDirection == LifterDirection.UP)
                    currentZone = LifterZoneTypes.SLOW_ZONE_TOP;
                // drive
                switch (lifterDirection)
                {
                    case UP:
                        lifterMotor.set(SLOW_SPEED_UP);
                        break;

                    default:// no drive
                        lifterMotor.set(0);
                        break;
                }
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

    /**
     * Sets the method to go about lifting.
     * 
     * @param liftType
     */
    public void setLift(LiftTypes liftType)
    {
        double current = board.getLifterCurrent();
        if (current > MAX_CURRENT_DOWN)
        {
            lifterMotor.set(0.0);
            return;
        }
        if (liftType == LiftTypes.NO_LIFT)
        {
            lifterMotor.set(0.0);
            currentLiftDirection = LifterDirection.STOP;
        }
        else if (liftType == LiftTypes.LIFT_DOWN_SLOW)
        {
            if (currentZone != LifterZoneTypes.STOP_ZONE_BOTTOM)
            {
                lifterMotor.set(SLOW_SPEED_DOWN);
                currentLiftDirection = LifterDirection.DOWN;
            }
            else
            {
                lifterMotor.set(0);
                currentLiftDirection = LifterDirection.STOP;
            }
        }
        else if (liftType == LiftTypes.LIFT_UP_SLOW)
        {
            if (currentZone != LifterZoneTypes.STOP_ZONE_TOP)
            {
                lifterMotor.set(SLOW_SPEED_UP);
                currentLiftDirection = LifterDirection.UP;
            }
            else
            {
                lifterMotor.set(0);
                currentLiftDirection = LifterDirection.STOP;
            }
        }
        else if (liftType == LiftTypes.LIFT_DOWN_FAST)
        {
            if (currentZone == LifterZoneTypes.FAST_ZONE || currentZone == LifterZoneTypes.SLOW_ZONE_TOP
                    || currentZone == LifterZoneTypes.STOP_ZONE_TOP)
            {
                lifterMotor.set(FAST_SPEED_DOWN);
                currentLiftDirection = LifterDirection.DOWN;
            }
            else if (currentZone != LifterZoneTypes.STOP_ZONE_BOTTOM)
            {
                lifterMotor.set(SLOW_SPEED_DOWN);
                currentLiftDirection = LifterDirection.DOWN;
            }
            else
            // in stop zone bottom
            {
                lifterMotor.set(0);
                currentLiftDirection = LifterDirection.STOP;
            }
        }
        else if (liftType == LiftTypes.LIFT_UP_FAST)
        {
            if (currentZone == LifterZoneTypes.FAST_ZONE || currentZone == LifterZoneTypes.SLOW_ZONE_BOTTOM
                    || currentZone == LifterZoneTypes.STOP_ZONE_BOTTOM)
            {
                lifterMotor.set(FAST_SPEED_UP);
                currentLiftDirection = LifterDirection.UP;
            }
            else if (currentZone != LifterZoneTypes.STOP_ZONE_TOP)
            {
                lifterMotor.set(SLOW_SPEED_UP);
                currentLiftDirection = LifterDirection.UP;
            }
            else
            // in stop zone top
            {
                lifterMotor.set(0);
                currentLiftDirection = LifterDirection.STOP;
            }
        }
        else
        // Should never get here
        {
            lifterMotor.set(0);
            currentLiftDirection = LifterDirection.STOP;
        }
    }

    /**
     * Gets the lift zone of the lifter.
     * 
     * @return
     */
    public LifterZoneTypes getLiftZone()
    {
        return this.currentZone;
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
