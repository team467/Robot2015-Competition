package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.DigitalOutput;

public class LEDStrip
{
    private DigitalOutput autoOut;
    private DigitalOutput teleopOut;
    private DigitalOutput warningOut;

    public LEDStrip()
    {
        autoOut = new DigitalOutput(4);
        teleopOut = new DigitalOutput(5);
        warningOut = new DigitalOutput(6);
        
        set(false, false, false);
    }

    public void set(boolean setAutoOut, boolean setTeleopOut, boolean setWarningOut)
    {
        autoOut.set(setAutoOut);
        teleopOut.set(setTeleopOut);
        warningOut.set(setWarningOut);
    }
}
