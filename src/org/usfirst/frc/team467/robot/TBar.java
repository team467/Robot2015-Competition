package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Talon;

public class TBar
{
    private static final Logger LOGGER = Logger.getLogger(TBar.class);
    
    private static TBar mrT = null;
    
    private CANTalon tMotor = null;
    
    public static TBar getInstance() {
        
        if(mrT == null) {
            
            mrT = new TBar();
            
        }
        return mrT;
    }
    
    private TBar() {
        
        //tMotor = new CANTalon(0); //switch to actual port number
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
