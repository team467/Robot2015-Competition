package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.AnalogInput;

public class Infrared
{
    AnalogInput infrared = new AnalogInput(0);
    
    public double newBall()
    {
        return infrared.getAverageValue();
    }

}
