/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.usfirst.frc.team467.robot;

// import edu.wpi.first.wpilibj.CameraServer;
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
    private OpsCalibrate opsCalibrate;
    private ButtonCalibrate buttonCalibrate;
    
    //CameraServer cameraServer;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit()
    {
        //Make robot objects
        driverstation = Driverstation.getInstance();
        
        drive = Drive.getInstance();
        opsCalibrate = OpsCalibrate.getInstance();
        buttonCalibrate = ButtonCalibrate.getInstance();
        
//        cameraServer = CameraServer.getInstance();
//        cameraServer.setQuality(50);
//        cameraServer.startAutomaticCapture("cam0");
//        
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
    	
    	switch (driverstation.getDriveMode())
    	{
    		case REVOLVE:
    		{
    			Direction direction = Direction.LEFT;
    			if (driverstation.getDriveJoystick().buttonDown(6))
    			{
    				direction = Direction.RIGHT;
    			}
    			drive.revolveDrive(direction);
    		}
    		break;
    	
    		case STRAFE:
    		{
    			Direction direction = Direction.LEFT;
    			if (driverstation.getDriveJoystick().getPOV() < 180) 
    			{
    				direction = Direction.RIGHT;
    			}
    			drive.strafeDrive(direction, 0.3);
    		}
    		break;
        
    		case TURN:
    			drive.turnDrive(-driverstation.getDriveJoystick().getTwist());
    			break;
        
    		case CAR:
    			drive.carDrive(driverstation.getDriveJoystick().getTwist(), 
        		 		   driverstation.getDriveJoystick().getStickY());
    			break;
        
    		case CRAB_FA:
    			drive.crabDrive(driverstation.getDriveJoystick().getStickAngle(), 
        				    driverstation.getDriveJoystick().getStickDistance(), 
        				    true /* field aligned */ );
    			break;
        
    		case CRAB_NO_FA:
    			drive.crabDrive(driverstation.getDriveJoystick().getStickAngle(),
        		  	      driverstation.getDriveJoystick().getStickDistance(), 
        		  	      false /* not field aligned */ );
    			break;
        
    		default:  //should never enter here
    			System.err.println("Button State not calculated correctly");
    			drive.crabDrive(driverstation.getDriveJoystick().getStickAngle(),
  		  	      driverstation.getDriveJoystick().getStickDistance(), 
  		  	      false /* not field aligned */ );
    			break;
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
