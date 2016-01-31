package org.usfirst.frc.team467.robot;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.usfirst.frc.team467.robot.DriverStation2015.Speed;

import edu.wpi.first.wpilibj.Ultrasonic;

import org.apache.log4j.Logger;

public class Autonomous
{
    private static final Logger LOGGER = Logger.getLogger(Autonomous.class);

    private static Autonomous autonomous = null;

    private Driveable drive = null;
    private Claw claw = null;
    private Lifter lifter = null;
    private VisionProcessor vision = null;
    private Ultrasonic ultrasonic = null;

    long actionStartTimeMS = -1;

    /**
     * Run the said action until this returns true.
     */
    private interface DoneCondition
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
        // The doneCondition should return false until then condition is met, then return true.
        private final String description;
        private final DoneCondition doneCondition;
        private final Action action;

        private Activity(String description, DoneCondition doneCondition, Action action)
        {
            this.description = description;
            this.doneCondition = doneCondition;
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
            return doneCondition.call();
        }
    }

    List<Activity> actions = new LinkedList<Activity>();

    private void addAction(String description, DoneCondition doneCondition, Action action)
    {
        actions.add(new Activity(description, doneCondition, action));
    }

    // Returns false for durationSecs, then returns true.
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
        return now > actionStartTimeMS + durationMS;
    }
    
    private boolean untilWidestCentered(int marginOfError)
    {
        if (!vision.isEnabled())
        {
            return false;
        }
            
        try
        {
            List<VisionProcessor.Contour> contours = vision.getContours();
            VisionProcessor.Contour contour;
            contour = Collections.max(contours, new VisionProcessor.WidthComp());
            final double centerX = contour.getCenterX();
            final double delta = Math.abs(centerX - vision.getHorizontalCenter());
            LOGGER.debug("untilCentered centerY=" + centerX + " delta=" + delta);
            return delta < marginOfError;
        }
        catch (Exception e)
        {
            LOGGER.info("Missed contour: " + e);
            return false;
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
        return false;
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
            autonomous = new Autonomous(
                    Claw.getInstance(),
                    Lifter.getInstance(), VisionProcessor.getInstance());
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
     */
    private Autonomous(Claw claw, Lifter lifter, VisionProcessor vision)
    {
        // TODO Change drive, claw, and lifter to generics implementing respective interfaces
        this.claw = claw;
        this.lifter = lifter;
        this.vision = vision;
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
            default:
                initStayInPlace();
                break;
        }

        LOGGER.info("Beginning action: " + actions.get(0).getDescription());
    }

//    private void initGrabCan()
//    {
//        // Start facing the wall in front of an item (container or tote), pick it up
//        // and carry it rolling backwards to the auto zone.
//        Gyro2015.getInstance().reset(GyroResetDirection.FACE_TOWARD);// reset to upfield
//        addAction("Close claw to grip container or tote",
//                () -> claw.isClosed(),
//                () -> {
//                    lifter.stop();
//                    claw.moveClaw(ClawMoveDirection.CLOSE, false);
//                    drive.stop();
//                });
//        addAction("Lift container", 
//                () -> forDurationSecs(0.5f),
//                () -> {
//                    lifter.driveLifter(LifterDirection.UP, Speed.FAST);
//                    claw.stop();
//                    drive.stop();
//                });
//        addAction("Lift container and drive backwards", 
//                () -> forDurationSecs(1.25f),
//                () -> {
//                    lifter.driveLifter(LifterDirection.UP, Speed.FAST);
//                    claw.stop();
//                    drive.arcadeDrive(null, false);
//                });
//        addAction("Drive backwards", 
//                () -> forDurationSecs(1.75f),
//                () -> {
//                    lifter.stop();
//                    claw.stop();
//                    drive.arcadeDrive(null, false);
//                });
//        addAction("Turn in place", 
//                () -> forDurationSecs(0.7f),
//                () -> {
//                    lifter.stop();
//                    claw.stop();
//                    drive.turnDrive(0.6);
//                });
//        addAction("Done",
//                () -> forever(), 
//                () -> {
//                    lifter.stop();
//                    claw.stop();
//                    drive.stop();
//                });
//    }

    private void initStayInPlace()
    {
        // Stay in place. Reset to upfield.
        Gyro2015.getInstance().reset();
        addAction("Stop driving", 
                () -> forever(), 
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.stop();
                });
    }
    
    private void initAim()
    {
        final int marginOfError = 30;
        
        addAction("Rotate until square with widest is centered",
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
        
        final double minTurnSpeed = 0.20;
        final double maxTurnSpeed = 0.26;
        final double turnSpeedRange = maxTurnSpeed - minTurnSpeed;
        final double horizontalCenter = vision.getHorizontalCenter();
        
        LOGGER.debug("start seekWidestContour()");
        List<VisionProcessor.Contour> contours = vision.getContours();
        LOGGER.debug("found contours");
        
        if (contours.size() == 0)
        {
            // Still haven't found what I'm looking for
            drive.stop();
            return false;
        }
        LOGGER.debug("Has contours");
        // Find the widest contour
        VisionProcessor.Contour widest = Collections.max(contours, new VisionProcessor.WidthComp());
        LOGGER.debug("Found widest contour");
        final double centerX = widest.getCenterX();
        final double delta = Math.abs(centerX - horizontalCenter);
        if (delta < marginOfError)
        {
            // Found target
            return true;
        }
//        if (widest.getCenterX() - vision.getHorizontalCenter()) 
        int direction = widest.getCenterX() > horizontalCenter ? -1 : 1;
        final double turnSpeed = direction * (minTurnSpeed + turnSpeedRange * (delta/horizontalCenter));
        LOGGER.info("Calculated direction, turnSpeed=" + turnSpeed);
        drive.turnDrive(turnSpeed);
        LOGGER.debug("Turned");
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
        NO_AUTO, AIM
    }
}
