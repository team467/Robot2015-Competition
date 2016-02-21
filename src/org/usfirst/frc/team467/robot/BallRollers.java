package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.MotorSafetyHelper;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class BallRollers
{
    private static final Logger LOGGER = Logger.getLogger(BallRollers.class);

    private final CANTalon rollerMotor;
    private final CANTalon manipMotor;
    private final MotorSafetyHelper safetyRoller;
    private final MotorSafetyHelper safetyManip;
    private final double rollerOutMotorSpeed = 1;
    private final double rollerInMotorSpeed = 0.7;
    
    private PowerDistroBoard467 board = null;
    
    private static final double MAX_CURRENT = 20;
    private final double manipMotorSpeed = 1;
    
    boolean isRetracted = false;
    boolean isExtended = false;
    // TODO Sensor
    
    public BallRollers(int motorChannelRoller, int motorChannelManip)
    {
        board = PowerDistroBoard467.getInstance();
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
        
    public void runManipulator (ManipIntent manipPosition)
    {
        String logString = "";
        
        switch(manipPosition) {
            case SHOULD_EXTEND:
                if (isExtended) {
                    stopManip();
                }
                else if (board.getManipCurrent() < MAX_CURRENT) {
                    logString = "EXTENDING: " + String.valueOf(board.getManipCurrent());
                    manipMotor.set(manipMotorSpeed);
                }
                else {
                    logString = "EXTENDED";
                    stopManip();
                    isExtended = true;
                }
                isRetracted = false;
                break;
            case SHOULD_RETRACT:
                if (isRetracted) {
                    stopManip();
                }
                else if (board.getManipCurrent() < MAX_CURRENT) {
                    logString = "RETRACTING: " + String.valueOf(board.getManipCurrent());
                    manipMotor.set(-manipMotorSpeed);
                }
                else {
                    logString = "RETRACTED";
                    stopManip();
                    isRetracted = true;
                }
                isExtended = false;
                break;
            default:
                logString = "IS STOPPED";
                stopManip();
                break;
        }

        LOGGER.info(logString);
        SmartDashboard.putString("DB/String 9", logString);
        safetyManip.feed();
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
    enum ManipIntent {
        SHOULD_EXTEND, SHOULD_RETRACT, SHOULD_STOP
    }
}
