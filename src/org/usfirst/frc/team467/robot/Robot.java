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
        

    // Robot objects
    private DriverStation2016 driverstation;

    public Driveable drive;
    private PowerDistroBoard467 board;
    private Autonomous autonomous;
//    private LEDStrip ledStrip;
    
    private CameraDashboard cameraDashboard;
    private VisionProcessor vision = null;

    private BallRollers rollers;
    private TBar tbar;
    private Shooter467 highShooter;
    
//    private Lifter lifter;
//    private Claw claw;
    private Ultrasonic2016 ultrasonic;
    private Gyro2016 gyro;
    private Infrared infra;
    private DigitalInput robotID;
    
    int session;
            
    //private LEDStrip ledStrip = new LEDStrip();

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
        try
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
            RobotID robotID = new DigitalInput(9).get() ? RobotID.KITBOT : RobotID.TANK2016;
            
            // FIXME NOTE: You must create the correct type of drive for the robot you are driving.
//        drive = new SwerveDrive(frontleft, backleft, frontright, backright);
//        drive = new makeTalonTank(1, 0, 3, 2);
            
         // Make robot objects
            driverstation = DriverStation2016.getInstance();
            autonomous = Autonomous.getInstance();
            board = PowerDistroBoard467.getInstance();
            vision = VisionProcessor.getInstance();
            infra = new Infrared(4);
            rollers = new BallRollers(RobotMap.ROLLER_MOTOR_CHANNEL, RobotMap.MANIPULATOR_MOTOR_CHANNEL, infra, driverstation);
            tbar = new TBar(RobotMap.TBAR_MOTOR_CHANNEL);
            
            if(robotID == RobotID.KITBOT)
            {    
                drive = TankDrive.makeTalonTank(
                        RobotMap.FRONT_LEFT_KITBOT,
                        RobotMap.FRONT_RIGHT_KITBOT,
                        RobotMap.BACK_LEFT_KITBOT,
                        RobotMap.BACK_RIGHT_KITBOT);
                LOGGER.info("Kitbot Set");
                highShooter = new Shooter467(null, null, drive, rollers, vision);

            }
            else if (robotID == RobotID.TANK2016)
            {
                //drive = new SwerveDrive(frontleft, backleft, frontright, backright);
                drive = TankDrive.makeCANTalonTank(
                        RobotMap.FRONT_LEFT_2016,
                        RobotMap.FRONT_RIGHT_2016,
                        RobotMap.BACK_LEFT_2016,
                        RobotMap.BACK_RIGHT_2016);
                LOGGER.info("CANTalon Set");
                highShooter = new Shooter467(RobotMap.LEFT_SHOOTER_MOTOR_CHANNEL, RobotMap.RIGHT_SHOOTER_MOTOR_CHANNEL, drive, rollers, vision);

            }
            
            gyro = Gyro2016.getInstance();
            ultrasonic = new Ultrasonic2016();
            
            
            autonomous.setDrive(drive);
            autonomous.setRoller(rollers);
            autonomous.setUltrasonic(ultrasonic);
            autonomous.setShooter(highShooter);
            autonomous.setTBar(tbar);
    //        ledStrip.setMode(Mode.OFF);
            
            // Initialize the camera dashboard and launch in separate thread.
            cameraDashboard = CameraDashboard.getInstance();
            cameraDashboard.setDrive(drive);
            LOGGER.debug("Camera Starting");
            cameraDashboard.start();
            
            Calibration.init(drive);
            
            LOGGER.info("Initialized robot");
        }
        catch (Exception e)
        {
            LOGGER.error("robotInit: " , e);
        }
    }

    public void disabledInit()
    {
        LOGGER.info("Robot disabled");
        gyro.reset();
    }

    public void disabledPeriodic()
    {
        vision.updateContours();
//        gyro.update();
//          ledStrip.setMode(Mode.BLUE_AND_GOLD);
        
//        double angle = gyro2016.autonomous();
//        LOGGER.debug("GYRO angle : " +  angle);
        drive.stop();
        drive.feedMotorSafety();

        String stickType = SmartDashboard.getString("DB/String 0", "EMPTY");
        SmartDashboard.putString("DB/String 5", stickType);
//        LOGGER.info("Rotation Sensor: " + tbar.rotationSensor.getAverageValue());
    }

    @Override
    public void autonomousInit()
    {
        LOGGER.info("Autonomous init");
        autonomous.initAutonomous();
        gyro.reset();
    }

    public void teleopInit()
    {
        if (!cameraDashboard.isAlive()) 
        {
            LOGGER.debug("Camera Starting");
            cameraDashboard.start();
        }
        LOGGER.info("Teleop init");
//        rollers.reset();
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
        
        LOGGER.debug("Yaw angle: " + gyro.getYawAngle());
        LOGGER.debug("Tilt angle: " + gyro.getTiltAngle());

        
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
        
        //LOGGER.info("Detected: " + infra.getInfrared());

        //LOGGER.info("Distance: " + ultrasonic.getRangeInches());

//        gyro.update();
//        if (driverstation.getGyroReset())
//        {
//            System.out.println("GYRO RESET");
//            gyro.reset();
//        }
//        LOGGER.debug("GYRO ANGLE: " + gyro.getAngle());
        
//        LOGGER.info("Distance: " + ultrasonic.getRangeInches());
//        SmartDashboard.putString("DB/String 9", String.valueOf(ultrasonic.getRangeInches()));
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
                    //ledStrip.setMode(Mode.PULSE_RED);
                    break;
                case Blue:
                    //ledStrip.setMode(Mode.PULSE_BLUE);
                    break;
                case Invalid:
                    //ledStrip.setMode(Mode.PULSE_YELLOW);
                    break;
            }
//            double angle = gyro2016.autonomous();
//            LOGGER.debug("GYRO angle : " +  angle);
        }
        else if (time > 20)
        {
            //ledStrip.setMode(Mode.PULSE_YELLOW);
        }
        else
        {
            //ledStrip.setMode(Mode.RAINBOW);
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
        rollers.runRoller(driverstation.getRollerDirection());
        SmartDashboard.putString("DB/String 8", driverstation.getManipPosition().toString());
        rollers.runManipulator(driverstation.getManipPosition());
        tbar.launchTBar(driverstation.getTBarDirection());
        driverstation.setIntakeLED(infra.getInfrared());
        
        if (driverstation.highShooterButton())
        {
            highShooter.shootNow();
        }
//        else if (driverstation.aimShooterButton())
//        {
//            highShooter.aimAndShoot();
//        }
        else
        {
            highShooter.stop();
        }
        
//        lifter.driveLifter(driverstation.getLiftDirection());
//        claw.moveClaw(driverstation.getClawDirection(), driverstation.getLowerCurrent());
    }
}