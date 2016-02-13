package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.Talon;

public class tBar
{
    private static final Logger LOGGER = Logger.getLogger(tBar.class);
    
    private static tBar mrT = null;
    
    private Talon tMotor = null;
    
    private PowerDistroBoard467 board = null;
    
    public boolean tUp;
    public boolean tDown;
    
    
    public static tBar getInstance() {
        
        if(mrT == null) {
            
            mrT = new tBar();
            
        }
        return mrT;
    }
    
    private tBar() {
        
        tMotor = new Talon(RobotMap.TBAR_MOTOR_CHANNEL);
        
        board = PowerDistroBoard467.getInstance();
    }
    public void stop() {
        tMotor.set(0);
    }
    
    public void launchTBar(tBarDirection tBarDirection) {
        
        switch(tBarDirection) {
            case DOWN:
                tMotor.set(0.2);
                break;
            case UP:
                tMotor.set(-0.2);
                break;
            case STOP:
                stop();
                break;
        }
        
        
    }
    enum tBarDirection {
        DOWN, UP, STOP
    }

}
