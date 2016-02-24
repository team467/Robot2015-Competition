package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Talon;

public class TBar
{
    private static final Logger LOGGER = Logger.getLogger(TBar.class);
    
    private Talon tMotor = null;
    
    public AnalogInput rotationSensor = new AnalogInput(3);
    
    public TBar(int tMotorChannel)
    {    
        tMotor = new Talon(tMotorChannel); //switch to actual port number
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
                LOGGER.info("want DOWN");
                if (rotationSensor.getAverageValue() > 1600) {
                    LOGGER.info("going down");
                    tMotor.set(-0.4);
                }
                break;
            case UP:
                LOGGER.info("want UP");
                if (rotationSensor.getAverageValue() < 1800) {
                    LOGGER.info("going up");
                    tMotor.set(0.4);
                }
                break;
            case STOP:
                LOGGER.info("stopping");
                stop();
                break;  
        }
        LOGGER.info("Rotation Sensor: " + rotationSensor.getAverageValue());
    }
    enum tBarDirection
    {
        DOWN, UP, STOP
    }
    //3216 is the top
    //381 is the bottom
    

}
