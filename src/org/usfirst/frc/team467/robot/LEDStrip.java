package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.DigitalOutput;

public class LEDStrip
{
    public enum Mode {
        OFF           (0),
        RAINBOW       (1),
        PULSE_RED     (2),
        PULSE_BLUE    (3),
        BLUE_AND_GOLD (4),
        PULSE_YELLOW  (6);
        
        private final int ledCode;
        
        Mode(int ledCode)
        {
            this.ledCode = ledCode;
        }
        
        public int getLedCode()
        {
            return this.ledCode;
        }
    }
    
    private DigitalOutput bit0;
    private DigitalOutput bit1;
    private DigitalOutput bit2;
    
    public LEDStrip()
    {
        bit0 = new DigitalOutput(4);
        bit1 = new DigitalOutput(5);
        bit2 = new DigitalOutput(6);
        
        setMode(Mode.OFF);
    }
    
    /**
     * @param mode - LEDStrip.Mode.&lt;mode&gt;
     */
    public void setMode(Mode mode)
    {
        //set(mode.getLedCode());

// TODO Remove if above code works
        switch (mode)
        {
            case OFF:
                set(false, false, false);
                break;
            case RAINBOW:
                set(true, false, false);
                break;
            case PULSE_RED:
                set(false, true, false);
                break;
            case PULSE_BLUE:
                set(true, true, false);
                break;
            case BLUE_AND_GOLD:
                set(false, false, true);
                break;
            case PULSE_YELLOW:
                set(false, true, true);
                break;
        }
    }
    
// TODO Remove if above code works    
    private void set(boolean setBit0, boolean setBit1, boolean setBit2)
    {
        bit0.set(setBit0);
        bit1.set(setBit1);
        bit2.set(setBit2);
    }
    
    /**
     * Maps the ledCode to bits
     * 
     * @param ledCode
     */
//    private void set(int ledCode)
//    {
//        // If specified bit is true
//        bit0.set((ledCode & 0x01) != 0);
//        bit1.set((ledCode & 0x02) != 0);
//        bit2.set((ledCode & 0x04) != 0);
//    }
}
