/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */

/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;
import com.ni.vision.NIVision.Image;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot
{
    private static final Logger LOGGER = Logger.getLogger(Robot.class);

    private static final double MIN_DRIVE_SPEED = 0.1;

    // Robot objects
    private DriverStation2015 driverstation;

    private Drive drive;
    private PowerDistroBoard467 board;
    private Autonomous autonomous;

    private CameraDashboard cameraDashboard;
    private Lifter lifter;
    private Claw claw;
    private Gyro2015 gyro;

    int session;
    Image frame;

    CameraServer cameraServer;
    
    public Joystick joy = null;
    public DigitalOutput autoOut = null;
    public DigitalOutput TeleopOut = null;
    public DigitalOutput warningOut = null;

    /**
     * Time in milliseconds
     */
    double time;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit()
    {
        // Initialize logging framework.
        Logging.init();

        // Make robot objects
        driverstation = DriverStation2015.getInstance();
        autonomous = Autonomous.getInstance();
        drive = Drive.getInstance();
        board = PowerDistroBoard467.getInstance();
        lifter = Lifter.getInstance();
        claw = Claw.getInstance();
        gyro = Gyro2015.getInstance();
        joy = new Joystick(0);
        autoOut = new DigitalOutput(4);
        TeleopOut = new DigitalOutput(5);
        warningOut = new DigitalOutput(6);
        autoOut.set(false);
        TeleopOut.set(false);
        warningOut.set(false);

        // Initialize the camera dashboard and launch in separate thread.
        cameraDashboard = CameraDashboard.getInstance();
        if (cameraDashboard.cameraExists()) 
        {
            LOGGER.debug("Camera Starting");
            cameraDashboard.start();
        }
        Calibration.init();
    }

    public void disabledInit()
    {
        LOGGER.info("Robot disabled");
    }

    public void disabledPeriodic()
    {
        gyro.update();
        LOGGER.debug("GYRO ANGLE: " + gyro.getAngle());
        autoOut.set(false);
        TeleopOut.set(false);
        warningOut.set(true);
    }

    @Override
    public void autonomousInit()
    {
        LOGGER.info("Autonomous init");
        autonomous.initAutonomous();
    }

    public void teleopInit()
    {
        
    }

    public void testInit()
    {
    }

    public void testPeriodic()
    {
    }

    public void autonomousPeriodic()
    {
        LOGGER.info("Autonomous");
        
        driverstation.readInputs();
        board.update();
        autonomous.updateAutonomousPeriodic();
        
        autoOut.set(true);
        TeleopOut.set(false);
        warningOut.set(false);
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic()
    {
        // Read driverstation inputs
        driverstation.readInputs();
        gyro.update();
        if (driverstation.getGyroReset())
        {
            System.out.println("GYRO RESET");
            gyro.reset();
        }
        LOGGER.debug("GYRO ANGLE: " + gyro.getAngle());

        if (driverstation.getCalibrate())
        {
            // Calibrate Mode
            Calibration.updateCalibrate();
        }
        else
        {
            // Drive Mode
            updateDrive();
            updateNavigator();
        }
        
        double time = DriverStation.getInstance().getMatchTime();
        if (time > 20)
        switch (DriverStation.getInstance().getAlliance()) 
        {
            case Red:
                autoOut.set(false);
                TeleopOut.set(true);
                warningOut.set(false);
                break;
            case Blue:
                autoOut.set(true);
                TeleopOut.set(true);
                warningOut.set(false);
                break;
            case Invalid:
                autoOut.set(false);
                TeleopOut.set(false);
                warningOut.set(true);
                break;
        }
        else 
        {
            autoOut.set(false);
            TeleopOut.set(true);
            warningOut.set(true);
        }

    }

    /**
     * called once per iteration to perform any necessary updates to the drive
     * system.
     */
    private void updateDrive()
    {
        DriveMode driveMode = driverstation.getDriveMode();
        switch (driveMode)
        {
            case UNWIND:
                for (Steering wheelpod : Drive.getInstance().steering)
                {
                    wheelpod.setAbsoluteAngle(0);
                }
                break;

            case REVOLVE_LARGE_LEFT:
                drive.revolveDriveLarge(Direction.LEFT);
                break;
            
            case REVOLVE_LARGE_RIGHT:
                drive.revolveDriveLarge(Direction.RIGHT);
                break;
                
            case REVOLVE_SMALL_LEFT:
                drive.revolveDriveSmall(Direction.LEFT);
                break;
                
            case REVOLVE_SMALL_RIGHT:
                drive.revolveDriveSmall(Direction.RIGHT);
                break;

            case STRAFE_LEFT:
                drive.strafeDrive(Direction.LEFT);
                break;

            case STRAFE_RIGHT:
                drive.strafeDrive(Direction.RIGHT);
                break;
                
            case TURN:
                drive.turnDrive(-driverstation.getDriveJoystick().getTwist()/2);
                break;

            case CRAB_FA:
            case CRAB_NO_FA:
                if (driverstation.getDriveJoystick().getStickDistance() < MIN_DRIVE_SPEED)
                {
                    // Don't start driving until commanded speed greater than minimum
                    drive.stop();
                }
                else
                {
                    drive.crabDrive(driverstation.getDriveJoystick().getStickAngle(), driverstation.getDriveJoystick()
                            .getStickDistance(), (driveMode == DriveMode.CRAB_FA));
                }
                break;
        }        
    }

    /**
     * Called from teleopPeriodic to drive the lifter and claw.
     */
    private void updateNavigator()
    {
        board.update();
        lifter.driveLifter(driverstation.getLiftDirection(), driverstation.getSpeedChange());
        claw.moveClaw(driverstation.getClawDirection(), driverstation.getSpeedChange());
    }
}
