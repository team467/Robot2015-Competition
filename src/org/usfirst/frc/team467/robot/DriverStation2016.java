package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;
import org.usfirst.frc.team467.robot.Autonomous.AutoType;
import org.usfirst.frc.team467.robot.BallRollers.ManipIntent;
import org.usfirst.frc.team467.robot.BallRollers.RollerDirection;
import org.usfirst.frc.team467.robot.TBar.tBarDirection;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DriverStation2016
{
    private static final Logger LOGGER = Logger.getLogger(DriverStation2016.class);
    
    private static DriverStation2016 instance = null;

    MainJoystick467 driverJoy1 = null;
    RightJoystick467 driverJoy2 = null;
    ButtonPanel2016 buttonPanel = null;
    String stickType;
    
    // Mapping of functions to Joystick Buttons for normal operation
    private static int REVOLVE_LARGE_LEFT_BUTTON = 3;
    private static int REVOLVE_LARGE_RIGHT_BUTTON = 4;
    private static int REVOLVE_SMALL_LEFT_BUTTON = 5;
    private static int REVOLVE_SMALL_RIGHT_BUTTON = 6;
    private static int GYRO_RESET_BUTTON = 8;
    
    private static int UNWIND_BUTTON = 10;
    
    public boolean kart = false;
    public boolean split = false;

    // LED Ids
    public static int LED_LIFTER_TOP_STOP = 5;
    public static int LED_LIFTER_TOP_SLOW = 2;
    public static int LED_LIFTER_BOTTOM_SLOW = 3;
    public static int LED_LIFTER_BOTTOM_STOP = 1;
    public static int LED_CLAW_STOPPED = 4;
    
    // BUTTONS
    public static int INTAKE = ButtonPanel2016.YELLOW_BUTTON;
    public static int LOW_SHOOTER = ButtonPanel2016.GREEN_BUTTON;
    public static int HIGH_SHOOTER = ButtonPanel2016.BLUE_BUTTON;
    public static int AIM_SHOOTER = ButtonPanel2016.RED_BUTTON;
    
    // TOGGLE
    public static int MANIPULATOR_UP = ButtonPanel2016.TOGGLE_UP;
    public static int MANIPULATOR_DOWN = ButtonPanel2016.TOGGLE_DOWN;
    
    //THREE SWITCH
    public static int TBAR_UP = ButtonPanel2016.SWITCH_UP;
    public static int TBAR_DOWN = ButtonPanel2016.SWITCH_DOWN;
    
    //LED
    public static int INTAKE_LED = 2;
    
    enum Speed 
    {
        SLOW, FAST
    }

    /**
     * Singleton instance of the object.
     * 
     * @return
     */
    public static DriverStation2016 getInstance()
    {
        if (instance == null)
        {
            instance = new DriverStation2016();
        }
        return instance;
    }

    /**
     * Private constructor
     */
    private DriverStation2016()
    {
        makeJoysticks();
    }
    
    private void makeJoysticks()
    {
        buttonPanel = new ButtonPanel2016(1);
        String newStickType = SmartDashboard.getString("DB/String 0", "XBSplit"); //Assume LT1
        if (newStickType.isEmpty())
        {
            newStickType = "LT1";
        }
        LOGGER.debug(newStickType);
        if (newStickType.equals(stickType))
        {
            return;
        }
        stickType = newStickType.toUpperCase();
        String stickTypeDescription;
        switch (stickType)
        {
            case "LT1":
                driverJoy1 = new LogitechJoystick(0);
                driverJoy2 = null;
                kart = false;
                split = false;
                stickTypeDescription = "Logitech 1-stick";
                break;
            case "LT2":
                driverJoy1 = new LogitechJoystick(0);
                driverJoy2 = new LogitechJoystick(2);
                kart = false;
                split = false;
                stickTypeDescription = "Logitech 2-stick";
                break;
            case "PS1":
                driverJoy1 = new PlayStationJoystickMain(4);
                driverJoy2 = null;
                kart = false;
                split = false;
                stickTypeDescription = "Playstation 1-stick";
                break;
            case "PS2":
                driverJoy1 = new PlayStationJoystickMain(4);
                driverJoy2 = new PlayStationJoystickRight(4);
                kart = false;
                split = false;
                stickTypeDescription = "Playstation 2-stick";
                break;
            case "PSKART":
                driverJoy1 = new PlayStationJoystickMain(4);
                driverJoy2 = null;
                kart = true;
                split = false;
                stickTypeDescription = "Playstation Kart";
                break;
            case "PSSPLIT":
                driverJoy1 = new PlayStationJoystickMain(4);
                driverJoy2 = new PlayStationJoystickRight(4);
                kart = false;
                split = true;
                stickTypeDescription = "Playstation split-stick";
                break;
            case "XB1":
                driverJoy1 = new XBoxJoystickMain(3);
                driverJoy2 = null;
                kart = false;
                split = false;
                stickTypeDescription = "XBox 1-stick";
                break;
            case "XB2":
                driverJoy1 = new XBoxJoystickMain(3);
                driverJoy2 = new XBoxJoystickRight(3);
                kart = false;
                split = false;
                stickTypeDescription = "XBox 2-stick";
                break;
            case "XBKART":
                driverJoy1 = new XBoxJoystickMain(3);
                driverJoy2 = null;
                kart = true;
                split = false;
                stickTypeDescription = "XBox Kart";
                break;
            case "XBSPLIT":
                driverJoy1 = new XBoxJoystickMain(3);
                driverJoy2 = new XBoxJoystickRight(3);
                kart = false;
                split = true;
                stickTypeDescription = "XBox split-stick";
                break;
            default:
                LOGGER.info("Auto Selector must be LT1, LT2, PS1, PS2, PSKART, PSSPLIT, "
                        + "XB1, XB2, XBKART, or XBSPLIT");
                stickTypeDescription = "Invalid(XBSplit)";
                LOGGER.info("Assuming XBSplit");
                driverJoy1 = new XBoxJoystickMain(3);
                driverJoy2 = new XBoxJoystickRight(3);
                kart = false;
                split = true;
                stickTypeDescription = "XBox split-stick";
                break;
        }
        SmartDashboard.putString("DB/String 5", "Stick type " + stickTypeDescription);
    }

    /**
     * Must be called prior to first button read.
     */
    public void readInputs()
    {
        makeJoysticks();
        
        driverJoy1.readInputs();
        if (driverJoy2 != null)
        {
            driverJoy2.readInputs();
        }
        buttonPanel.readInputs();
        buttonPanel.updateLEDs();
    }

    /**
     * Turns on all LEDs.
     */
    public void testLEDs()
    {
//        buttonPanel.setAllLEDsOn();
    }

//    /**
//     * Gets if the driverstation switch is in calibrate selector mode.
//     * 
//     * @return
//     */
//    public boolean isCalibrateMode()
//    {
//        return buttonPanel.isButtonDown(AUTO_CAL_SWITCH);
//    }

    /**
     * Gets the type of autonomous to use.
     * 
     * @return Autonomous mode or DRIVE_ONLY if for some reason no position on
     *         the switch is selected, or NULL if not in autonomous mode.
     */
    public AutoType getAutoType()
    {
//        return AutoType.AIM;
        // Dial positions are mutually exclusive
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_2)) return AutoType.DRIVE_ONLY;                       
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_3)) return AutoType.GRAB_CAN;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_4)) return AutoType.HOOK_AND_PUSH;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_5)) return AutoType.HOOK_AND_PUSH_OVER_RAMP;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_1)) return AutoType.AIM;
        String mode = SmartDashboard.getString("Auto Selector", "invalid").toUpperCase();
        
        switch(mode) {
            case "AIM":
                return AutoType.AIM;
            case "STAY_IN_PLACE":
                return AutoType.STAY_IN_PLACE;
            default:
                return AutoType.NO_AUTO;
        }
    }

