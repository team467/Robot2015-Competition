package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.Talon;

public class Claw
{
    private static final Logger LOGGER = Logger.getLogger(Lifter.class);

    private static Claw claw = null;

    private Talon clawMotor = null;

    PowerDistroBoard467 board = null;

    private final double OPEN_SPEED_SLOW = -0.4;
    private final double OPEN_SPEED_FAST = -0.8;
    private final double CLOSE_SPEED_SLOW = -OPEN_SPEED_SLOW;
    private final double CLOSE_SPEED_FAST = -OPEN_SPEED_FAST;

    private final double MAX_CURRENT_GRIP = 6;
    private final double MAX_CURRENT_UNGRIP = 4;

    private boolean isClosed = false;
    private boolean isFullyOpen = false;

    /**
     * Singleton instance of the robot.
     * 
     * @return
     */
    public static Claw getInstance()
    {
        if (claw == null)
        {
            claw = new Claw();
        }
        return claw;
    }

    /**
     * Private constructor
     */
    private Claw()
    {
        clawMotor = new Talon(RobotMap.CLAW_MOTOR_CHANNEL);

        board = PowerDistroBoard467.getInstance();
    }

    /**
     * Basic move without current or limit switching
     * 
     * @param clawDir
     * @param turbo
     */
    public void basicMoveClaw(ClawMoveDirection clawDir, boolean turbo)
    {
        LOGGER.debug("CLAW CURRENT: " + board.getClawCurrent());
        switch (clawDir)
        {
            case CLOSE:
                LOGGER.debug("CLOSE");
                clawMotor.set((turbo) ? CLOSE_SPEED_FAST : CLOSE_SPEED_SLOW);
                break;

            case OPEN:
                LOGGER.debug("OPEN");
                clawMotor.set((turbo) ? OPEN_SPEED_FAST : OPEN_SPEED_SLOW);
                break;

            case STOP:
                clawMotor.set(0);
                break;
        }
    }

    public void moveClawNew(ClawMoveDirection clawDir, boolean turbo)
    {
        LOGGER.debug("CLAW CURRENT: " + board.getClawCurrent());
        switch (clawDir)
        {
            case CLOSE:
                LOGGER.debug("CLOSE");                
                if (!isClosed)
                {
                    isClosed = (board.getClawCurrent() > MAX_CURRENT_UNGRIP);
                    isFullyOpen = false;
                    clawMotor.set((turbo) ? CLOSE_SPEED_FAST : CLOSE_SPEED_SLOW);
                }
                else
                {
                    clawMotor.set(0);
                }
                break;

            case OPEN:
                LOGGER.debug("OPEN");                
                if (!isFullyOpen)
                {
                    isFullyOpen = (board.getClawCurrent() > MAX_CURRENT_UNGRIP);
                    isClosed = false;
                    clawMotor.set((turbo) ? OPEN_SPEED_FAST : OPEN_SPEED_SLOW);
                }
                else
                {
                    clawMotor.set(0);
                }
                break;

            case STOP:
                clawMotor.set(0);
                break;
        }
    }

    /**
     * Moves the claw given the ClawMoveType
     * 
     * @param clawTypes
     */
    public void moveClaw(ClawMoveTypes clawTypes)
    {
        double current = board.getClawCurrent();
        switch (clawTypes)
        {
            case GRIP_SLOW:
                isClosed = (current > MAX_CURRENT_GRIP);
                if (!isClosed)
                {
                    clawMotor.set(CLOSE_SPEED_SLOW);
                }
                else
                {
                    clawMotor.set(0);
                }
                break;

            case GRIP_FAST:
                isClosed = (current > MAX_CURRENT_GRIP);
                if (!isClosed)
                {
                    clawMotor.set(CLOSE_SPEED_FAST);
                }
                else
                {
                    clawMotor.set(0);
                }
                break;

            case UNGRIP_SLOW:
                isFullyOpen = (current > MAX_CURRENT_UNGRIP);
                if (!isFullyOpen)
                {
                    clawMotor.set(OPEN_SPEED_SLOW);
                }
                else
                {
                    clawMotor.set(0);
                }
                break;

            case UNGRIP_FAST:
                isFullyOpen = (current > MAX_CURRENT_UNGRIP);
                if (!isFullyOpen)
                {
                    clawMotor.set(OPEN_SPEED_FAST);
                }
                else
                {
                    clawMotor.set(0);
                }
                break;

            default:
                clawMotor.set(0);
        }
    }

    /**
     * When something is in the claw
     * 
     * @return isClosed
     */
    public boolean isClosed()
    {
        return isClosed;
    }

    /**
     * When claw can't open any further
     * 
     * @return isFullyOpen
     */
    public boolean isFullyOpen()
    {
        return isFullyOpen;
    }

}

enum ClawMoveDirection
{
    OPEN, CLOSE, STOP
}

/**
 * Different movement types for the claw.
 * 
 * @author kyle
 *
 */
enum ClawMoveTypes
{
    GRIP_SLOW, GRIP_FAST, UNGRIP_SLOW, UNGRIP_FAST, STOP
}
