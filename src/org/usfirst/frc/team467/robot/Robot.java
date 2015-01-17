/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot
{
    public static final boolean SINGLE_STICK_DRIVE = false;

    //Robot objects
    private Driverstation driverstation;
    private Drive drive;

    private OpsDrive opsDrive;
    private OpsCalibrate opsCalibrate;
    private ButtonDrive buttonDrive;
    private ButtonCalibrate buttonCalibrate;
    
    CameraServer cameraServer;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit()
    {
        //Make robot objects
        driverstation = Driverstation.getInstance();
        drive = Drive.getInstance();
        buttonDrive = ButtonDrive.getInstance();
        
        opsDrive = OpsDrive.getInstance();
        opsCalibrate = OpsCalibrate.getInstance();
        buttonDrive = ButtonDrive.getInstance();
        buttonCalibrate = ButtonCalibrate.getInstance();
        
        cameraServer = CameraServer.getInstance();
        cameraServer.setQuality(50);
        cameraServer.startAutomaticCapture("cam0");
        
        Calibration.init();        
    }

    public void disabledInit()
    {
    }

    /**
     * This function is run when autonomous control mode is first enabled
     */
    public void autonomousInit()
    {
        // Read driverstation inputs
        driverstation.readInputs();
    }

    /**
     * This function is run when operator control mode is first enabled
     */
    public void teleopInit()
    {
    }

    /**
     * This function is run when test mode is first enabled
     */
    public void testInit()
    {
    }

    /**
     * This function is called periodically test mode
     */
    public void testPeriodic()
    {
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic()
    {
    }

    // this is a static variable to select the wheel to calibrate.
    int calibrateWheelSelect = 0;

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic()
    {
        // Read driverstation inputs
        driverstation.readInputs();        
        
        // Use driver's stick        
        Joystick467 joyLeft = driverstation.getDriveJoystick();
        
        //updates the buttons
        buttonDrive.updateButtons(joyLeft);
        buttonCalibrate.updateButtons(joyLeft);

        if (buttonCalibrate.getCalibrate())
        {
            System.out.println("CALIBRATE");
            updateCalibrateControl(joyLeft);
        }
        else // drive mode, not calibrate
        {
            // operates using the updated buttons
            updateDriveAndNavigate();
        }
    }

    private void updateDriveAndNavigate()
    {
        ///
        /// Update Drive
        ///
    	
        // priority for each state is intentional, not bug
    	if (buttonDrive.getRevolveDrive())
    	{
    		opsDrive.revolveDrive();
    	}
    	else if (buttonDrive.getStrafeDrive())
        {
        	opsDrive.strafeDrive();
        }
        else if (buttonDrive.getTurnInPlace())
        {
            opsDrive.turnInPlace();
        }
        else if (buttonDrive.getCarDrive())
        {
            opsDrive.carDrive();
        }
        else if (buttonDrive.getCrabDriveFA())
        {
            opsDrive.swerveDriveFAlign();
        }
        else if (buttonDrive.getCrabDriveNoFA())
        {
            opsDrive.swerveDriveNoFAlign();
        }
        else if (buttonDrive.getHybridDrive())
        {
            System.out.println("HD D");
            opsDrive.hybridDrive();
        }
        else //should never enter here
        {
            System.err.println("Button State not calculated correctly");
            opsDrive.swerveDriveNoFAlign();
        }
    }
    
    /**
     * Update steering calibration control
     */
    private void updateCalibrateControl(Joystick467 joy)
    {
        calibrateWheelSelect = opsCalibrate.getWheel(joy, calibrateWheelSelect);
        Calibration.updateSteeringCalibrate(calibrateWheelSelect, joy);
    }
}
