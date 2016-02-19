package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;


import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.MotorSafetyHelper;


public class BallRollers
{
    private static final Logger LOGGER = Logger.getLogger(BallRollers.class);

    private final CANTalon motor;
    private final MotorSafetyHelper safety;
    private final double motorSpeed = 1;

    // TODO Sensor
    
    public BallRollers(int motorChannel)
    {
        motor = new CANTalon(motorChannel);
        safety = new MotorSafetyHelper(motor);
    }
    
    public void in()
    {
        if (isLoaded())
        {
            stop();
            return;
        }
        LOGGER.info("IN");
        motor.set(motorSpeed);
        safety.feed();
        LOGGER.info("Troll");
    }
    
    public void out()
    {
        LOGGER.info("OUT");
        motor.set(-motorSpeed);
        safety.feed();
        LOGGER.info("Troll");
    }
    
    public void stop()
    {
        motor.set(0.0);
        safety.feed();
        LOGGER.info("Troll");
    }
    
    private boolean isLoaded()
    {
        // TODO Work with sensor
        return false;
    }
}
