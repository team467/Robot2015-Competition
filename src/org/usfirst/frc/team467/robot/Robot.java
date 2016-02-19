/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */

/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;
import org.usfirst.frc.team467.robot.LEDStrip.Mode;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
//import edu.wpi.first.wpilibj.AnalogGyro;
//import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

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
        
    Gyro2016 gyro2016;

    // Robot objects
    private DriverStation2015 driverstation;

    public Driveable drive;
    private PowerDistroBoard467 board;
    private Autonomous autonomous;
  
    private CameraDashboard cameraDashboard;
    private VisionProcessor vision = null;
        
    private Lifter lifter;
    private Claw claw;
    private Ultrasonic ultrasonic;
    private Gyro2016 Agyro;
    private DigitalInput robotID;

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
        
        CANTalon frontleft = new CANTalon(RobotMap.FRONT_LEFT_MOTOR_CHANNEL);
        CANTalon backleft = new CANTalon(RobotMap.BACK_LEFT_MOTOR_CHANNEL);
        CANTalon frontright = new CANTalon(RobotMap.FRONT_RIGHT_MOTOR_CHANNEL);
        CANTalon backright = new CANTalon(RobotMap.BACK_RIGHT_MOTOR_CHANNEL);
//        robotID = new DigitalInput(9);

        // Robot id 0 = can tank
        // Robot id 1 = kitbot tank
        int robotID = new DigitalInput(9).get() ? 1 : 0;
        
        // FIXME NOTE: You must create the correct type of drive for the robot you are driving.
//        drive = new SwerveDrive(frontleft, backleft, frontright, backright);
//        drive = new makeTalonTank(1, 0, 3, 2);
        
        if(robotID == 1) {
            
            drive = TankDrive.makeTalonTank(1, 0, 2, 3);
            LOGGER.info("Tank Set");
            
        }
        else if (robotID == 0){
            //drive = new SwerveDrive(frontleft, backleft, frontright, backright);
            drive = TankDrive.makeCANTalonTank(2, 5, 1, 6);
            LOGGER.info("CANTalon Set");
        }
        
        // Make robot objects
        driverstation = DriverStation2015.getInstance();
        autonomous = Autonomous.getInstance();
        autonomous.setDrive(drive);
        Agyro = Gyro2016.getInstance();
        board = PowerDistroBoard467.getInstance();
        vision = VisionProcessor.getInstance();
        lifter = Lifter.getInstance();
        claw = Claw.getInstance();
        gyro2016 = Gyro2016.getInstance();
        ultrasonic = new Ultrasonic(1, 0);
        ledStrip.setMode(Mode.OFF);
        
        autonomous.setDrive(drive);
        autonomous.setUltrasonic(ultrasonic);
        
        ledStrip.setMode(Mode.OFF);
        
       
        // Initialize the camera dashboard and launch in separate thread.
//        cameraDashboard = CameraDashboard.getInstance();
//        cameraDashboard.setDrive(drive);
//        if (cameraDashboard.cameraExists()) 
//        {
//            LOGGER.debug("Camera Starting");
//            cameraDashboard.start();
//        }
        
        Calibration.init(drive);
        
        LOGGER.info("Initialized robot");
    }

    public void disabledInit()
    {
        LOGGER.info("Robot disabled");
        gyro2016.reset();
    }

    public void disabledPeriodic()
    {
        vision.updateContours();
//        gyro.update();
        ledStrip.setMode(Mode.BLUE_AND_GOLD);
        
//        double angle = gyro2016.autonomous();
//        LOGGER.debug("GYRO angle : " +  angle);

        String stickType = SmartDashboard.getString("DB/String 0", "EMPTY");
        SmartDashboard.putString("DB/String 5", stickType);
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
        
//        gyro2016.reset();
        
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
//        LOGGER.info("Autonomous");
        vision.updateContours();
        LOGGER.debug("Contours updated");
//        
        driverstation.readInputs();
        LOGGER.debug("Read driverStation");
        board.update();
        LOGGER.debug("Update powerDistroBoard");
        autonomous.updateAutonomousPeriodic();
        
        autonomous.initGrabCan();
        LOGGER.debug("Gyro angle: " + Agyro.getAngle());
        
//        ledStrip.setMode(Mode.RAINBOW);
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic()
    {
        vision.updateContours();
        // Read driverstation inputs
        driverstation.readInputs();

        LOGGER.info("Distance: " + ultrasonic.getRangeInches());

//        gyro.update();
//        if (driverstation.getGyroReset())
//        {
//            System.out.println("GYRO RESET");
//            gyro.reset();
//        }
//        LOGGER.debug("GYRO ANGLE: " + gyro.getAngle());
        
//        LOGGER.info("Distance: " + ultrasonic.getRangeInches());
        SmartDashboard.putString("DB/String 9", String.valueOf(ultrasonic.getRangeInches()));
//        if (driverstation.getCalibrate())
//        {
//            // Calibrate Mode
//            Calibration.updateCalibrate();
//        }
//        else
//        {
            // Drive Mode
            updateDrive();
            updateNavigator();
//        }
        
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
                    ledStrip.setMode(Mode.PULSE_YELLOW);
                    break;
            }
//            double angle = gyro2016.autonomous();
//            LOGGER.debug("GYRO angle : " +  angle);
        }
        else if (time > 20)
        {
            ledStrip.setMode(Mode.PULSE_YELLOW);
        }
        else
        {
            ledStrip.setMode(Mode.RAINBOW);
        }
        
//        LOGGER.debug("GYRO angle : " + gyro2016.autonomous());
    }

    /**
     * called once per iteration to perform any necessary updates to the drive
     * system.
     */
    private void updateDrive()
    {
        DriveMode driveMode = driverstation.getDriveMode();
        if (driverstation.kart)
        {
            drive.cartDrive(driverstation.getDriveJoystick1());
            LOGGER.info("Kart Drive");
            return;
        }
        if (driverstation.split)
        {
            drive.splitDrive(driverstation.getDriveJoystick1(), driverstation.getDriveJoystick2());
            LOGGER.info("Split Stick Drive");
            return;
        }
        switch (driveMode)
        {
            case UNWIND:
                drive.unwind();
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
                LOGGER.info("Strafe Front");
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
                drive.turnDrive(-driverstation.getDriveJoystick1().getTurn()/2);
                break;

            case ARCADE_FA:
            case ARCADE_NO_FA:
                if (driverstation.getDriveJoystick2() == null)
                {
                    if (driverstation.getDriveJoystick1().getStickDistance() < MIN_DRIVE_SPEED)
                    {
                        // Don't start driving until commanded speed greater than minimum
                        drive.stop();
                    }
                    else
                    {
//                        drive.arcadeDrive(
//                                  driverstation.getDriveJoystick().getStickAngle(),
//                                  driverstation.getDriveJoystick().getStickDistance(),
//                                  (driveMode == DriveMode.ARCADE_FA));
                        drive.oneStickDrive(
                                driverstation.getDriveJoystick1(),
                                (driveMode == DriveMode.ARCADE_FA));
                    }
                }
                else
                {
                    drive.twoStickDrive(driverstation.getDriveJoystick1(), driverstation.getDriveJoystick2());
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
//        lifter.driveLifter(driverstation.getLiftDirection());
//        claw.moveClaw(driverstation.getClawDirection(), driverstation.getLowerCurrent());
    }
}