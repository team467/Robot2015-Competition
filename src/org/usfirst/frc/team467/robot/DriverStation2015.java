package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;
import org.usfirst.frc.team467.robot.Autonomous.AutoType;

public class DriverStation2015
{
    private static final Logger LOGGER = Logger.getLogger(DriverStation2015.class);
    
    private static DriverStation2015 driverstation2015 = null;

    Joystick467 driverJoy = null;
    ButtonPanel2015 buttonPanel = null;
    
    // Mapping of functions to Joystick Buttons for normal operation
    private static int SLOW_BUTTON = Joystick467.TRIGGER;
    private static int TURN_BUTTON = 2;
    private static int REVOLVE_LARGE_LEFT_BUTTON = 3;
    private static int REVOLVE_LARGE_RIGHT_BUTTON = 4;
    private static int REVOLVE_SMALL_LEFT_BUTTON = 5;
    private static int REVOLVE_SMALL_RIGHT_BUTTON = 6;
    private static int TURBO_BUTTON = 7;
    private static int GYRO_RESET_BUTTON = 8;
    
    // Mapping of functions to Joystick Buttons for calibration mode
    private static int CALIBRATE_CONFIRM_BUTTON = Joystick467.TRIGGER;
    private static int CALIBRATE_SLOW_BUTTON = 4;
    
    // Mapping of POV position to functions
    private static int POV_STRAFE_FRONT = 0;
    private static int POV_STRAFE_LEFT = 270;
    private static int POV_STRAFE_RIGHT = 90;
    private static int POV_STRAFE_BACK = 180;
    
    private static int UNWIND_BUTTON = 10;

    // CAL/AUTO
    public static int AUTO_CAL_SWITCH = ButtonPanel2015.COVERED_SWITCH;

    // JOYSTICK
    public static int CHANGE_SPEED_BUTTON = ButtonPanel2015.JOY_TOP_BUTTON;
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
    
    enum Speed 
    {
        SLOW, FAST
    }

    /**
     * Singleton instance of the object.
     * 
     * @return
     */
    public static DriverStation2015 getInstance()
    {
        if (driverstation2015 == null)
        {
            driverstation2015 = new DriverStation2015();
        }
        return driverstation2015;
    }

    /**
     * Private constructor
     */
    private DriverStation2015()
    {
        driverJoy = new Joystick467(0);
        buttonPanel = new ButtonPanel2015(1, false);
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
     * Gets if the driverstation switch is in calibrate selector mode.
     * 
     * @return
     */
    public boolean isCalibrateMode()
    {
        return buttonPanel.isButtonDown(AUTO_CAL_SWITCH);
    }

    /**
     * Gets the type of autonomous to use.
     * 
     * @return Autonomous mode or DRIVE_ONLY if for some reason no position on
     *         the switch is selected, or NULL if not in autonomous mode.
     */
    public AutoType getAutoType()
    {
        // Dial positions are mutually exclusive
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_2)) return AutoType.DRIVE_ONLY;                       
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_3)) return AutoType.GRAB_CAN;
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_4)) return AutoType.HOOK_AND_PUSH;
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_5)) return AutoType.HOOK_AND_PUSH_OVER_RAMP;

        return AutoType.NO_AUTO;
    }

    /**
     * Gets the wheel to calibrate.
     * 
     * @return - Steering motor IDs from RobotMap or -1 if no wheel is selected,
     */
    public int getCalibrateWheel()
    {   
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_2)) return RobotMap.FRONT_LEFT;
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_3)) return RobotMap.BACK_RIGHT;
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_4)) return RobotMap.BACK_LEFT;
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_5)) return RobotMap.FRONT_RIGHT;
        
        // no wheel selected
        return -1;
    }
    
    public double getLifterRampRate()
    {
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_1)) return 0.1;
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_2)) return 0.2;
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_3)) return 0.3;
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_4)) return 0.4;
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_5)) return 0.5;
        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_6)) return 0.6;
        
        LOGGER.error("No Dial Position Selected");
        return 0.2;
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
        {
            drivemode = DriveMode.CRAB_FA;
        }
        
        if (getDriveJoystick().buttonDown(TURN_BUTTON))
        {
            drivemode = DriveMode.TURN;
        }
        
        if (pov == POV_STRAFE_FRONT)
        {
            drivemode = DriveMode.STRAFE_FRONT;
        }
        
        if (pov == POV_STRAFE_LEFT)
        {
            drivemode = DriveMode.STRAFE_LEFT;
        }
        
        if (pov == POV_STRAFE_RIGHT)
        {
            drivemode = DriveMode.STRAFE_RIGHT;
        }
        
        if (pov == POV_STRAFE_BACK)
        {
            drivemode = DriveMode.STRAFE_BACK;
        }
        
        if (getDriveJoystick().buttonDown(REVOLVE_SMALL_LEFT_BUTTON))
        {
            drivemode = DriveMode.REVOLVE_SMALL_LEFT;
        }
        
        if (getDriveJoystick().buttonDown(REVOLVE_SMALL_RIGHT_BUTTON))
        {
            drivemode = DriveMode.REVOLVE_SMALL_RIGHT;
        }
        
        if (getDriveJoystick().buttonDown(REVOLVE_LARGE_LEFT_BUTTON))
        {
            drivemode = DriveMode.REVOLVE_LARGE_LEFT;
        }
        
        if (getDriveJoystick().buttonDown(REVOLVE_LARGE_RIGHT_BUTTON))
        {
            drivemode = DriveMode.REVOLVE_LARGE_RIGHT;
        }
        
        if (getDriveJoystick().buttonDown(UNWIND_BUTTON))
        {
            drivemode = DriveMode.UNWIND;
        }
        return drivemode;
    }

    /**
     * 
     * @return true if button required to enable slow driving mode are pressed
     */
    public boolean getSlow()
    {
        return getDriveJoystick().buttonDown(SLOW_BUTTON);
    }

    /**
     * 
     * @return true if button required to enable turbo driving mode are pressed
     */
    public boolean getTurbo()
    {
        return getDriveJoystick().buttonDown(TURBO_BUTTON);
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
        return getCalibrationJoystick().buttonDown(CALIBRATE_CONFIRM_BUTTON);
    }

    /**
     * 
     * @return true if button to enable calibration slow turn mode is pressed
     */
    public boolean getCalibrateSlowTurn()
    {
        return getCalibrationJoystick().buttonDown(CALIBRATE_SLOW_BUTTON);
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

//    /**
//     * Checks to see if the speed change button is pressed.
//     * 
//     * @return
//     */
//    public Speed getSpeedChange()
//    {
////        if (buttonPanel.isButtonDown(CHANGE_SPEED_BUTTON))
////        {
////            return Speed.SLOW;
//        }
//        return Speed.FAST;
//    }
    
    public boolean getLowerCurrent()
    {
        return buttonPanel.isButtonDown(CHANGE_SPEED_BUTTON);
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
        buttonPanel.setLED(LED_CLAW_STOPPED, state);        
    }
    
    /**
     * Sets the state of the lifter LED
     * 
     * @param state
     */
    public void setLifterLED(int led, boolean state)
    {
        buttonPanel.setLED(led, state);        
    }

}
