package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.Joystick;

public class ButtonPanel2015
{

    // joystick object to read inputs off of
    Joystick buttonPanel = null;

    // array of button states
    // NOTE: button indexes begin at 1, therefore 0 is ignored
    boolean[] buttons = new boolean[17];
    // NOTE: LED indexes begin at 1, therefore 0 is ignored
    private boolean[] ledStates = new boolean[7];

    // CONSTANTS:

    // DIAL (FROM LEFT):
    public static int DIAL_POS_1 = 1;
    public static int DIAL_POS_2 = 2;
    public static int DIAL_POS_3 = 5;
    public static int DIAL_POS_4 = 4;
    public static int DIAL_POS_5 = 3;
    public static int DIAL_POS_6 = 6;

    // AUTO SWITCH (ON is UP):
    public static int COVERED_SWITCH = 9;

    // JOYSTICK:
    public static int JOY_TOP_BUTTON = 16;
    public static int JOY_UP = 14;
    public static int JOY_DOWN = 12;
    public static int JOY_LEFT = 13;
    public static int JOY_RIGHT = 15;

    /**
     * ButtonPanel for the 2015 driverstation
     * 
     * @param port
     */
    public ButtonPanel2015(int port)
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
