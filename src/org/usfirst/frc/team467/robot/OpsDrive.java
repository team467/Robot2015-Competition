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
    private Speed speed = null;
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
        speed = Speed.getInstance();
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
        drive.carDrive(joyLeft.getTwist(), speed.getCarDriveSpeed());
    }

    /**
     * Swerve Drive with field align on
     *
     */
    public void swerveDriveFAlign()
    {
        drive.crabDrive(joyLeft.getStickAngle(), speed.getCrabDriveFASpeed(), true/*field aligned*/);
    }

    /**
     * Swerve Drive with field align off
     *
     */
    public void swerveDriveNoFAlign()
    {
        drive.crabDrive(joyLeft.getStickAngle(), speed.getCrabDriveNoFASpeed(), false/*not field aligned*/);
    }
    
    /**
     * Turn in place drive
     */
    public void turnInPlace()
    {
        drive.turnDrive(speed.getTurnInPlaceSpeed());
    } 
    
    /**
     * Hybrid Drive
     */
    public void hybridDrive()
    {                
        drive.hybridDrive(joyLeft.getTwist(), joyLeft.getStickAngle(), speed.getHybridDriveSpeed());
    }
    
    public void strafeDrive()
    {
    	
    }
}
