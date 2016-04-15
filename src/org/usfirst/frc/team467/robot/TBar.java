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
    
    private PowerDistroBoard467 board = null;
    
    int MAX_CURRENT = 0;
    
    public TBar(int tMotorChannel)
    {    
        tMotor = new Talon(tMotorChannel); //switch to actual port number
        board = PowerDistroBoard467.getInstance();
    }
    
    public void stop()
    {
        LOGGER.debug("Rotation Sensor: " + rotationSensor.getAverageValue());
        tMotor.set(0.0);
    }
    
    public double getSensorReading()
    {
        return rotationSensor.getAverageValue();
    }
    
    public void launchTBar(tBarDirection tBarDirection)
    {
        LOGGER.debug("tBarDirection=" + tBarDirection);
//        if (board.getTBarCurrent() > MAX_CURRENT) {
//          tBarDirection = tBarDirection.STOP;
//        }
        LOGGER.debug(board.getTBarCurrent());
        
        switch(tBarDirection)
        {       
            case DOWN:
                LOGGER.debug("want DOWN");
                if (!isDown()) {
                    LOGGER.info("going down");
                    tMotor.set(-0.4);
                }
                else {
                    stop();
                }
                break;
            case UP:
                LOGGER.debug("want UP");
                if (!isUp()) {
                    LOGGER.info("going up");
                    tMotor.set(0.4);
                }
                else {
                    stop();
                }
                break;
            case STOP:
                //LOGGER.info("stopping");
                stop();
                break;  
        }
        LOGGER.debug("Rotation Sensor: " + rotationSensor.getAverageValue());
    }
    
    public boolean isUp(){
        return rotationSensor.getAverageValue() >= 3000;
   }
    
    public boolean isDown(){
        return (rotationSensor.getAverageValue() <= 300);
    }
    
    enum tBarDirection
    {
        DOWN, UP, STOP
    }
    
    
    //3216 is the top
    //381 is the bottom
    
}