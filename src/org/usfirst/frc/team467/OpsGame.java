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
public class OpsGame
{

    private static OpsGame opsGame = null;
    private Launcher launcher = null;
    private Feeder feeder = null;

    public static OpsGame getInstance()
    {
        if (opsGame == null)
        {
            opsGame = new OpsGame();
        }
        return opsGame;
    }

    private OpsGame()
    {
        launcher = Launcher.getInstance();
        feeder = Feeder.getInstance();
    }
    
    public void fire()
    {
        launcher.fireLauncher();
    }
    
    public void pullBack()
    {
        launcher.pullBackLauncher();
    }
    
    public void feedIn()
    {
        feeder.driveFeederMotor(-1.0);
    }
    
    public void feedOut()
    {
        feeder.driveFeederMotor(1.0);
    }
    
    public void feedHalt()
    {
        feeder.driveFeederMotor(0.0);
    }
    
    public void feedDeploy()
    {
        feeder.lowerFeeder();
    }
    
    public void feedRetract()
    {
        feeder.raiseFeeder();
    }
}
