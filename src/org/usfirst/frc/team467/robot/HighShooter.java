package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.Talon;

public class HighShooter
{
    private Talon rightMotor;
    private Talon leftMotor;
    
    private double motorSpeed = 0.7;
    
    private DriverStation2016 driverstation;
    
    
    public HighShooter(int rightMotorChannel, int leftMotorChannel, DriverStation2016 driverstation)
    {
        rightMotor = new Talon(rightMotorChannel);
        leftMotor = new Talon(leftMotorChannel);
        this.driverstation = driverstation;
    }
    public void stop() {
        rightMotor.set(0);
        leftMotor.set(0);
    }
    public void shoot()
    {
        rightMotor.set(-motorSpeed);
        leftMotor.set(motorSpeed);
    }

}
