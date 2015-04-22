package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.MotorSafety;

public interface WheelPod extends MotorSafety
{

    public void drive(double speed, double angle);

    public void drive(double speed);

    public void steer(double angle);

    public void absoluteSteer(double angle);

    public Steering getSteering();
}
