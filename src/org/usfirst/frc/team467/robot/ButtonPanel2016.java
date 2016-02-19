package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.Joystick;

public class ButtonPanel2016
{
 // joystick object to read inputs off of
    Joystick buttonPanel = null;

    // array of button states
    // NOTE: button indexes begin at 1, therefore 0 is ignored
    boolean[] buttons = new boolean[17];
    // NOTE: LED indexes begin at 1, therefore 0 is ignored
    private boolean[] ledStates = new boolean[7];

    // CONSTANTS:
    
    public static final int RED_BUTTON = 14;
    public static final int YELLOW_BUTTON = 15;
    public static final int GREEN_BUTTON = 16;
    
    public static final int SWITCH_DOWN = 17;
    public static final int SWITCH_UP = 18;
    
    public static final int TOGGLE_UP = 19;
    public static final int TOGGLE_DOWN = 20;
    
    /**
     * ButtonPanel for the 2015 driverstation
     * 
     * @param port
     * @param allowJoystickDiagonals If false, diagonals do nothing.
     */
    public ButtonPanel2016(int port)
    {
        buttonPanel = new Joystick(port);
    }

    /**
     * Updates values on the button panel. Must be called at the start of each
     * loop.
     */
    public void readInputs()
    {
        // starts at 1 because buttons are 1 based
        for (int i = 1; i < buttons.length; i++)
        {
            buttons[i] = buttonPanel.getRawButton(i);
        }
    }
    /**
     * Updates the LEDs to be in the proper states, then resets them. Must be
     * called each loop.
     */
    public void updateLEDs()
    {
        for (int i = 1; i < ledStates.length; i++)
        {
            buttonPanel.setOutput(i, ledStates[i]);
        }
        // reset all LEDs
        for (int i = 1; i < ledStates.length; i++)
        {
            ledStates[i] = false;
        }
    }

    /**
     * Sets the LED index to on or off.
     * 
     * @param index
     * @param light
     */
    public void setLED(int index, boolean light)
    {
        ledStates[index] = light;
    }

    /**
     * Checks button state array to see if a button is down.
     * 
     * @param button
     */
    public boolean isButtonDown(int button)
    {
        return buttonPanel.getRawButton(button);
    }

    /**
     * Prints all pressed button numbers.
     */
    public void printPressedButtons()
    {
        // starts at 1 because buttons are 1 based
        for (int i = 1; i < buttons.length; i++)
        {
            if (buttons[i])
            {
                System.out.print(i + " ");
            }
        }
        System.out.println();
    }

    /**
     * Turns on all digital outputs on the button panel.
     */
    public void setAllLEDsOn()
    {
        for (int i = 1; i < ledStates.length; i++)
        {
            buttonPanel.setOutput(i, true);
        }
    }
    
    

}
