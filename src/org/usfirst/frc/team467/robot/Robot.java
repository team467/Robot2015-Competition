/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */

/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.usfirst.frc.team467.robot;

import java.util.Comparator;

import org.apache.log4j.Logger;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.Image;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.DriverStation;
// import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;

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

    public class ParticleReport implements Comparator<ParticleReport>, Comparable<ParticleReport>
    {
        double PercentAreaToImageArea;
        double Area;
        double BoundingRectLeft;
        double BoundingRectTop;
        double BoundingRectRight;
        double BoundingRectBottom;

        public int compareTo(ParticleReport r)
        {
            return (int) (r.Area - this.Area);
        }

        public int compare(ParticleReport r1, ParticleReport r2)
        {
            return (int) (r1.Area - r2.Area);
        }
    };

    // Structure to represent the scores for the various tests used for target
    // identification
    public class Scores
    {
        double Area;
        double Aspect;
    };

    // Images
    Image binaryFrame;
    int imaqError;

    // Constants
    NIVision.Range TOTE_HUE_RANGE = new NIVision.Range(101, 64); // Default hue
                                                                 // range for
                                                                 // yellow
                                                                 // tote
    NIVision.Range TOTE_SAT_RANGE = new NIVision.Range(88, 255); // Default
                                                                 // saturation
                                                                 // range for
                                                                 // yellow
                                                                 // tote
    NIVision.Range TOTE_VAL_RANGE = new NIVision.Range(134, 255); // Default
                                                                  // value
                                                                  // range for
                                                                  // yellow
                                                                  // tote
    double AREA_MINIMUM = 0.5; // Default Area minimum for particle as a
                               // percentage of total image area
    double LONG_RATIO = 2.22; // Tote long side = 26.9 / Tote height = 12.1 =
                              // 2.22
    double SHORT_RATIO = 1.4; // Tote short side = 16.9 / Tote height = 12.1 =
                              // 1.4
    double SCORE_MIN = 70.0; // Minimum score to be considered a tote
    double VIEW_ANGLE = 49.4; // View angle fo camera, set to Axis m1011 by
                              // default, 64 for m1013, 51.7 for 206, 52 for
                              // HD3000 square, 60 for HD3000 640x480
    NIVision.ParticleFilterCriteria2 criteria[] = new NIVision.ParticleFilterCriteria2[1];
    NIVision.ParticleFilterOptions2 filterOptions = new NIVision.ParticleFilterOptions2(0, 0, 1, 1);
    Scores scores = new Scores();

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

        // Initalize the camera dashboard and launch in separate thread.
        cameraDashboard = CameraDashboard.getInstance();
        if (cameraDashboard.cameraExists()) {
            LOGGER.debug("Camera Starting");
            cameraDashboard.start();
        }

        Calibration.init();
    }

    public void disabledInit()
    {
        LOGGER.info("Robot disabled");
        autoOut.set(false);
        TeleopOut.set(false);
        warningOut.set(false);
        
    }

    public void disabledPeriodic()
    {
        gyro.update();
        LOGGER.debug("GYRO ANGLE: " + gyro.getAngle());
    }

    public void autonomousInit()
    {
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
        LOGGER.debug("Autonomous");
        
        driverstation.readInputs();
        board.update();
        autonomous.updateAutonomousPeriodic();
        autoOut.set(true);
        TeleopOut.set(false);
        warningOut.set(false);
    }

    // read file in from disk. For this example to run you need to copy
    // image.jpg from the SampleImages folder to the
    // directory shown below using FTP or SFTP:
    // http://wpilib.screenstepslive.com/s/4485/m/24166/l/282299-roborio-ftp

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic()
    {
        // Read driverstation inputs
        driverstation.readInputs();
        gyro.update();               
        if(driverstation.getGyroReset())
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
        System.out.println("time=" + time);
        
        if (time > 20) {
            DriverStation.Alliance color = DriverStation.getInstance().getAlliance();
            if (color == DriverStation.Alliance.Red) {
                autoOut.set(false);
                TeleopOut.set(true);
                warningOut.set(false);
            }
            else if (color == DriverStation.Alliance.Blue) {
                autoOut.set(true);
                TeleopOut.set(true);
                warningOut.set(false);
            }
        }
        else {
        autoOut.set(false);    
        TeleopOut.set(false);
        warningOut.set(true);
        }
    }

    /**
     * called once per iteration to perform any necessary updates to the drive
     * system.
     */
    private void updateDrive()
    {
        switch (driverstation.getDriveMode())
        {
            case UNWIND:
                for (Steering wheelpod : Drive.getInstance().steering)
                {
                    wheelpod.setAbsoluteAngle(0);
                }
                break;

            case REVOLVE_LARGE:
            {
                Direction direction = Direction.LEFT;
                if (driverstation.getDriveJoystick().buttonDown(4))
                {
                    direction = Direction.RIGHT;
                }
                drive.revolveDriveLarge(direction);
            }
                break;
                
            case REVOLVE_SMALL:
            {
                Direction direction = Direction.LEFT;
                if (driverstation.getDriveJoystick().buttonDown(6))
                {
                    direction = Direction.RIGHT;
                }
                drive.revolveDriveSmall(direction);
                
            }
                break;

            case STRAFE:
            {
                Direction direction = Direction.LEFT;
                if (driverstation.getDriveJoystick().getPOV() == 90)
                {
                    direction = Direction.RIGHT;
                }
                drive.strafeDrive(direction);
            }
                break;

            case TURN:
                drive.turnDrive(-driverstation.getDriveJoystick().getTwist()/2);
                break;

            case CRAB_FA:
                if (driverstation.getDriveJoystick().getStickDistance() < MIN_DRIVE_SPEED)
                {
                    // If in joystick deadzone, don't steer, leave wheel at current angle.
                    drive.noDrive();
                }
                else
                {
                    drive.crabDrive(driverstation.getDriveJoystick().getStickAngle(), driverstation.getDriveJoystick()
                            .getStickDistance(), true /* field aligned */);
                }
                break;

            case CRAB_NO_FA:
                if (driverstation.getDriveJoystick().getStickDistance() < MIN_DRIVE_SPEED)
                {
                    // If in joystick deadzone, don't steer, leave wheel at current angle.
                    drive.noDrive();
                }
                else
                {
                    drive.crabDrive(driverstation.getDriveJoystick().getStickAngle(), driverstation.getDriveJoystick()
                            .getStickDistance(), false /* not field aligned */);
                }
                break;

            default:  // should never enter here
                LOGGER.error("Button State not calculated correctly");
                if (driverstation.getDriveJoystick().getStickDistance() < MIN_DRIVE_SPEED)
                {
                    drive.noDrive();

                }
                else
                {
                    drive.crabDrive(driverstation.getDriveJoystick().getStickAngle(), driverstation.getDriveJoystick()
                            .getStickDistance(), false /* not field aligned */);
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
        lifter.driveLifter(driverstation.getLiftDirection(), driverstation.getMoveTurbo());
        claw.moveClaw(driverstation.getClawDirection(), driverstation.getMoveTurbo());
    }
}
