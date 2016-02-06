package org.usfirst.frc.team467.robot;
import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.AnalogGyro;
public class Gyro2016
{
    private static final Logger LOGGER = Logger.getLogger(Gyro2016.class);

    private static Gyro2016 instance = null;  
    private AnalogGyro gyro = null;  
    
    private Gyro2016(){
        gyro = new AnalogGyro(1);
        
    }
    
    public static Gyro2016 getInstance(){
        LOGGER.info("getting instance");
        if(instance == null)
        {
            LOGGER.info("null Gyro");
            instance = new Gyro2016();
            LOGGER.info("Gyro made");
        }
        return instance;
    }
   
    public double getAngle(){
       return gyro.getAngle();
    }
    
    public void reset(){
        gyro.reset();
    }
    
    public boolean up(){
        return getAngle() >= 4.0;
    }
    
    public boolean isFlat(){
        return Math.abs(getAngle()) <= 4.0;
    }
    
    public boolean down(){
        return getAngle() <= -4.0;
    }
}
