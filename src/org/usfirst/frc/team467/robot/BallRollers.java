package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;


import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.MotorSafetyHelper;


public class BallRollers
{
    private static final Logger LOGGER = Logger.getLogger(BallRollers.class);

    private final CANTalon rollerMotor;
    //private final CANTalon manipMotor;
    private final MotorSafetyHelper safetyRoller;
    //private final MotorSafetyHelper safetyManip;
    private final double rollerOutMotorSpeed = 1;
    private final double rollerInMotorSpeed = -0.7;
    //private final double manipMotorSpeed = 0.2;

    // TODO Sensor
    
    public BallRollers(int motorChannelRoller)
    {
        rollerMotor = new CANTalon(motorChannelRoller);
        //manipMotor = new CANTalon(motorChannelManip);
        safetyRoller = new MotorSafetyHelper(rollerMotor);
        //safetyManip = new MotorSafetyHelper(manipMotor);
    }
    
    public void stopRoller()
    {
        rollerMotor.set(0.0);
        safetyRoller.feed();
    }
    public void stopManip()
    {
        //manipMotor.set(0.0);
        //safetyManip.feed();
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
//                if (!isUp()) {
//                    LOGGER.info("GOING UP");
//                    manipMotor.set(manipMotorSpeed);
//                    safetyManip.feed();
//                }
//                else {
//                    LOGGER.info("IS UP");
//                    stopManip();
//                }
                //manipMotor.set(manipMotorSpeed);
                break;
            case DOWN:
//                if (!isDown()) {
//                    LOGGER.info("GOING DOWN");
//                    manipMotor.set(-manipMotorSpeed);
//                    safetyManip.feed();
//                }
//                else {
//                    LOGGER.info("IS DOWN");
//                    stopManip();
//                }
                stopManip();
                break;
            case STOP:
                stopManip();
                break;
        }
    }

    
    private boolean isLoaded()
    {
        // TODO Work with sensor
        return false;
    }
    private boolean isUp()
    {
        //we need to detect the position of the arm
        return false;
    }
    private boolean isDown()
    {
      //we need to detect the position of the arm
        return false;
    }
    enum RollerDirection {
        IN, OUT, STOP
    }
    enum ManipPosition {
        UP, DOWN, STOP
    }
}
