/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc.team467.robot;

/**
 *
 * @author kyle
 */
public class OpsCalibrate
{

    private static OpsCalibrate opsCalibrate = null;
    private ButtonCalibrate buttonCalibrate  =null;

    public static OpsCalibrate getInstance()
    {
        if (opsCalibrate == null)
        {
            opsCalibrate = new OpsCalibrate();
        }
        return opsCalibrate;
    }

    private OpsCalibrate()
    {
        buttonCalibrate = ButtonCalibrate.getInstance();
    }
    
    

    /**
     * Gets the wheel selected by the stick.
     * @param joy stick to use to select
     * @param prevSelectedWheel previously selected wheel
     * @return int val of which wheel to select
     */
    public int getWheel(Joystick467 joy, int prevSelectedWheel)
    {
        double stickAngle = joy.getStickAngle();
        //Branch into motor being calibrated
        if (joy.getStickDistance() > 0.5)
        {
            if (stickAngle < 0)
            {
                if (stickAngle < -0.5)
                {
                    return RobotMap.BACK_LEFT;
                }
                else
                {
                    return RobotMap.FRONT_LEFT;
                }
            }
            else
            {
                if (stickAngle > 0)
                {
                    if (stickAngle > 0.5)
                    {
                        return RobotMap.BACK_RIGHT;
                    }
                    else
                    {
                        return RobotMap.FRONT_RIGHT;
                    }
                }
            }
        }
        //no new selected, return previous wheel selected
        return prevSelectedWheel;
    }
    
    /**
     * Gets the angle to set the calibrating wheel.
     * @param joy joystick to get the angle from
     * @param prevCalibrationAngle previous angle to update
     * @return angle for setting the angle
     */
    public double getCalibrationAngle(Joystick467 joy, double prevCalibrationAngle)
    {
        
        // If slow pressed on stick is pressed, slow down wheel calibration.
        double rateMultiplier = (buttonCalibrate.getSlowTurn())? getSlowTurnRate() : 1;

        //Drive motor based on twist angle
        //Increase wheel angle by a small amount based on joystick twist
        prevCalibrationAngle += (joy.getTwist() / 100.0) * rateMultiplier;
        
        return prevCalibrationAngle;        
    }
    
    /**
     * rate of turn slow down modifier
     * @return 
     */
    private double getSlowTurnRate()
    {
        return 0.4;
    }
}
