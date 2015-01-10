/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.usfirst.frc.team467.robot;

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

    //private Camera467 cam;    

    private OpsDrive opsDrive;
    private OpsCalibrate opsCalibrate;
    private ButtonDrive buttonDrive;
    private ButtonCalibrate buttonCalibrate;
    //private LEDring LED;

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
        //cam = Camera467.getInstance();
        opsDrive = OpsDrive.getInstance();
        opsCalibrate = OpsCalibrate.getInstance();
        buttonDrive = ButtonDrive.getInstance();
        buttonCalibrate = ButtonCalibrate.getInstance();
        //SpeedCalibration.init();
        //LED = LEDring.getInstance();
        // static static static static static
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
        //Read driverstation inputs
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
        /*
         driverstation.readInputs();
         Joystick467 joy = driverstation.getNavJoystick();
         //        if(joy.buttonDown(Joystick467.TRIGGER))
         //        {            
         //            drive.driveFeeder(joy.getStickY());
         //        }
        
         comp.update();
         */

        /*
         driverstation.readInputs();
         driverstation.clearPrint();
         Joystick467 joy = driverstation.getRightJoystick();
        
         double speed = joy.getStickY();
        
         if (joy.buttonDown(4))
         {
         drive.driveParasite(speed);
         }
        
         if (joy.buttonDown(9))
         {
         gts.reset();
         }
        
         gts.update();
        
         double RPM = gts.getAccurateRPM();
        
         driverstation.println("Parasite Wheel", 1);
         driverstation.println("RPM: " + (int) RPM, 2);
         driverstation.println("Ticks: " + gts.getTicks(), 3);
         driverstation.println("Power: " + drive.getParasite().get(), 4);
         driverstation.println("Speed: ~" + (int) gts.convertRPMtoVelocity(RPM) + " ft/s", 5);
        
         if (joy.buttonDown(5))
         {
         drive.driveParasite(Math.sin((System.currentTimeMillis() - startTime)*.01));
         }
        
         driverstation.sendData();
         */
        /*
         Steering steering = drive.getSteering(RobotMap.FRONT_LEFT);
        
         steering.getMotor().set(.3);
        
         double val = drive.getSteering(RobotMap.FRONT_LEFT).getSensorValue();
        
         if (val > steeringRange) {
         steeringRange = val;
         }
        
         System.out.println(steeringRange);
         */
//</editor-fold>
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic()
    {
    }

    //this is a persistant variable to select the wheel to calibrate.
    int calibrateWheelSelect = 0;
    boolean calibrateDebounce = false;

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic()
    {
        //Read driverstation inputs
        driverstation.readInputs();

        Steering steering = drive.getSteering(RobotMap.FRONT_LEFT);
        //System.out.println("FL: " + steering.getSensorValue());
        steering.printSteeringParameters();
        
        //
        //Branch based on mode
        //Use driver's stick        
        Joystick467 joyLeft = driverstation.getDriveJoystick();
        Joystick467 joyRight = driverstation.getNavJoystick();

        //updates the buttons
        if (SINGLE_STICK_DRIVE)
        {
            buttonDrive.updateButtons(joyLeft);
            buttonCalibrate.updateButtons(joyLeft);
        }
        else
        {
            buttonDrive.updateButtons(joyLeft);
            buttonCalibrate.updateButtons(joyLeft);
        }

        if (buttonCalibrate.getCalibrate())
        {
            System.out.println("CALIBRATE");
            buttonDrive.updateButtons(joyLeft);
            updateCalibrateControl(joyLeft);
        }
        else//drive mode, not calibrate
        {
            //operates using the updated buttons
            updateDriveAndNavigate();
        }
        calibrateDebounce = buttonCalibrate.getCalibrate();

    }

    private void updateDriveAndNavigate()
    {

        ///
        ///Update Drive
        ///
        //priority for each state is intentional, not bug
        if (buttonDrive.getTurnInPlace())
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
        else//should never enter here
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