//    /**
//     * Gets the wheel to calibrate.
//     * 
//     * @return - Steering motor IDs from RobotMap or -1 if no wheel is selected,
//     */
//    public int getCalibrateWheel()
//    {   
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_2)) return RobotMap.FRONT_LEFT;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_3)) return RobotMap.BACK_RIGHT;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_4)) return RobotMap.BACK_LEFT;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_5)) return RobotMap.FRONT_RIGHT;
//        
//        // no wheel selected
//        return -1;
//    }
    
//    public double getLifterRampRate()
//    {
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_1)) return 0.1;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_2)) return 0.2;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_3)) return 0.3;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_4)) return 0.4;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_5)) return 0.5;
//        if (buttonPanel.isButtonDown(ButtonPanel2015.DIAL_POS_6)) return 0.6;
//        
//        LOGGER.error("No Dial Position Selected");
//        return 0.2;
//    }

    /**
     * Prints all pressed buttons to the console
     */
    public void logButtonPanelPressedButtons()
    {
        buttonPanel.logPressedButtons();
    }

    /**
     * @return first joystick instance used by driver.
     */
    public MainJoystick467 getDriveJoystick1()
    {
        return driverJoy1;
    }
    
    /**
     * @return second joystick instance used by driver.
     */
    public RightJoystick467 getDriveJoystick2()
    {
        return driverJoy2;
    }

    /**
     * Get joystick instance used for calibration.
     *
     * @return
     */
    public MainJoystick467 getCalibrationJoystick()
    {
        return driverJoy1;
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
        boolean fieldAligned = getDriveJoystick1().getFieldAligned();
        Direction strafe = getDriveJoystick1().getStrafeDirection();

        DriveMode drivemode = DriveMode.ARCADE_NO_FA;  // default is regular crab drive
        
        if (fieldAligned)
        {
            drivemode = DriveMode.ARCADE_FA;
        }
        
        if (getDriveJoystick1().getTurnButton())
        {
            drivemode = DriveMode.TURN;
        }
        
        switch (strafe)
        {
            case FRONT:
                drivemode = DriveMode.STRAFE_FRONT;
                break;
            case LEFT:
                drivemode = DriveMode.STRAFE_LEFT;
                break;
            case BACK:
                drivemode = DriveMode.STRAFE_BACK;
                break;
            case RIGHT:
                drivemode = DriveMode.STRAFE_RIGHT;
                break;
            default:
                // Eat default, nothing happens here
                break;
        }
        
        if (getDriveJoystick1().buttonDown(REVOLVE_SMALL_LEFT_BUTTON))
        {
            drivemode = DriveMode.REVOLVE_SMALL_LEFT;
        }
        
        if (getDriveJoystick1().buttonDown(REVOLVE_SMALL_RIGHT_BUTTON))
        {
            drivemode = DriveMode.REVOLVE_SMALL_RIGHT;
        }
        
        if (getDriveJoystick1().buttonDown(REVOLVE_LARGE_LEFT_BUTTON))
        {
            drivemode = DriveMode.REVOLVE_LARGE_LEFT;
        }
        
        if (getDriveJoystick1().buttonDown(REVOLVE_LARGE_RIGHT_BUTTON))
        {
            drivemode = DriveMode.REVOLVE_LARGE_RIGHT;
        }
        
        if (getDriveJoystick1().buttonDown(UNWIND_BUTTON))
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
        return getDriveJoystick1().getSlow();
    }

    /**
     * 
     * @return true if button required to enable turbo driving mode are pressed
     */
    public boolean getTurbo()
    {
        return getDriveJoystick1().getTurbo();
    }

    // Calibration functions. Calibration is a separate use mode - so the buttons used
    // here can overlap with those used for the regular drive modes

