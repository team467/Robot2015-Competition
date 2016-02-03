package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;

public class Tilt
{
    private Accelerometer accel;
    
    Tilt (Accelerometer accel){
        this.accel = accel;
    }
    
    Tilt (){
        
    }
    
    public double calc(double xVal, double yVal, double zVal){
       double g = -1;
       
        double alphaTilt = Math.toDegrees(Math.asin(xVal / g));
        return alphaTilt;
    }
}
