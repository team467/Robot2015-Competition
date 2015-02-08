package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.Joystick;

public class ButtonPanel2015 {

	// joystick object to read inputs off of
	Joystick buttonPanel = null;

	// array of button states
	//NOTE: button indexes begin at 1, therefore 0 is ignored
	boolean[] buttons = new boolean[17];
	
	//CONSTANTS:
	
	//DIAL (FROM LEFT):
	public static int DIAL_POS_1 = 6;
	public static int DIAL_POS_2 = 5;
	public static int DIAL_POS_3 = 4;
	public static int DIAL_POS_4 = 3;
	public static int DIAL_POS_5 = 1;
	public static int DIAL_POS_6 = 2;

	//AUTO SWITCH (ON is UP):
	public static int COVERED_SWITCH = 9;
	
	//JOYSTICK:
	public static int JOY_TOP_BUTTON = 15;
	public static int JOY_UP = 12;
	public static int JOY_DOWN = 14;
	public static int JOY_LEFT = 13;
	public static int JOY_RIGHT = 16;
	
	/**
	 * ButtonPanel for the 2015 driverstation
	 * 
	 * @param port
	 */
	public ButtonPanel2015(int port) {
		buttonPanel = new Joystick(port);
	}

	/**
	 * Updates values on the button panel. Must be called at the start of each
	 * loop.
	 */
	public void readInputs() {
		//starts at 1 because buttons are 1 based
		for (int i = 1; i < buttons.length; i++) {
			buttons[i] = buttonPanel.getRawButton(i);
		}
	}
	
	/**
	 * Checks button state array to see if a button is down.
	 * @param button
	 */
	public boolean isButtonDown(int button)
	{
		return buttons[button];
	}
	
	/**
	 * Prints all pressed button numbers. 
	 */
	public void printPressedButtons()
	{
		//starts at 1 because buttons are 1 based
		for (int i = 1; i < buttons.length; i++) {
			if(buttons[i])
			System.out.print(i + " ");
		}
		System.out.println();
	}

}
