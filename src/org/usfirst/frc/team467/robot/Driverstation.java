package org.usfirst.frc.team467.robot;

import javax.management.RuntimeErrorException;
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

}
