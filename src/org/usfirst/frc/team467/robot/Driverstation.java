package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * Singleton class to handle driverstation I/O on Team467 2014 Robot
 *
 * @author Team467
 */
public class Driverstation
{

    //Singleton instance variable
    private static Driverstation instance;

    //Driverstation objects
    private DriverStation driverstation;

    //Joystick objects
    private Joystick467 JoystickLeft;
    private Joystick467 JoystickRight;

    //Singleton so constructor is private
    private Driverstation()
    {
        driverstation = DriverStation.getInstance();
        JoystickLeft = new Joystick467(0);
        JoystickRight = new Joystick467(1);
    }

    /**
     * Returns the single instance of this class
     *
     * @return
     */
    public static Driverstation getInstance()
    {
        if (instance == null)
        {
            instance = new Driverstation();
        }
        return instance;
    }

    public double getBatteryVoltage()
    {
        return driverstation.getBatteryVoltage();
    }

    /**
     * Read all Robot Inputs. Typically, this is called once per iteration of
     * the main event loop. Any soft filtering of inputs to remove noise or
     * implement non-linear acceleration is also done here.
     */
    public void readInputs()
    {
        JoystickLeft.readInputs();
        JoystickRight.readInputs();
    }

    /**
     * Gets left joystick instance.
     *
     * @return
     */
    public Joystick467 getDriveJoystick()
    {
        return JoystickLeft;
    }

    /**
     * Gets right joystick instance.
     *
     * @return
     */
    public Joystick467 getNavJoystick()
    {
        return JoystickRight;
    }

    // All button mappings are accessed through the functions below
    
    /**
     * returns the current drive mode. Modes lower in the function will override those higher up.
     * only 1 mode can be active at any time
     * @return currently active drive mode. 
     */
    public DriveMode getDriveMode()
    {
    	DriveMode drivemode = DriveMode.CRAB_NO_FA;  // default is regular crab drive
    	Joystick467 drivestick = Driverstation.getInstance().getDriveJoystick();
    	
    	if (drivestick.buttonDown(3)) drivemode = DriveMode.CAR;
    	// if (drivestick.buttonDown(5)) drivemode = DriveMode.CRAB_FA;
    	if (drivestick.buttonDown(2)) drivemode = DriveMode.TURN;
    	
    	int pov = drivestick.getPOV();
        if (pov != -1 && pov != 0 && pov != 180) drivemode = DriveMode.STRAFE;
        
        if (drivestick.buttonDown(5) || drivestick.buttonDown(6)) drivemode = DriveMode.REVOLVE;
    	
    	return drivemode;
    } 
    
    /**
     * 
     * @return true if button required to enable slow driving mode are pressed
     */
    public boolean getSlow()
    {
        return Driverstation.getInstance().getDriveJoystick().buttonDown(Joystick467.TRIGGER);
    }    
    
    /**
     * 
     * @return true if button required to enable turbo driving mode are pressed
     */
    public boolean getTurbo()
    {
        return Driverstation.getInstance().getDriveJoystick().buttonDown(7);
    }
}
