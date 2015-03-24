package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.DigitalOutput;

public class LEDStrip
{
    public enum Mode {
        OFF,
        RAINBOW,
        BLUE_AND_GOLD,
        PULSE_RED,
        PULSE_BLUE,
        PULSE_YELLOW
    }
    
    private DigitalOutput autoOut;
    private DigitalOutput teleopOut;
    private DigitalOutput warningOut;

    public LEDStrip()
    {
        autoOut = new DigitalOutput(4);
        teleopOut = new DigitalOutput(5);
        warningOut = new DigitalOutput(6);
        
        setMode(Mode.OFF);
    }
    
    public void setMode(Mode mode)
    {
        switch (mode)
        {
            case OFF:
                set(false, false, false);
                break;
            case RAINBOW:
                set(true, false, false);
                break;
            case BLUE_AND_GOLD:
                set(false, false, true);
                break;
            case PULSE_RED:
                set(false, true, false);
                break;
            case PULSE_BLUE:
                set(true, true, false);
                break;
            case PULSE_YELLOW:
                set(false, true, true);
                break;
        }
    }

    private void set(boolean setAutoOut, boolean setTeleopOut, boolean setWarningOut)
    {
        autoOut.set(setAutoOut);
        teleopOut.set(setTeleopOut);
        warningOut.set(setWarningOut);
    }
}