//    /**
//     * 
//     * @return true if calibration mode selected
//     */
//    public boolean getCalibrate()
//    {
//        return buttonPanel.isButtonDown(AUTO_CAL_SWITCH);
//    }
    
    public boolean getGyroReset()
    {
        return driverJoy1.buttonDown(GYRO_RESET_BUTTON);
    }

    /**
     * 
     * @return true if button to confirm calibration selection is pressed
     */
    public boolean getCalibrateConfirmSelection()
    {
        return getCalibrationJoystick().getCalibrateConfirm();
    }

//    /**
//     * Direction to drive the claw.
//     * 
//     * @return
//     */
//    public ClawMoveDirection getClawDirection()
//    {
//        if (buttonPanel.isButtonDown(CLAW_OPEN))
//        {
//            return ClawMoveDirection.OPEN;
//        }
//        else if (buttonPanel.isButtonDown(CLAW_CLOSED))
//        {
//            return ClawMoveDirection.CLOSE;
//        }
//        else
//        {
//            return ClawMoveDirection.STOP;
//        }
//    }

    public tBarDirection getTBarDirection()
    {
        if (buttonPanel.isButtonDown(TBAR_DOWN)) {
            return tBarDirection.DOWN;
        }
        else if (buttonPanel.isButtonDown(TBAR_UP)) {
            return tBarDirection.UP;
        }
        else {
            return tBarDirection.STOP;
        }
        
        
    }
    public RollerDirection getRollerDirection()
    {
        if (buttonPanel.isButtonDown(INTAKE)) {
            return RollerDirection.IN;
        }
        else if (buttonPanel.isButtonDown(LOW_SHOOTER)) {
            return RollerDirection.OUT;
        }
        else {
            return RollerDirection.STOP;
        }
        
    }
    public boolean highShooterButton()
    {
        if (buttonPanel.isButtonDown(HIGH_SHOOTER)) {
            return true;
        }
        else {
            return false;
        }
    }
    public boolean aimShooterButton()
    {
        if (buttonPanel.isButtonDown(AIM_SHOOTER)) {
            return true;
        }
        else {
            return false;
        }
    }
    public ManipIntent getManipPosition()
    {
        if (buttonPanel.isButtonDown(MANIPULATOR_UP)) {
            return ManipIntent.SHOULD_EXTEND;
        }
        else if (buttonPanel.isButtonDown(MANIPULATOR_DOWN)) {
            return ManipIntent.SHOULD_RETRACT;
        }
        else {
            //this should never happen ;P
            return ManipIntent.SHOULD_STOP;
        }
    }
    public void setIntakeLED(boolean state)
    {
        buttonPanel.setLED(INTAKE_LED, state);
    }
