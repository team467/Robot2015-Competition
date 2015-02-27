package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.Talon;

public class Claw
{
    private static final Logger LOGGER = Logger.getLogger(Lifter.class);

    private static Claw claw = null;

    private Talon clawMotor = null;
    PowerDistroBoard467 board = null;
    DriverStation2015 driverstation= null;

    private final double OPEN_SPEED_SLOW = -0.6;
    private final double OPEN_SPEED_FAST = -0.8;
    private final double CLOSE_SPEED_SLOW = -OPEN_SPEED_SLOW;
    private final double CLOSE_SPEED_FAST = -OPEN_SPEED_FAST;


    private final double MAX_CURRENT_GRIP = 7;
    private final double MAX_CURRENT_UNGRIP = 15;

    private boolean m_isClosed = false;
    private boolean m_isFullyOpen = false;

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
        driverstation = DriverStation2015.getInstance();
    }

    /**
     * Basic move without current or limit switching
     * 
     * @param clawDir
     * @param turbo
     */

    public void moveClaw(ClawMoveDirection clawDir, boolean turbo)
    {
        LOGGER.debug("CLAW CURRENT: " + board.getClawCurrent());
        switch (clawDir)
        {
            case CLOSE:
                LOGGER.debug("CLOSE");

                if (!m_isClosed)
                {
                    m_isClosed = (board.getClawCurrent() > MAX_CURRENT_GRIP);
                    m_isFullyOpen = false;
                    clawMotor.set((turbo) ? CLOSE_SPEED_FAST : CLOSE_SPEED_SLOW);
                }
                else
                {                    
                    clawMotor.set(0);
                }
                break;

            case OPEN:
                LOGGER.debug("OPEN");

                if (!m_isFullyOpen)
                {
                    m_isFullyOpen = (board.getClawCurrent() > MAX_CURRENT_UNGRIP);
                    m_isClosed = false;
                    clawMotor.set((turbo) ? OPEN_SPEED_FAST : OPEN_SPEED_SLOW);
                }
                else
                {
                    clawMotor.set(0);
                }
                break;

            case STOP:
                clawMotor.set(0);
                m_isClosed = false;
                m_isFullyOpen = false;                
                break;
        }
        driverstation.setClawLED(m_isClosed || m_isFullyOpen);
    }

    /**
     * When something is in the claw
     * 
     * @return isClosed
     */
    public boolean isClosed()
    {
        return m_isClosed;
    }

    /**
     * When claw can't open any further
     * 
     * @return isFullyOpen
     */
    public boolean isFullyOpen()
    {
        return m_isFullyOpen;
    }

}

enum ClawMoveDirection
{
    OPEN, CLOSE, STOP
}

