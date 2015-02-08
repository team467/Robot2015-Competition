package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.Joystick;

public class Driverstation2015 {

	private static Driverstation2015 driverstation2015 = null;

	Joystick467 driverJoy = null;
	ButtonPanel2015 buttonPanel = null;

	// CAL/AUTO
	public static int AUTO_CAL_BUTTON = ButtonPanel2015.COVERED_SWITCH;

	// KNOB - CAL
	public static int CAL_STD_DRIVE_1 = ButtonPanel2015.DIAL_POS_1;
	public static int CAL_STD_DRIVE_2 = ButtonPanel2015.DIAL_POS_2;
	public static int CAL_FL = ButtonPanel2015.DIAL_POS_3;
	public static int CAL_FR = ButtonPanel2015.DIAL_POS_4;
	public static int CAL_BL = ButtonPanel2015.DIAL_POS_5;
	public static int CAL_BR = ButtonPanel2015.DIAL_POS_6;

	// KNOB - AUTO
	public static int AUTO_DRIVE_ONLY = ButtonPanel2015.DIAL_POS_1;
	public static int AUTO_GRAB_ITEM = ButtonPanel2015.DIAL_POS_2;
	public static int AUTO_PUSH_TOTE = ButtonPanel2015.DIAL_POS_3;
	public static int AUTO_GRAB_CONTAINER_PUSH_TOTE = ButtonPanel2015.DIAL_POS_4;
	public static int AUTO_GRAB_BOTH = ButtonPanel2015.DIAL_POS_5;
	public static int AUTO_NOTHING = ButtonPanel2015.DIAL_POS_6;

	// JOYSTICK
	public static int OPERATE_FASTER_BUTTON = ButtonPanel2015.JOY_TOP_BUTTON;
	public static int CLAW_OPEN = ButtonPanel2015.JOY_LEFT;
	public static int CLAW_CLOSED = ButtonPanel2015.JOY_RIGHT;
	public static int ELEVATOR_UP = ButtonPanel2015.JOY_UP;
	public static int ELEVATOR_DOWN = ButtonPanel2015.JOY_DOWN;

	/**
	 * Singleton instance of the object.
	 * 
	 * @return
	 */
	public static Driverstation2015 getInstance() {
		if (driverstation2015 == null)
			driverstation2015 = new Driverstation2015();
		return driverstation2015;
	}

	/**
	 * Private constructor
	 */
	private Driverstation2015() {
		driverJoy = new Joystick467(0);
		buttonPanel = new ButtonPanel2015(1);
	}

	/**
	 * Must be called prior to first button read.
	 */
	public void readInputs() {
		driverJoy.readInputs();
		buttonPanel.readInputs();
	}

	/**
	 * Gets the speed to drive the lifter motor.
	 * 
	 * @return
	 */
	public double getLiftSpeed() {
		double driveSpeed = 0;
		if (buttonPanel.isButtonDown(ELEVATOR_UP))
			driveSpeed = 0.4;// TODO determine sign
		else if (buttonPanel.isButtonDown(ELEVATOR_DOWN))
			driveSpeed = -0.4;// TODO determine sign
		if (buttonPanel.isButtonDown(OPERATE_FASTER_BUTTON))
			driveSpeed *= 2;
		return driveSpeed;
	}

	/**
	 * Gets the speed to drive the gripper motor.
	 * 
	 * @return
	 */
	public double getGripperMoveSpeed() {
		double driveSpeed = 0;
		if (buttonPanel.isButtonDown(CLAW_OPEN))
			driveSpeed = 0.4;// TODO determine sign
		else if (buttonPanel.isButtonDown(CLAW_CLOSED))
			driveSpeed = -0.4;// TODO determine sign
		if (buttonPanel.isButtonDown(OPERATE_FASTER_BUTTON))
			driveSpeed *= 2;
		return driveSpeed;
	}

	/**
	 * Gets if the driverstation should be in auto mode.
	 * 
	 * @return
	 */
	public boolean isAutonomousMode() {
		return buttonPanel.isButtonDown(AUTO_CAL_BUTTON);
	}

	/**
	 * Gets if the driverstation should be in calibrate mode.
	 * 
	 * @return
	 */
	public boolean isCalibrateMode() {
		return !isAutonomousMode();
	}

	/**
	 * Gets the type of autonomous to use.
	 * 
	 * @return
	 */
	public AutoType getAutoType() {
		if (buttonPanel.isButtonDown(AUTO_DRIVE_ONLY))
			return AutoType.DRIVE_ONLY;
		else if (buttonPanel.isButtonDown(AUTO_GRAB_BOTH))
			return AutoType.GRAB_BOTH;
		else if (buttonPanel.isButtonDown(AUTO_GRAB_CONTAINER_PUSH_TOTE))
			return AutoType.GRAB_CONTAINER_PUSH_TOTE;
		else if (buttonPanel.isButtonDown(AUTO_PUSH_TOTE))
			return AutoType.PUSH_TOTE;
		else
			return AutoType.DRIVE_ONLY;
	}

	/**
	 * Gets the wheel to calibrate.
	 * 
	 * @return - Steering motor IDs from RobotMap
	 */
	public int getCalibrateWheel()
	{
//		if(isCalibrateMode())
//			if(buttonPanel.isButtonDown(CAL_STD_DRIVE_1))
//			{
//				
//			}
		return 0; //TODO implement... ha
	}

	/**
	 * Prints all pressed buttons to the console
	 */
	public void printPressedButtons() {
		buttonPanel.printPressedButtons();
	}

}
