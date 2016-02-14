package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;

public class AccelerometerVelocity
{
    private Accelerometer accel;
    
    double dt = 0.02;
    
    public double vx;
    public double vy;
    public double vz;
  
    AccelerometerVelocity(Accelerometer accel)
    {
        this.accel = accel;
        
        vx = 0;
        vy = 0;
        vz = 0;
        
    }
    
    AccelerometerVelocity()
    {
        
    }
    
    public void velocity()
    {   
        double z = accel.getZ();
        double x = accel.getX();
        double y = accel.getY();
        
        velocityCalc(x, y, z);
    }
    
    public void velocityCalc(double x, double y, double z)
    {
        
        vx += ((x * 9.8) * dt);
        vy += ((y * 9.8) * dt);
        vz += (((z - 1) * 9.8) * dt);   
    }

}
