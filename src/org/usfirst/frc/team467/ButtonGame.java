/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc.team467;

/**
 *
 * @author kyle
 */
public class ButtonGame
{

    private static ButtonGame buttonGame = null;

    private boolean fire = false;
    private boolean feedIn = false;
    private boolean feedOut = false;
    private boolean feederDeployed = false;

    public static ButtonGame getInstance()
    {
        if (buttonGame == null)
        {
            buttonGame = new ButtonGame();
        }
        return buttonGame;
    }

    private ButtonGame()
    {
    }

    /**
     * Update the value of the buttons. Must be called <b>every</b> loop.
     *
     * @param joy
     */
    public void updateButtons(Joystick467 joy)
    {
        feedOut = (joy.getHatY() > 0.5);
        feedIn = (joy.getHatY() < -0.5);
        fire = joy.getFlap();
        feederDeployed = joy.getStickY() < -0.5;
//        System.out.println("Fire " + fire + " FeedIn " + feedIn + " FeedOut " + feedOut + " FeederDep " + feederDeployed);
    }
    
    public boolean getFire()
    {
        return false;
//        return fire;
    }
    
    public boolean getFeedIn()
    {
        return false;
//        return feedIn;
    }
    
    public boolean getFeedOut()
    {
        return false;
//        return feedOut;
    }    
    
    public boolean getFeedDeployed()
    {
        return false;
//        return feederDeployed;
    }
}
