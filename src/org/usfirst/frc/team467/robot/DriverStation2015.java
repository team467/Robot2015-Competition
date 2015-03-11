package org.usfirst.frc.team467.robot;

import org.usfirst.frc.team467.robot.Autonomous.AutoType;

public class DriverStation2015
{

    private static DriverStation2015 driverstation2015 = null;

    Joystick467 driverJoy = null;
    ButtonPanel2015 buttonPanel = null;
    
    public static int GYRO_RESET_BUTTON = 8;

    // CAL/AUTO
    public static int AUTO_CAL_SWITCH = ButtonPanel2015.COVERED_SWITCH;

    // KNOB - CAL
    public static int CAL_STD_DRIVE_1 = ButtonPanel2015.DIAL_POS_1;
    public static int CAL_STD_DRIVE_2 = ButtonPanel2015.DIAL_POS_2;
    public static int CAL_FL = ButtonPanel2015.DIAL_POS_2;
    public static int CAL_FR = ButtonPanel2015.DIAL_POS_5;
    public static int CAL_BL = ButtonPanel2015.DIAL_POS_4;
    public static int CAL_BR = ButtonPanel2015.DIAL_POS_3;

    // JOYSTICK
    public static int OPERATE_FASTER_BUTTON = ButtonPanel2015.JOY_TOP_BUTTON;
    public static int CLAW_OPEN = ButtonPanel2015.JOY_RIGHT;
    public static int CLAW_CLOSED = ButtonPanel2015.JOY_LEFT;
    public static int ELEVATOR_UP = ButtonPanel2015.JOY_UP;
    public static int ELEVATOR_DOWN = ButtonPanel2015.JOY_DOWN;

    // LED Ids
    public static int LED_LIFTER_TOP_STOP = 5;
    public static int LED_LIFTER_TOP_SLOW = 2;
    public static int LED_LIFTER_BOTTOM_SLOW = 3;
    public static int LED_LIFTER_BOTTOM_STOP = 1;
    public static int LED_CLAW_STOPPED = 4;

    /**
     * Singleton instance of the object.
     * 
     * @return
     */
    public static DriverStation2015 getInstance()
    {
        if (driverstation2015 == null)
            driverstation2015 = new DriverStation2015();
        return driverstation2015;
    }

    /**
     * Private constructor
     */
    private DriverStation2015()
    {
        driverJoy = new Joystick467(0);
        buttonPanel = new ButtonPanel2015(1);
    }

    /**
     * Must be called prior to first button read.
     */
    public void readInputs()
    {
        driverJoy.readInputs();
        buttonPanel.readInputs();
        buttonPanel.updateLEDs();
    }

    /**
     * Turns on all LEDs.
     */
    public void testLEDs()
    {
        buttonPanel.setAllLEDsOn();
    }

