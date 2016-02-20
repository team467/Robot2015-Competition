package org.usfirst.frc.team467.robot;
import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.AnalogGyro;
public class Gyro2016
{
    private static final Logger LOGGER = Logger.getLogger(Gyro2016.class);

    private static Gyro2016 instance = null;  
    private AnalogGyro tiltGyro = null;  
    private AnalogGyro yawGyro = null;
    
    
    public Gyro2016(){
        tiltGyro = new AnalogGyro(1);
        yawGyro = new AnalogGyro(0);
    }
    
    public static Gyro2016 getInstance(){
        LOGGER.debug("getting instance of Gyro2016s");
        if(instance == null)
        {
            LOGGER.debug("null Gyro");
            instance = new Gyro2016();
            LOGGER.debug("Gyros made");
        }
        return instance;
    }
   
    public double getTiltAngle(){
       return tiltGyro.getAngle();
    }
    
    public double getYawAngle(){
       return yawGyro.getAngle();
    }
    
    public void reset(){
        tiltGyro.reset();
        yawGyro.reset();
    }
    
    public boolean isUp(){
        return getTiltAngle() >= 4.0;
    }
    
    public boolean isFlat(){
        return Math.abs(getTiltAngle()) <= 4.0;
    }
    
    public boolean isDown(){
        return getTiltAngle() <= -4.0;
    }
    
    public boolean shouldTurnRight(double angle){
        return (getYawAngle() < angle);
    }
    
    public boolean shouldTurnLeft(double angle){
        return (getYawAngle() > angle);
    }
    
    public double wrapAngle(double val)
    {
        double newVal = val % 360;
        return (newVal < 0) ? 360 + newVal : newVal;
    }
}
