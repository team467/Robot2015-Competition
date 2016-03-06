package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.DigitalInput;

public class Infrared
{
    DigitalInput infrared;
    
    public Infrared(int port)
    {
        infrared = new DigitalInput(port); 
    }
    
    public boolean getInfrared()
    {
        return infrared.get();
    }

}
