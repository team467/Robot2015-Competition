package org.usfirst.frc.team467.robot;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.MotorSafetyHelper;

public class Shooter467
{
    private static final Logger LOGGER = Logger.getLogger(Shooter467.class);
    
    private Talon leftMotor;
    private Talon rightMotor;
    private MotorSafetyHelper leftSafety;
    private MotorSafetyHelper rightSafety;
    private BallRollers roller;
    private VisionProcessor vision;
    private Driveable drive;
    
    private long timePrimeStarted;
    private boolean isPriming = false;
    private boolean isPrimed = false;
    
    private double LEFT_SPEED = 0.8;
    private double RIGHT_SPEED = 0.8;
    
    public Shooter467(int leftMotorChannel, int rightMotorChannel, Driveable drive, BallRollers roller, VisionProcessor vision)
    {
        leftMotor = new Talon(leftMotorChannel);
        leftSafety = new MotorSafetyHelper(leftMotor);
        rightMotor = new Talon(rightMotorChannel);
        rightSafety = new MotorSafetyHelper(rightMotor);
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
            LOGGER.debug("aim no vision");
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
            LOGGER.debug("aim no contours");

            // Still haven't found what I'm looking for
            drive.stop();
            return false;
        }
        // Find the widest contour
        VisionProcessor.Contour widest = Collections.max(contours, new VisionProcessor.WidthComp());
        double offset;
        try
        {
            offset = Double.valueOf(SmartDashboard.getString("DB/String 1"));
        }
        catch (Exception e)
        {
            offset = 0.0;
        }
        final double centerX = widest.getCenterX() + offset;
        final double delta = Math.abs(centerX - horizontalCenter);
        LOGGER.debug("Found widest contour, centerX=" + centerX + " delta=" + delta);
        if (delta < marginOfError)
        {
            LOGGER.debug("aim found target");

            // Found target
            return true;
        }
//        if (widest.getCenterX() - vision.getHorizontalCenter()) 
        int direction = widest.getCenterX() > horizontalCenter ? -1 : 1;
        final double turnSpeed = direction * (minTurnSpeed + turnSpeedRange * (delta/horizontalCenter));
        drive.turnDrive(turnSpeed);
        
        LOGGER.debug("aim target seen but not centered");
        return false;
    }
    
    /**
     * xxxxxxxxxx
     * 
     * @param time how long to prime before shooting, in seconds
     */
    private boolean prime(double time)
    {
        if (isPrimed)
        {
            LOGGER.debug("prime isPrimed");
            // Don't slow down when you are primed
            leftMotor.set(LEFT_SPEED);
            rightMotor.set(RIGHT_SPEED);
            return true;
        }
        
        if (isPriming)
        {
            final long now = System.currentTimeMillis();
            LOGGER.debug("prime isPriming timePrimeStarted=" + timePrimeStarted + " now=" + now);
            if ((time * 1000) > now - timePrimeStarted)
            {
                isPrimed = true;
            }
            leftMotor.set(LEFT_SPEED);
            rightMotor.set(RIGHT_SPEED);
            return isPrimed;
        }
        
        // Wasn't priming or primed
        LOGGER.debug("prime begin priming");
        timePrimeStarted = System.currentTimeMillis();
        isPriming = true;
        return false;
    }
    
    public void shootNow()
    {
        if (prime(5.0))
        {
            roller.rollIn();
        }
    }
    
    public void aimAndShoot()
    {
        LOGGER.debug("aimAndShoot aiming");
        
        // Bypass short-circuit logic on &&
        final boolean isOnTarget = aim(30);
        final boolean isPrimed = prime(5.0);

        LOGGER.debug("aimAndShoot aiming isOnTarget=" + isOnTarget + " isPrimed=" + isPrimed);
        if (isOnTarget && isPrimed)
        {
            LOGGER.debug("aimAndShoot shooting");
            roller.rollIn();
        }
    }
    
    public void stop()
    {
        LOGGER.debug("stop");
        
        leftMotor.stopMotor();
        rightMotor.stopMotor();
        
        isPrimed = false;
        isPriming = false;
    }
}