    /**
     * Gets the Lift type to lift the Lifter
     * 
     * @return
     */
    public LiftTypes getLiftType()
    {
        double driveSpeed = 0;
        if (buttonPanel.isButtonDown(ELEVATOR_UP))
            if (buttonPanel.isButtonDown(OPERATE_FASTER_BUTTON))
                return LiftTypes.LIFT_UP_FAST;
            else
                return LiftTypes.LIFT_UP_SLOW;
        else if (buttonPanel.isButtonDown(ELEVATOR_DOWN))
            if (buttonPanel.isButtonDown(OPERATE_FASTER_BUTTON))
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
    public double getClawMoveType()
    {
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
    public boolean isAutonomousMode()
    {
        return !buttonPanel.isButtonDown(AUTO_CAL_SWITCH);
    }

    /**
     * Gets if the driverstation switch is in calibrate selector mode.
     * 
     * @return
     */
    public boolean isCalibrateMode()
    {
        return !isAutonomousMode();
    }

    /**
     * Gets the type of autonomous to use.
     * 
     * @return Autonomous mode or DRIVE_ONLY if for some reason no position on
     *         the switch is selected, or NULL if not in autonomous mode.
     */
    public AutoType getAutoType()
    {
        if (isAutonomousMode())
        {
            if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_2))
            {
                return AutoType.DRIVE_ONLY;
            }                           
            else if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_3))
            {
                return AutoType.GRAB_CAN;
            }
            else if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_4))
            {
                return AutoType.HOOK_AND_PUSH_OVER_RAMP;
            }
            else
            {
                return AutoType.NO_AUTO;
            }
        }
        else
        {
            return AutoType.NO_AUTO;
        }
    }

    /**
     * Gets the wheel to calibrate.
     * 
     * @return - Steering motor IDs from RobotMap or -1 if no wheel is selected,
     *         or -9999 if not in calibrate mode
     */
    public int getCalibrateWheel()
    {
        if (DriverStation2015.getInstance().getCalibrate())
        {
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
        }
        else
        {
            // not in calibrate mode
            return -9999;
        }
    }

    /**
     * Prints all pressed buttons to the console
     */
    public void printButtonPanelPressedButtons()
    {
        buttonPanel.printPressedButtons();
    }

    /**
     * Gets joystick instance used by driver.
     *
     * @return
     */
    public Joystick467 getDriveJoystick()
    {
        return driverJoy;
    }

    /**
     * Get joystick instance used for calibration.
     *
     * @return
     */
    public Joystick467 getCalibrationJoystick()
    {
        return driverJoy;
    }

    // All button mappings are accessed through the functions below

    /**
     * returns the current drive mode. Modes lower in the function will override
     * those higher up. only 1 mode can be active at any time
     * 
     * @return currently active drive mode.
     */
    public DriveMode getDriveMode()
    {
        boolean flap = getDriveJoystick().getFlap();
        int pov = getDriveJoystick().getPOV();

        DriveMode drivemode = DriveMode.CRAB_NO_FA;  // default is regular crab drive
        
        if (flap) // If flap is up
            drivemode = DriveMode.CRAB_FA;
        
        if (getDriveJoystick().buttonDown(2))
            drivemode = DriveMode.TURN;
        
        if (pov == 270 || pov == 90)
            drivemode = DriveMode.STRAFE;
        
        if (getDriveJoystick().buttonDown(5) || getDriveJoystick().buttonDown(6))
            drivemode = DriveMode.REVOLVE_SMALL;
        
        if (getDriveJoystick().buttonDown(3) || getDriveJoystick().buttonDown(4))
            drivemode = DriveMode.REVOLVE_LARGE;

        if (pov == 0)
            drivemode = DriveMode.UNWIND;

        return drivemode;
    }

    /**
     * 
     * @return true if button required to enable slow driving mode are pressed
     */
    public boolean getSlow()
    {
        return getDriveJoystick().buttonDown(Joystick467.TRIGGER);
    }

    /**
     * 
     * @return true if button required to enable turbo driving mode are pressed
     */
    public boolean getTurbo()
    {
        return getDriveJoystick().buttonDown(7);
    }

    // Calibration functions. Calibration is a separate use mode - so the buttons used
    // here can overlap with those used for the regular drive modes

    /**
     * 
     * @return true if calibration mode selected
     */
    public boolean getCalibrate()
    {
        return buttonPanel.isButtonDown(AUTO_CAL_SWITCH);
    }
    
    public boolean getGyroReset()
    {
        return driverJoy.buttonDown(GYRO_RESET_BUTTON);
    }

    /**
     * 
     * @return true if button to confirm calibration selection is pressed
     */
    public boolean getCalibrateConfirmSelection()
    {
        return getCalibrationJoystick().buttonDown(Joystick467.TRIGGER);
    }

    /**
     * 
     * @return true if button to enable calibration slow turn mode is pressed
     */
    public boolean getCalibrateSlowTurn()
    {
        return getCalibrationJoystick().buttonDown(4);
    }

    /**
     * Direction to drive the claw.
     * 
     * @return
     */
    public ClawMoveDirection getClawDirection()
    {
        if (buttonPanel.isButtonDown(CLAW_OPEN))
        {
            return ClawMoveDirection.OPEN;
        }
        else if (buttonPanel.isButtonDown(CLAW_CLOSED))
        {
            return ClawMoveDirection.CLOSE;
        }
        else
        {
            return ClawMoveDirection.STOP;
        }
    }

    /**
     * Gets the lifter direction for lifting.
     * 
     * @return
     */
    public LifterDirection getLiftDirection()
    {
        if (buttonPanel.isButtonDown(ELEVATOR_DOWN))
        {
            return LifterDirection.DOWN;
        }
        else if (buttonPanel.isButtonDown(ELEVATOR_UP))
        {
            return LifterDirection.UP;
        }
        else
        {
            return LifterDirection.STOP;
        }
    }

    /**
     * Checks to see if turbo from the nav is pressed.
     * 
     * @return
     */
    public boolean getMoveTurbo()
    {
        return buttonPanel.isButtonDown(OPERATE_FASTER_BUTTON);
    }
    
    public boolean getResetGyro()
    {
        return driverJoy.buttonDown(8);
    }

    /**
     * Sets the state of the claw LED
     * 
     * @param state
     */
    public void setClawLED(boolean state)
    {
        System.out.println("SETTING LED_CLAW to " + state);
        buttonPanel.setLED(LED_CLAW_STOPPED, state);        
    }
    
    /**
     * Sets the state of the claw LED
     * 
     * @param state
     */
    public void setLifterLED(int led, boolean state)
    {
        System.out.println("SETTING LED "+ led + " to " + state);
        buttonPanel.setLED(led, state);        
    }

}
