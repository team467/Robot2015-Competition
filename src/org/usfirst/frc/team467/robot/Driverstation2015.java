package org.usfirst.frc.team467.robot;

public class Driverstation2015 {

	private static Driverstation2015 driverstation2015 = null;

	Joystick467 driverJoy = null;
	ButtonPanel2015 buttonPanel = null;
	
	Lifter lifter = null;

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
		lifter = Lifter.getInstance();
	}

	/**
	 * Must be called prior to first button read.
	 */
	public void readInputs() {
		driverJoy.readInputs();
		buttonPanel.readInputs();
	}

	/**
	 * Gets the Lift type to lift the Lifter
	 * 
	 * @return
	 */
	public LiftTypes getLiftType() {
		double driveSpeed = 0;
		if (buttonPanel.isButtonDown(ELEVATOR_UP))
			if(buttonPanel.isButtonDown(OPERATE_FASTER_BUTTON))
				return LiftTypes.LIFT_UP_FAST;
			else
				return LiftTypes.LIFT_UP_SLOW;
		else if (buttonPanel.isButtonDown(ELEVATOR_DOWN))
			if(buttonPanel.isButtonDown(OPERATE_FASTER_BUTTON))
				return LiftTypes.LIFT_DOWN_FAST;
			else
				return LiftTypes.LIFT_DOWN_SLOW;
		else
			return LiftTypes.NO_LIFT;
			
		
	}

	/**
	 * Gets the speed to drive the gripper motor in PWM, between -1.0 and 1.0.
	 * 
	 * @return moves speed for the gripper between -1.0 and 1.0
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
	 * Gets if the driverstation switch is in Auto selector mode.
	 * 
	 * @return
	 */
	public boolean isAutonomousMode() {
		return buttonPanel.isButtonDown(AUTO_CAL_BUTTON);
	}

	/**
	 * Gets if the driverstation switch is in calibrate selector mode.
	 * 
	 * @return
	 */
	public boolean isCalibrateMode() {
		return !isAutonomousMode();
	}

	/**
	 * Gets the type of autonomous to use.
	 * 
	 * @return Autonomous mode or DRIVE_ONLY if for some reason no position on
	 *         the switch is selected, or NULL if not in autonomous mode.
	 */
	public AutoType getAutoType() {
		if (isAutonomousMode()) {
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
		else
		{
			return null;
		}
	}

	/**
	 * Gets the wheel to calibrate.
	 * 
	 * @return - Steering motor IDs from RobotMap or -1 if no wheel is selected, or
	 *  -9999 if not in calibrate mode
	 */
	public int getCalibrateWheel() {
		if (isCalibrateMode()) {
			if (buttonPanel.isButtonDown(CAL_BL))
				return RobotMap.BACK_LEFT;
			else if (buttonPanel.isButtonDown(CAL_BR))
				return RobotMap.BACK_RIGHT;
			else if (buttonPanel.isButtonDown(CAL_FL))
				return RobotMap.FRONT_LEFT;
			else if (buttonPanel.isButtonDown(CAL_FR))
				return RobotMap.FRONT_RIGHT;
			else
				// no wheel selected
				return -1;
		} else {
			// not in calibrate mode
			return -9999;
		}
	}

	/**
	 * Prints all pressed buttons to the console
	 */
	public void printButtonPanelPressedButtons() {
		buttonPanel.printPressedButtons();
	}

}
