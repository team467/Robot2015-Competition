/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc.team467.robot;

/**
 * @author Team467 This class contains only static variables and functions, and
 * simply acts as a container for all the calibration code.
 */
public class Calibration
{
    //Creates objects

    private static Drive drive;
    private static DataStorage data;
    
    private static ButtonCalibrate buttonCalibrate;
    private static OpsCalibrate opsCalibrate;

    //Incremented angle used for calibrating wheels
    private static double calibrationAngle = 0.0;

    /**
     * Initialize calibration code
     */
    public static void init()
    {
        //makes the objects
        drive = Drive.getInstance();
        data = DataStorage.getInstance();
        buttonCalibrate = ButtonCalibrate.getInstance();
        opsCalibrate = OpsCalibrate.getInstance();
    }

    /**
     * Updates steering calibration
     *
     * @param motorId The id of the motor to calibrate
     */
    public static void updateSteeringCalibrate(int motorId, Joystick467 joy)
    {
        calibrationAngle = opsCalibrate.getCalibrationAngle(joy, calibrationAngle);
        
        if (calibrationAngle > 1.0)
        {
            calibrationAngle -= 2.0;
        }
        if (calibrationAngle < -1.0)
        {
            calibrationAngle += 2.0;
        }

        //Drive specified steering motor with no speed to allow only steering        
        drive.individualSteeringDrive(calibrationAngle, 0, motorId);

        //Write and set new center if trigger is pressed
        if (buttonCalibrate.getConfirmSelection())
        {
            double currentAngle = drive.getSteeringAngle(motorId);

            //Write data to robot
            data.putDouble(RobotMap.STEERING_KEYS[motorId], currentAngle);
            data.save();

            //Set new steering center
            drive.setSteeringCenter(motorId, currentAngle);

            //Reset calibration angle
            calibrationAngle = 0.0;
        }
    }
}
