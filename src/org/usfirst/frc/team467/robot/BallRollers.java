package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.MotorSafetyHelper;


public class BallRollers
{
    private static final Logger LOGGER = Logger.getLogger(BallRollers.class);

    private final CANTalon rollerMotor;
    private final CANTalon manipMotor;
    private final MotorSafetyHelper safetyRoller;
    private final MotorSafetyHelper safetyManip;
    private final double rollerOutMotorSpeed = 1;
    private final double rollerInMotorSpeed = -0.7;
    
    private PowerDistroBoard467 board = null;
    
    private static final double MAX_CURRENT = 20;
    private final double manipMotorSpeed = 0.3;
    
    boolean atTop;

    // TODO Sensor
    
    public BallRollers(int motorChannelRoller, int motorChannelManip)
    {
        rollerMotor = new CANTalon(motorChannelRoller);
        manipMotor = new CANTalon(motorChannelManip);
        safetyRoller = new MotorSafetyHelper(rollerMotor);
        safetyManip = new MotorSafetyHelper(manipMotor);
    }
    
    public void stopRoller()
    {
        rollerMotor.set(0.0);
        safetyRoller.feed();
    }
    public void stopManip()
    {
        manipMotor.set(0.0);
        safetyManip.feed();
    }
    
    public void runRoller (RollerDirection rollerDirection) {
        
        switch(rollerDirection) {
            case IN:
                if (isLoaded())
                {
                    stopRoller();
                    return;
                }
                LOGGER.info("IN");
                rollerMotor.set(-rollerInMotorSpeed);
                safetyRoller.feed();
                break;
            case OUT:
                LOGGER.info("OUT");
                rollerMotor.set(rollerOutMotorSpeed);
                safetyRoller.feed();
                break;
            case STOP:
                stopRoller();
                break;
        }
    }
        
    public void runManipulator (ManipPosition manipPosition) {
        
        
        //for now, we just control the Manipulator arm manually until we can detect the position of the arm
        switch(manipPosition) {
            case UP:
                if (board.getManipCurrent() < MAX_CURRENT && !atTop) {
                    LOGGER.info("GOING UP");
                    manipMotor.set(manipMotorSpeed);
                    safetyManip.feed();
                }
                else {
                    LOGGER.info("IS UP");
                    stopManip();
                    atTop = true;
                }
                //manipMotor.set(manipMotorSpeed);
                break;
            case DOWN:
                if (board.getManipCurrent() < MAX_CURRENT && atTop) {
                    LOGGER.info("GOING DOWN");
                    manipMotor.set(-manipMotorSpeed);
                    safetyManip.feed();
                }
                else {
                    LOGGER.info("IS DOWN");
                    stopManip();
                    atTop = false;
                }
                stopManip();
                break;
            case STOP:
                stopManip();
                break;
        }
        }

    public void in(double motorSpeed)
    {
        if (isLoaded())
        {
            stop();
            return;
        }
        LOGGER.info("IN");
        rollerMotor.set(motorSpeed);
        safetyRoller.feed();
    }
    
    public void out(double motorSpeed)
    {
        LOGGER.info("OUT");
        rollerMotor.set(-motorSpeed);
        safetyRoller.feed();
        }
    
    public void stop()
    {
        rollerMotor.set(0.0);
        safetyRoller.feed();
    }

    
    private boolean isLoaded()
    {
        // TODO Work with sensor
        return false;
    }
    enum RollerDirection {
        IN, OUT, STOP
    }
    enum ManipPosition {
        UP, DOWN, STOP
    }
}
