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
public class Speed
{

    private static Speed speed = null;
    private Joystick467 joyLeft = null;
    private ButtonDrive buttonState = null;

    public static Speed getInstance()
    {

        if (speed == null)
        {
            speed = new Speed();
        }
        return speed;
    }

    private Speed()
    {
        this.joyLeft = Driverstation.getInstance().getDriveJoystick();
        buttonState = ButtonDrive.getInstance();
    }   
    
    public double getHybridDriveSpeed()
    {
        return -calcMultiplier(joyLeft.getStickDistance());
    }

    public double getCarDriveSpeed()
    {
        return calcMultiplier(joyLeft.getStickY());
    }

    public double getTurnInPlaceSpeed()
    {
        return calcMultiplier(-joyLeft.getTwist());//inversion purpousful
    }

    public double getCrabDriveNoFASpeed()
    {
        return calcMultiplier(joyLeft.getStickDistance());
    }

    public double getCrabDriveFASpeed()
    {
        return calcMultiplier(joyLeft.getStickDistance());
    }
    
    ///
    ///private utility functions
    ///

    private double getTurboMultiplier()
    {
        return 2.0; //multiplies by 2
    }
    
    private double getSneakMultiplier()
    {
        return 1 / (3.0); //divides by 3
    }

    /**
     * Calculates the multiplier(s) to be applied to the given speed
     * @param d initial speed
     * @return speed with multiplier(s) added
     */
    private double calcMultiplier(double d)
    {
        //Purpously does both operations if both buttons pressed...
        if (buttonState.getSneak())
        {
            d = d * getSneakMultiplier();
        } else if (buttonState.getTurbo())
        {
        	// Turbo mode disabled
//            d = d * getTurboMultiplier();
        }
        else
        {
        	// Regular speed is 80%.
        	d *= 0.8;
        }
        
        return d;
    }

}
