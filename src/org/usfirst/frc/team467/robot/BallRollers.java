package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.MotorSafetyHelper;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class BallRollers
{
    private static final Logger LOGGER = Logger.getLogger(BallRollers.class);

    private final CANTalon rollerMotor;
    private final Talon manipMotor;
    private final MotorSafetyHelper safetyRoller;
    private final MotorSafetyHelper safetyManip;
    private final double rollerOutMotorSpeed = 1;
    private final double rollerInMotorSpeed = 0.7;
    
    private DigitalInput isRetractedSwitch;
    
    private PowerDistroBoard467 board = null;
    
    private static final double MAX_CURRENT_OUT = 11;
    private static final double MAX_CURRENT_IN = 11;
    private final double manipMotorSpeed = 0.9;
    
    //boolean isRetracted;
    boolean isExtended;

    // TODO Sensor
    
    public BallRollers(int motorChannelRoller, int motorChannelManip)
    {
        board = PowerDistroBoard467.getInstance();
        rollerMotor = new CANTalon(motorChannelRoller);
        manipMotor = new Talon(motorChannelManip);
        safetyRoller = new MotorSafetyHelper(rollerMotor);
        safetyManip = new MotorSafetyHelper(manipMotor);
        try {
            isRetractedSwitch = new DigitalInput(2);
        } catch (Exception e) {
            LOGGER.error("Cannot initialize retracted switch", e);
        }
        reset();
    }
    
    public void reset()
    {
        //isRetracted = false;
        isExtended = false;
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
    public void retractManip()
    {
        manipMotor.set(manipMotorSpeed);
    }
    public void extendManip()
    {
        manipMotor.set(-manipMotorSpeed);
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
        
    public void runManipulator (ManipIntent manipPosition) {
        String logstring;
        
        //for now, we just control the Manipulator arm manually until we can detect the position of the arm
        switch(manipPosition) {
            case SHOULD_EXTEND:
                if (isExtended) {
                    stopManip();
                    logstring = "Extended";
                    LOGGER.info(logstring);
                }
                else if (board.getManipCurrent() < MAX_CURRENT_OUT) {
                    logstring = "Extending:" + String.valueOf(board.getManipCurrent());
                    LOGGER.info(logstring);
                    extendManip();
                }
                else {
                    logstring = "Extended";
                    LOGGER.info(logstring);
                    stopManip();
                    isExtended = true;
                }
                break;
            case SHOULD_RETRACT:
//                if (isRetracted) {
//                    stopManip();
//                    logstring = "Retracted";
//                    LOGGER.info(logstring);
//                }
//                else if (!isRetracted()) {
//                    manipMotor.set(-manipMotorSpeed);
//                    logstring = "Retracting";
//                }
//                else {
//                    logstring = "Retracted";
//                    LOGGER.info(logstring);
//                    stopManip();
//                    isRetracted = true;
//                }
                if (isRetracted()) {
                      stopManip();
                      logstring = "Retracted";
                      LOGGER.info(logstring);
                 }
                else {
                    retractManip();
                    logstring = "Retracting";
                }
                isExtended = false;
                break;
            default:
                logstring = "Is Stopped";
                LOGGER.info(logstring);
                stopManip();
                break;
            }
        SmartDashboard.putString("DB/String 9", logstring);
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
    private boolean isRetracted()
    {
        LOGGER.info("isRetracted=" + isRetractedSwitch.get());
        return isRetractedSwitch.get();
    }
}
