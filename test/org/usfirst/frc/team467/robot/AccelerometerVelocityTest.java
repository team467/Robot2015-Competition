package org.usfirst.frc.team467.robot;

import org.junit.Test;

import edu.wpi.first.wpilibj.interfaces.Accelerometer;

public class AccelerometerVelocityTest
{
    static AccelerometerVelocity superClass = new AccelerometerVelocity();
    
    private Accelerometer accel;
    double g = -1;
    double xVal;
    double yVal;
    double zVal;
   
    static double vx = 0;
    static double vy = 0;
    static double vz = 0;
    
/*    AccelerometerVelocityTest(Accelerometer accel)
    {
        this.accel = accel;
    }
*/
    
    public AccelerometerVelocityTest()
    {
        
    }

    public static void velocity()
    {  
        double z = -0.06;
        double x = 0.67;
        double y = -0.89;
        
        double vx = superClass.vx;
        double vy = superClass.vy;
        double vz = superClass.vz;
        
        for(int i = 0; i < 10; i++)
        {        
        x = x + 0.01;
        y = y + 0.09;
        z = z - 0.01;
        superClass.velocityCalc(x, y, z);
        System.out.println(superClass.vx + " " + superClass.vy + " " + superClass.vz);
        }
    }
    
    public double getVX()
    {
        double vx = superClass.vx;      
        return vx;
    }
    
    public double getVY()
    {
        double vy = superClass.vy;
        return vy;
    }
    
    public double getVZ()
    {
        double vz = superClass.vz;
        return vz;
    }
@Test
    public void myTest()
    {
        velocity();
    }
}
