package org.usfirst.frc.team467.robot;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.usfirst.frc.team467.robot.DriverStation2015.Speed;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.apache.log4j.Logger;

public class Autonomous
{
    private static final Logger LOGGER = Logger.getLogger(Autonomous.class);

    private static Autonomous autonomous = null;

    Gyro2016 gyro = null;    
    private Driveable drive = null;
//    private Claw claw = null;
//    private Lifter lifter = null;
    private VisionProcessor vision = null;
    private Ultrasonic ultrasonic = null;
    
    long actionStartTimeMS = -1;

    /**
     * Run the said action while the condition returns true.
     */
    private interface WhileCondition
    {
        boolean call();
    }
    
    /**
     * Should Always contain:<br>
     * • Lifter<br>
     * • Claw<br>
     * • Drive
     */
    private interface Action
    {
        void call();
    }

    private static class Activity
    {
        // The WhileCondition should return true until the action returns false.
        private final String description;
        private final WhileCondition whileCondition;
        private final Action action;

        private Activity(String description, WhileCondition whileCondition, Action action)
        {
            this.description = description;
            this.whileCondition = whileCondition;
            this.action = action;
        }

        public String getDescription()
        {
            return description;
        }

        void doIt()
        {
            action.call();
        }

        boolean isDone()
        {
            return !whileCondition.call();
        }
    }

    List<Activity> actions = new LinkedList<Activity>();

    private void addAction(String description, WhileCondition whileCondition, Action action)
    {
        actions.add(new Activity(description, whileCondition, action));
    }

    // Returns true for durationSecs, then returns false.
    private boolean forDurationSecs(float durationSecs)
    {
        LOGGER.debug("forDurationSecs durationSecs=" + durationSecs);
        if (actionStartTimeMS < 0)
        {
            resetActionStartTime();
        }
        LOGGER.debug("forDurationSecs actionStartTimeMS=" + actionStartTimeMS);

        final long now = System.currentTimeMillis();
        final long durationMS = (long) (1000 * durationSecs);
        return now < actionStartTimeMS + durationMS;
    }
    
    private boolean whileWidestNotCentered(int marginOfError)
    {
        if (!vision.isEnabled())
        {
            return true;
        }
            
        try
        {
            List<VisionProcessor.Contour> contours = vision.getContours();
            VisionProcessor.Contour contour;
            contour = Collections.max(contours, new VisionProcessor.WidthComp());
            final double centerX = contour.getCenterX();
            final double delta = Math.abs(centerX - vision.getHorizontalCenter());
            LOGGER.debug("untilCentered centerY=" + centerX + " delta=" + delta);
            return delta > marginOfError;
        }
        catch (Exception e)
        {
            LOGGER.info("Missed contour: " + e);
            return true;
        }
    }
    
    private void resetActionStartTime()
    {
        actionStartTimeMS = System.currentTimeMillis();
    }

    // Always returns false, indicates is never done.
    private boolean forever()
    {
        // Never done.
        return true;
    }

    /**
     * Gets a singleton instance of the Autonomous
     * 
     * @return Autonomous object
     */
    public static Autonomous getInstance()
    {
        if (autonomous == null)
        {
            autonomous = new Autonomous(VisionProcessor.getInstance(), Gyro2016.getInstance());
        }
        return autonomous;
    }
    
    public void setDrive(Driveable drive)
    {
        this.drive = drive;
    }

    public void setUltrasonic(Ultrasonic ultra)
    {
        this.ultrasonic = ultra;
    }

    /**
     * Private constructor to setup the Autonomous
     * @param gyro2 
     */
    private Autonomous(VisionProcessor vision, Gyro2016 gyro)
    {
        // TODO Change drive, claw, and lifter to generics implementing respective interfaces
//        this.claw = claw;
//        this.lifter = lifter;
        this.vision = vision;
        this.gyro = gyro;
    }

    /**
     * Sets up the periodic function
     */
    public void initAutonomous()
    {
        AutoType autonomousType = DriverStation2015.getInstance().getAutoType();
        LOGGER.info("AUTO MODE " + autonomousType);

        // Reset actions.
        actions.clear();
        resetActionStartTime();
        
        // Set up gyro and create actions list.
        switch (autonomousType)
        {
            case AIM:
                initAim();
                break;
            case GRAB_CAN:
                initGrabCan();
                break;
            default:
                initStayInPlace();
                break;
        }

        LOGGER.info("Beginning action: " + actions.get(0).getDescription());
    }

