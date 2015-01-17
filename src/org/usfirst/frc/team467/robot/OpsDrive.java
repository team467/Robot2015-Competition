/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc.team467.robot;

/**
 *
 * @author Kyle
 */
public class OpsDrive
{
    private static OpsDrive opsDrive = null;
    private Drive drive = null;
    private Joystick467 joyLeft = null;
    
    public static OpsDrive getInstance()
    {
        if (opsDrive == null)
        {
        	opsDrive = new OpsDrive();
        }
        return opsDrive;
    }    
    
    private OpsDrive()
    {
        drive = Drive.getInstance();
        joyLeft = Driverstation.getInstance().getDriveJoystick();
    }

    /**
     * Car Drive Drive Mode
     *     
     */
    public void carDrive()
    {
        // Speed for car drive
        drive.carDrive(joyLeft.getTwist(), joyLeft.getStickY());
    }

    /**
     * Swerve Drive with field align on
     *
     */
    public void swerveDriveFAlign()
    {
        drive.crabDrive(joyLeft.getStickAngle(), joyLeft.getStickDistance(), true /* field aligned */ );
    }

    /**
     * Swerve Drive with field align off
     *
     */
    public void swerveDriveNoFAlign()
    {
        drive.crabDrive(joyLeft.getStickAngle(), joyLeft.getStickDistance(), false /* not field aligned */);
    }
    
    /**
     * Turn in place drive
     */
    public void turnInPlace()
    {
        drive.turnDrive(-joyLeft.getTwist());
    } 
    
    public void strafeDrive()
    {
    	Direction direction = Direction.LEFT;
    	if (joyLeft.getPOV() < 180) {
    		direction = Direction.RIGHT;
    	}
    	drive.strafeDrive(direction, 0.3);
    }
    
    public void revolveDrive()
    {
    	Direction direction = Direction.LEFT;
    	if (joyLeft.buttonDown(6))
    	{
    		direction = Direction.RIGHT;
    	}
    	drive.revolveDrive(direction);
    }
}
