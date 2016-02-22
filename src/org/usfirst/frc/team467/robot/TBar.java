package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Talon;

public class TBar
{
    private static final Logger LOGGER = Logger.getLogger(TBar.class);
    
    private CANTalon tMotor = null;
    
    public TBar(int tMotorChannel)
    {    
        tMotor = new CANTalon(tMotorChannel); //switch to actual port number
    }
    
    public void stop()
    {
        tMotor.set(0.0);
    }
    
    public void launchTBar(tBarDirection tBarDirection)
    {
        switch(tBarDirection)
        {
            case DOWN:
                tMotor.set(0.1);
                break;
            case UP:
                tMotor.set(-0.1);
                break;
            case STOP:
                stop();
                break;
        }
    }
    enum tBarDirection
    {
        DOWN, UP, STOP
    }

}
