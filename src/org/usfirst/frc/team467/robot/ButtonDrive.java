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
public class ButtonDrive
{
    private static ButtonDrive buttonDrive = null;

    //bools    
    private boolean carDrive = false;
    private boolean crabDriveFA = false;
    private boolean crabDriveNoFA = false;    
    private boolean hybridDrive = false;
    private boolean strafeDrive = false;
    private boolean turnInPlace = false;   
    private boolean sneak = false;
    private boolean turbo = false;    

    /**
     * Singleton
     *
     * @return
     */
    public static ButtonDrive getInstance()
    {
        if (buttonDrive == null)
        {
            buttonDrive = new ButtonDrive();
        }
        return buttonDrive;
    }

    /**
     * Private constructor
     */
    private ButtonDrive()
    {
    }
    
    /**
     * Update the value of the buttons. Must be called <b>every</b> loop.
     * @param joyLeft 
     */
    public void updateButtons(Joystick467 joyLeft)
    {        
        carDrive = joyLeft.buttonDown(3);         
        hybridDrive = joyLeft.buttonDown(4);
        //crabDriveFA = joyLeft.buttonDown(5);
        turnInPlace = joyLeft.buttonDown(2);
        strafeDrive = joyLeft.buttonDown(5) || joyLeft.buttonDown(6);
        crabDriveNoFA = !(carDrive || crabDriveFA || turnInPlace || hybridDrive);        
        sneak = joyLeft.buttonDown(Joystick467.TRIGGER);
        turbo = joyLeft.buttonDown(7);                
    }
    
    /**
     * Update the value of the buttons. Must be called <b>every</b> loop.
     * @param joyLeft
     * @param joyRight 
     */
    public void updateButtons(Joystick467 joyLeft, Joystick467 joyRight)
    {

    }

    public boolean getSneak()
    {
        return sneak;
    }    
    
    public boolean getTurbo()
    {
        return turbo;
    }
        
    public boolean getCarDrive()
    {
        return carDrive;
    }

    public boolean getCrabDriveFA()
    {
        return crabDriveFA;
    }

    public boolean getCrabDriveNoFA()
    {
        return crabDriveNoFA;
    }
    
    public boolean getTurnInPlace()
    {
        return turnInPlace;
    }    

    public boolean getHybridDrive()
    {
        return hybridDrive;
    }
    
    public boolean getStrafeDrive()
    {
    	return strafeDrive;
    }

}
