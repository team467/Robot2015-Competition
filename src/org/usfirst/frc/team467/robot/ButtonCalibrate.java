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
public class ButtonCalibrate
{
    private static ButtonCalibrate buttonGame = null;

    private boolean calibrate = false;
    
    private boolean confirmSelection = false;    
    private boolean slowTurn = false;    

    public static ButtonCalibrate getInstance()
    {
        if (buttonGame == null)
        {
            buttonGame = new ButtonCalibrate();
        }
        return buttonGame;
    }

    private ButtonCalibrate()
    {
    }
    
    public boolean getCalibrate()
    {
        return calibrate;
    }
    
    public boolean getConfirmSelection()
    {
        return confirmSelection;
    }
    
    public boolean getSlowTurn()
    {
        return slowTurn;
    }        
    
    public void updateButtons(Joystick467 joy)
    {
        calibrate = joy.getFlap();
        confirmSelection = joy.buttonDown(Joystick467.TRIGGER);
        slowTurn = joy.buttonDown(4);
    }
}
