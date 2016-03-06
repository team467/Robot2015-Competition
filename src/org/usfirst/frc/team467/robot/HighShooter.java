package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.CANTalon;

public class HighShooter
{
    private CANTalon rightMotor;
    private CANTalon leftMotor;
    
    private int motorSpeed = 1;
    
    private DriverStation2015 driverstation;
    
    
    public HighShooter(int rightMotorChannel, int leftMotorChannel, DriverStation2015 driverstation)
    {
        rightMotor = new CANTalon(rightMotorChannel);
        leftMotor = new CANTalon(leftMotorChannel);
        this.driverstation = driverstation;
    }
    public void stop() {
        rightMotor.set(0);
        leftMotor.set(0);
    }
    public void shoot()
    {
        if (driverstation.highShooterReady()) {
            rightMotor.set(motorSpeed);
            leftMotor.set(-motorSpeed);
        }
        else {
            stop();
        }
        
    }

}