    public void initGrabCan()
    {
        // Start facing the wall in front of an item (container or tote), pick it up
        // and carry it rolling backwards to the auto zone.
        
        Gyro2016 Agyro = Gyro2016.getInstance();//.reset(GyroResetDirection.FACE_TOWARD);// reset to upfield
        addAction("Move while flat",
                () -> gyro.isFlat(),
                () -> {
                    drive.arcadeDrive(0.0, -0.7);
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                });
        addAction("Move forward until gyro is 7 degrees",
                () -> gyro.up(),
                () -> {
                    drive.arcadeDrive(0.0, -0.7);
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                });
        if(gyro.isFlat()){
            addAction("Do nothing",
                    () -> forDurationSecs(1.0f),
                    () ->{
                        drive.stop();
                        LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                    });
        }
        addAction("Move",
                () -> gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Move foward while gyro is pointing down",
                () -> gyro.down(),
                () ->{
                    drive.arcadeDrive(0.0, -0.5);
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                });
        addAction("Move",
                () -> forDurationSecs(1.0f),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                });
        addAction("Stop moving",
                () -> gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                    drive.stop();
                });
    }
    
    private void initStayInPlace()
    {
        // Stay in place. Reset to upfield.
//        Gyro2015.getInstance().reset();
          Gyro2016.getInstance();
        addAction("Stop driving", 
                () -> forever(), 
                () -> {
                    drive.stop();
                });
    }
    
    private void initAim()
    {
        final int marginOfError = 30;
        
        addAction("Rotate while square with widest is not centered",
//                () -> untilWidestCentered(marginOfError),
                () -> forever(),
                () -> {
                    seekWidestContour(marginOfError);
                });
        
        addAction("Stop driving",
                () -> forever(),
                () -> {
                    drive.stop();
                });
    }

    private void seekWidestContour(int marginOfError)
    {
        final boolean targetIsCentered = seekAngle(marginOfError);
        SmartDashboard.putString("DB/String 8", "Centered: " + targetIsCentered);
        if (targetIsCentered)
        {
            approach(40);
        }
    }
    
    /**
     * 
     * @param marginOfError
     * @return if robot successfully centered on target
     */
    private boolean seekAngle(int marginOfError)
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
        final double turnSpeed = direction * (minTurnSpeed + turnSpeedRange * (delta/horizontalCenter));
        drive.turnDrive(turnSpeed);
        LOGGER.info("Turned with turnSpeed " + turnSpeed);
        LOGGER.debug("end seekWidestContour()");
        
        // Target seen but not centered
        return false;
    }
    
    /**
     * 
     * @param desiredDistance in inches
     */
    private void approach(double desiredDistance)
    {
        final double measuredDistance = ultrasonic.getRangeInches();
        final double delta = Math.abs(measuredDistance - desiredDistance);
        if (delta > 12)
        {
            if (measuredDistance > desiredDistance)
            {
                drive.strafeDrive(Direction.FRONT);
            }
            else
            {
                drive.strafeDrive(Direction.BACK);
            }
        }
        else
        {
            drive.stop();
        }
    }
    
    /**
     * Updates the Autonomous routine. Called by Robot.autonomousPeriodic().
     */
    public void updateAutonomousPeriodic()
    {
        // Make sure there something to do.
        if (actions.isEmpty())
        {
            LOGGER.debug("No more actions");
            return;
        }

        Activity currentAction = actions.get(0);
        if (currentAction.isDone())
        {
            // This action is done. Remove it from the list and prepare the next one.
            LOGGER.info("Finished action: " + currentAction.getDescription());
            actions.remove(0);
            resetActionStartTime();
            if (actions.isEmpty())
            {
                // Ran out of actions. We're done.
                return;
            }

            // Move on to the next action.
            currentAction = actions.get(0);
            LOGGER.info("Beginning action: " + currentAction.getDescription());
        }

        LOGGER.debug("Running action: " + currentAction.getDescription() + " number of actions: " + actions.size());
        currentAction.doIt();
    }

    /**
     * Type of Autonomous drive to use.
     *
     */
    enum AutoType
    {
        NO_AUTO, AIM, DRIVE_ONLY, GRAB_CAN, HOOK_AND_PUSH, HOOK_AND_PUSH_OVER_RAMP
    }
}