//    /**
//     * Gets the lifter direction for lifting.
//     * 
//     * @return
//     */
//    public LifterDirection getLiftDirection()
//    {
//        if (buttonPanel.isButtonDown(ELEVATOR_DOWN))
//        {
//            return LifterDirection.DOWN;
//        }
//        else if (buttonPanel.isButtonDown(ELEVATOR_UP))
//        {
//            return LifterDirection.UP;
//        }
//        else
//        {
//            return LifterDirection.STOP;
//        }
//    }


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
    
//    public boolean getLowerCurrent()
//    {
//        return buttonPanel.isButtonDown(CHANGE_SPEED_BUTTON);
//    }
    
    public boolean getResetGyro()
    {
        return driverJoy1.getResetGyro();
    }
    
    public CameraDashboard.CamView getView()
    {
        boolean shooterViewActivated = SmartDashboard.getBoolean("DB/Button 1");
        if (shooterViewActivated)
        {
            return CameraDashboard.CamView.SHOOTER;
        }
        else
        {
            return CameraDashboard.CamView.TANK;
        }
    }

//    /**
//     * Sets the state of the claw LED
//     * 
//     * @param state
//     */
//    public void setClawLED(boolean state)
//    {
//        buttonPanel.setLED(LED_CLAW_STOPPED, state);        
//    }
    
//    /**
//     * Sets the state of the lifter LED
//     * 
//     * @param state
//     */
//    public void setLifterLED(int led, boolean state)
//    {
//        buttonPanel.setLED(led, state);        
//    }

}
