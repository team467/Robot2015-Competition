package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.AnalogGyro;
public class Gyro2016
{
    private Gyro gyro;
    
    double Kp = 0.03;
    
    public Gyro gyro(){
        gyro = new AnalogGyro(1);
        return gyro;
    }
   
    public double autonomous(){
        gyro.reset();
        double angle = gyro.getAngle();
        return angle;
    }

}
