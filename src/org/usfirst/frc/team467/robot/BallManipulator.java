package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.CANTalon;

public class BallManipulator
{
    private static final Logger LOGGER = Logger.getLogger(TBar.class);
    
    private static BallManipulator roller = null;
    
    private CANTalon rollerMotor = null;
    
    public static BallManipulator GetInstance()
    {
        if (roller == null)
        {
            roller = new BallManipulator();
        }
        return roller;
    }

}
