/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */

/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;
import org.usfirst.frc.team467.robot.LEDStrip.Mode;

import edu.wpi.first.wpilibj.DriverStation;
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
    private static final Logger LOGGER = Logger.getLogger(Robot.class);

    private static final double MIN_DRIVE_SPEED = 0.1;

    // Robot objects
    private DriverStation2015 driverstation;

    private Drive drive;
    private PowerDistroBoard467 board;
    private Autonomous autonomous;

//    private CameraDashboard cameraDashboard;
    private VisionProcessor vision = null;
    
    private Lifter lifter;
    private Claw claw;
//    private Gyro2015 gyro;

    int session;
    
    private LEDStrip ledStrip = new LEDStrip();

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
        vision = VisionProcessor.getInstance();
        lifter = Lifter.getInstance();
        claw = Claw.getInstance();
//        gyro = Gyro2015.getInstance();
        ledStrip.setMode(Mode.OFF);

        // Initialize the camera dashboard and launch in separate thread.
//        cameraDashboard = CameraDashboard.getInstance();
//        if (cameraDashboard.cameraExists()) 
//        {
//            LOGGER.debug("Camera Starting");
//            cameraDashboard.start();
//        }
        
        Calibration.init();
        
        LOGGER.info("Initialized robot");
    }

    public void disabledInit()
    {
        LOGGER.info("Robot disabled");
    }

    public void disabledPeriodic()
    {
        vision.updateContours();
//        gyro.update();
        ledStrip.setMode(Mode.BLUE_AND_GOLD);
    }

    @Override
    public void autonomousInit()
    {
        LOGGER.info("Autonomous init");
        autonomous.initAutonomous();
    }

    public void teleopInit()
    {
        LOGGER.info("Teleop init");
    }

    public void testInit()
    {
        LOGGER.info("Test init");
    }

    public void testPeriodic()
    {
    }

    public void autonomousPeriodic()
    {
        LOGGER.info("Autonomous");
        vision.updateContours();
        LOGGER.debug("Contours updated");
        
        driverstation.readInputs();
        LOGGER.debug("Read driverStation");
        board.update();
        LOGGER.debug("Update powerDistroBoard");
        autonomous.updateAutonomousPeriodic();
        
        ledStrip.setMode(Mode.RAINBOW);
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic()
    {
        vision.updateContours();
        // Read driverstation inputs
        driverstation.readInputs();
//        gyro.update();
//        if (driverstation.getGyroReset())
//        {
//            System.out.println("GYRO RESET");
//            gyro.reset();
//        }
//        LOGGER.debug("GYRO ANGLE: " + gyro.getAngle());

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
        if (time > 40)
        {
            switch (DriverStation.getInstance().getAlliance()) 
            {
                case Red:
                    ledStrip.setMode(Mode.PULSE_RED);
                    break;
                case Blue:
                    ledStrip.setMode(Mode.PULSE_BLUE);
                    break;
                case Invalid:
                    ledStrip.setMode(Mode.BLUE_AND_GOLD);
                    break;
            }
        }
        else if (time > 20)
        {
            ledStrip.setMode(Mode.PULSE_YELLOW);
        }
        else
        {
            ledStrip.setMode(Mode.RAINBOW);
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
                
            case STRAFE_FRONT:
                drive.strafeDrive(Direction.FRONT);
                break;
                
            case STRAFE_LEFT:
                drive.strafeDrive(Direction.LEFT);
                break;

            case STRAFE_RIGHT:
                drive.strafeDrive(Direction.RIGHT);
                break;
                
            case STRAFE_BACK:
                drive.strafeDrive(Direction.BACK);
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
        lifter.driveLifter(driverstation.getLiftDirection());
        claw.moveClaw(driverstation.getClawDirection(), driverstation.getLowerCurrent());
    }
}