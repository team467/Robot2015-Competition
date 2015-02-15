package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * Singleton class to handle driverstation I/O on Team467 Robot
 *
 * @author Team467
 */
public class DriverStation467
{
    // Singleton instance variable
    private static DriverStation467 instance;

    // Driverstation objects
    private DriverStation driverstation;

    // Joystick objects
    private Joystick467 joystick1;
    private Joystick467 joystick2;

    // Singleton so constructor is private
    private DriverStation467()
    {
        driverstation = DriverStation.getInstance();
        joystick1 = new Joystick467(0);
        joystick2 = new Joystick467(1);
    }

    /**
     * Returns the single instance of this class
     *
     * @return
     */
    public static DriverStation467 getInstance()
    {
        if (instance == null)
        {
            instance = new DriverStation467();
        }
        return instance;
    }

    public double getBatteryVoltage()
    {
        return driverstation.getBatteryVoltage();
    }

    /**
     * Read all Robot Inputs. Typically, this is called once per iteration of
     * the main event loop.
     */
    public void readInputs()
    {
        joystick1.readInputs();
        joystick2.readInputs();
    }

    /**
     * Gets joystick instance used by driver.
     *
     * @return
     */
    public Joystick467 getDriveJoystick()
    {
        return joystick1;
    }

    /**
     * Get joystick instance used by navigator.
     *
     * @return
     */
    public Joystick467 getNavJoystick()
    {
        return joystick2;
    }

    /**
     * Get joystick instance used for calibration.
     *
     * @return
     */
    public Joystick467 getCalibrationJoystick()
    {
        return joystick1;
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
        DriveMode drivemode = DriveMode.CRAB_NO_FA;  // default is regular crab drive

        // if (getDriveJoystick().buttonDown(5)) drivemode = DriveMode.CRAB_FA;
        if (getDriveJoystick().buttonDown(2))
            drivemode = DriveMode.TURN;

        int pov = getDriveJoystick().getPOV();
        if (pov != -1 && pov != 0 && pov != 180)
            drivemode = DriveMode.STRAFE;

        if (getDriveJoystick().buttonDown(5) || getDriveJoystick().buttonDown(6))
            drivemode = DriveMode.REVOLVE;

        if (getDriveJoystick().buttonDown(11))
            drivemode = DriveMode.UNWIND;

        return drivemode;
    }

    /**
     * 
     * @return true if button required to enable slow driving mode are pressed
     */
    public boolean getSlow()
    {
        return getDriveJoystick().buttonDown(7);
    }

    /**
     * 
     * @return true if button required to enable turbo driving mode are pressed
     */
    public boolean getTurbo()
    {
        return getDriveJoystick().buttonDown(Joystick467.TRIGGER);
    }

    // Calibration functions. Calibration is a separate use mode - so the buttons used
    // here can overlap with those used for the regular drive modes

    /**
     * 
     * @return true if calibration mode selected
     */
    public boolean getCalibrate()
    {
        return getCalibrationJoystick().getFlap();
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
}
