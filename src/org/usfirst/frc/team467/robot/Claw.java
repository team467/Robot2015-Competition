package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;
import org.usfirst.frc.team467.robot.DriverStation2015.Speed;

import edu.wpi.first.wpilibj.Talon;

public class Claw
{
    private static final Logger LOGGER = Logger.getLogger(Claw.class);

    private static Claw claw = null;

    private Talon clawMotor = null;
    PowerDistroBoard467 board = null;
    DriverStation2015 driverstation= null;
    
    // Change these if joystick goes the wrong way
    private final double OPEN_SPEED_SLOW = 0.6;
    private final double OPEN_SPEED_FAST = 0.8;
    private final double CLOSE_SPEED_SLOW = -OPEN_SPEED_SLOW;
    private final double CLOSE_SPEED_FAST = -OPEN_SPEED_FAST;

    private final double HIGH_MAX_CURRENT_GRIP = 20;
    private final double LOW_MAX_CURRENT_GRIP = 15;

    private final double MAX_CURRENT_UNGRIP = 20;
    
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
    
    public void stop()
    {
        clawMotor.set(0);
    }
    
    /**
     * Basic move without current or limit switching
     * 
     * @param clawDir
     * @param speed
     */
    public void moveClaw(ClawMoveDirection clawDir, boolean lowCurrentLimit)
    {
        Speed speed = Speed.FAST;
        final double clawCurrent = board.getClawCurrent();
        
        LOGGER.debug("moveClaw CURRENT=" + clawCurrent + " previous m_isClosed=" + m_isClosed
                + " clawDir=" + clawDir + " speed=" + speed);
        switch (clawDir)
        {
            case CLOSE:
                LOGGER.debug("CLOSE");
                
                if (!lowCurrentLimit)
                {
                    m_isClosed = m_isClosed || (clawCurrent > HIGH_MAX_CURRENT_GRIP);
                }
                else
                {
                    m_isClosed = m_isClosed || (clawCurrent > LOW_MAX_CURRENT_GRIP);
                }
                
                if (m_isClosed)
                {
                    stop();
                }
                else
                {
                    m_isFullyOpen = false;
                    switch (speed)
                    {
                        case FAST:
                            clawMotor.set(CLOSE_SPEED_FAST);
                            break;
                        case SLOW:
                            clawMotor.set(CLOSE_SPEED_SLOW);
                            break;
                    }
                }
                break;

            case OPEN:
                LOGGER.debug("OPEN");

                m_isFullyOpen = m_isFullyOpen || (clawCurrent > MAX_CURRENT_UNGRIP);
                
                if (m_isFullyOpen)
                {
                    stop();
                }
                else
                {
                    m_isClosed = false;
                    switch (speed)
                    {
                        case FAST:
                            clawMotor.set(OPEN_SPEED_FAST);
                            break;
                        case SLOW:
                            clawMotor.set(OPEN_SPEED_SLOW);
                            break;
                    }
                }
                break;

            case STOP:
                stop();
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

