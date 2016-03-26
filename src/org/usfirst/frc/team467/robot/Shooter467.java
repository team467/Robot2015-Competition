package org.usfirst.frc.team467.robot;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.MotorSafetyHelper;

public class Shooter467
{
    private static final Logger LOGGER = Logger.getLogger(Shooter467.class);
    
    private Talon leftMotor = null;
    private Talon rightMotor = null;
    private MotorSafetyHelper leftSafety = null;
    private MotorSafetyHelper rightSafety = null;
    private BallRollers roller;
    private VisionProcessor vision;
    private Driveable drive;
    
    private long timePrimeStarted;
    private boolean isPriming = false;
    private boolean isPrimed = false;
    
    private double LEFT_SPEED = -1.0;
    private double RIGHT_SPEED = 1.0;
    
    public Shooter467(Integer leftMotorChannel, Integer rightMotorChannel, Driveable drive, BallRollers roller, VisionProcessor vision)
    {
        if (leftMotorChannel != null) {
            leftMotor = new Talon(leftMotorChannel);
            leftSafety = new MotorSafetyHelper(leftMotor);
        }
        if (rightMotorChannel != null) {
        rightMotor = new Talon(rightMotorChannel);
        rightSafety = new MotorSafetyHelper(rightMotor);
        }
        this.drive = drive;
        this.roller = roller;
        this.vision = vision;
    }
    
    /**
     * 
     * @param marginOfError
     * @return if robot successfully centered on target
     */
    public boolean aim(int marginOfError)
    {
        if (!vision.isEnabled())
        {
            drive.stop();
            return false;
        }
        
//        final double minTurnSpeed = Double.parseDouble(SmartDashboard.getString("DB/String 3", "0.0"));
//        final double maxTurnSpeed = Double.parseDouble(SmartDashboard.getString("DB/String 4", "0.0"));
        final double minTurnSpeed = 0.3; // Double.parseDouble(SmartDashboard.getString("DB/String 3", "0.0"));
        final double maxTurnSpeed = 0.45; // Double.parseDouble(SmartDashboard.getString("DB/String 4", "0.0"));
        final double turnSpeedRange = maxTurnSpeed - minTurnSpeed;
        final double horizontalCenter = vision.getHorizontalCenter();
        
        LOGGER.debug("start seekWidestContour() minTurnSpeed=" + minTurnSpeed + " maxTurnSpeed=" + maxTurnSpeed);
        List<VisionProcessor.Contour> contours = vision.getContours();
        LOGGER.debug("Found " + contours.size() + " contours");
        
        if (contours.size() == 0)
        {
            // Still haven't found what I'm looking for
            drive.stop();
            return false;
        }
        // Find the widest contour
        VisionProcessor.Contour widest = Collections.max(contours, new VisionProcessor.WidthComp());
        final double centerX = widest.getCenterX();
        final double delta = Math.abs(centerX - horizontalCenter);
        LOGGER.debug("Found widest contour, centerX=" + centerX + " delta=" + delta);
        if (delta < marginOfError)
        {
            // Found target
            return true;
        }
//        if (widest.getCenterX() - vision.getHorizontalCenter()) 
        int direction = widest.getCenterX() > horizontalCenter ? -1 : 1;
        LOGGER.debug("direction=" + direction);
        final double turnSpeed = direction * (minTurnSpeed + turnSpeedRange * (delta/horizontalCenter));
        drive.turnDrive(turnSpeed);
        LOGGER.info("Turned with turnSpeed " + turnSpeed);
        LOGGER.debug("end seekWidestContour()");
        
        // Target seen but not centered
        return false;
    }
    
    private void motorsOn()
    {
        if (leftMotor != null)
        {
            leftMotor.set(LEFT_SPEED);
            leftSafety.feed();
        }
        if (rightMotor != null)
        {
            rightMotor.set(RIGHT_SPEED);
            rightSafety.feed();
        }
    }
    
    /**
     * @param time how long to prime before shooting, in seconds
     */
    boolean prime(double time)
    {
        long lTime = (long)time*1000;
        LOGGER.debug("isPrimed=" + isPrimed);
        if (isPrimed)
        {
            LOGGER.debug("Primed");
            motorsOn();
            return true;
        }
        
        if (isPriming)
        {
            LOGGER.debug("Primimg");
            final long now = System.currentTimeMillis();
            long delta = now - timePrimeStarted;
            if (delta > lTime)
            {
                LOGGER.debug("became primed, time=" + lTime + " now="
                        + now+ " timePrimeStarted=" + timePrimeStarted);
                isPrimed = true;
            }
            motorsOn();
            return isPrimed;
        }
        
        // Wasn't priming or primed
        LOGGER.debug("Start Priming");
        timePrimeStarted = System.currentTimeMillis();
        isPriming = true;
        return false;
    }
    
    public void shootNow()
    {
        LOGGER.debug("Prime and shoot");
        if (prime(3.0))
        {
            LOGGER.debug("SHOOT!");
            roller.rollIn();
        }
    }
    
    public void aimAndShoot()
    {
        LOGGER.debug("Aim, prime, and shoot");
        // Bypass short-circuit logic on &&
        final boolean isOnTarget = aim(30);
        final boolean isPrimed = prime(5.0);
        if (isOnTarget && isPrimed)
        {
            LOGGER.debug("SHOOT!");
            roller.rollIn();
        }
    }
    
    public void stop()
    {
        LOGGER.debug("stop");
        if (leftMotor != null)
        {
            leftMotor.set(0);
            leftSafety.feed();
        }
        if (rightMotor != null)
        {
            rightMotor.set(0);
            rightSafety.feed();
        }
        isPriming = false;
        isPrimed = false;
    }
}
