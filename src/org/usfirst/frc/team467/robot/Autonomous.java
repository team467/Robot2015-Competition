package org.usfirst.frc.team467.robot;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.usfirst.frc.team467.robot.DriverStation2015.Speed;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.interfaces.Gyro;

import org.apache.log4j.Logger;

public class Autonomous
{
    private static final Logger LOGGER = Logger.getLogger(Autonomous.class);

    private static Autonomous autonomous = null;

    Gyro2016 gyro = null;
    private AnalogGyro Agyro = null;
    
    private Driveable drive = null;
    private Claw claw = null;
    private Lifter lifter = null;
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
            return whileCondition.call();
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
            autonomous = new Autonomous(
                    Claw.getInstance(),
                    Lifter.getInstance(), VisionProcessor.getInstance(), Gyro2016.getInstance());
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
    private Autonomous(Claw claw, Lifter lifter, VisionProcessor vision, Gyro2016 gyro)
    {
        // TODO Change drive, claw, and lifter to generics implementing respective interfaces
        this.claw = claw;
        this.lifter = lifter;
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
            case GRAB_CAN:
                initGrabCan();
                break;
            case DRIVE_ONLY:
                initDriveOnly();
                break;
            case HOOK_AND_PUSH_OVER_RAMP:
                initHookAndPush(4.5f);
                break;
            case HOOK_AND_PUSH:
                initHookAndPush(3.75f);
                break;
            case AIM:
                initAim();
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
        
        Gyro2016.getInstance();//.reset(GyroResetDirection.FACE_TOWARD);// reset to upfield
        addAction("Move while flat",
                () -> gyro.isFlat(),
                () -> {
                    drive.arcadeDrive(0, 0.5, false);
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                });
        addAction("Move forward until gyro is 7 degrees",
                () -> gyro.up(),
                () -> {
                    drive.arcadeDrive(0, 0.5, false);
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                });
        addAction("Move",
                () -> forDurationSecs(0.25f),
                () -> {
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                    drive.arcadeDrive(0, 0.5, false);
                });
        addAction("Do nothing",
                () -> forDurationSecs(0.1f),
                () ->{
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                });
        addAction("Move foward until gyro is 0 degrees",
                () -> gyro.down(),
                () ->{
                    drive.arcadeDrive(0, 0.5, false);
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                });
        addAction("Move",
                () -> forDurationSecs(0.25f),
                () -> {
                    drive.arcadeDrive(0, 0.5, false);
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                });
        addAction("Stop moving",
                () -> gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + Agyro.getAngle());
                    drive.stop();
                });
/*        addAction("Close claw to grip container or tote",
                () -> claw.isClosed(),
                () -> {
                    lifter.stop();
                    claw.moveClaw(ClawMoveDirection.CLOSE, false);
                    drive.stop();
                });
        addAction("Lift container", 
                () -> forDurationSecs(0.5f),
                () -> {
                    lifter.driveLifter(LifterDirection.UP, Speed.FAST);
                    claw.stop();
                    drive.stop();
                });
        addAction("Lift container and drive backwards", 
                () -> forDurationSecs(1.25f),
                () -> {
                    lifter.driveLifter(LifterDirection.UP, Speed.FAST);
                    claw.stop();
                    drive.arcadeDrive(Math.PI, 0.6, false);
                });
        addAction("Drive backwards", 
                () -> forDurationSecs(1.75f),
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.arcadeDrive(Math.PI, 0.6, false);
                });
        addAction("Turn in place", 
                () -> forDurationSecs(0.7f),
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.turnDrive(0.6);
                });*/
        addAction("Done",
                () -> forever(), 
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.stop();
                });
    }

    private void initDriveOnly()
    {
//        Gyro2015.getInstance().reset(GyroResetDirection.FACE_LEFT);// reset to upfield
        // Drive to auto zone. Starts on the very edge and just creeps into the zone
        Gyro2016.getInstance();
        addAction("Drive into auto zone", 
                () -> forDurationSecs(2.0f), 
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.arcadeDrive(Math.PI / 2, 0.5, false);
                });
        addAction("Stop driving", 
                () -> forever(), 
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.stop();
                });
    }
    
    /**
     * @param sidewaysSecs - time for driving sideways
     */
    private void initHookAndPush(float sidewaysSecs)
    {
        // Starts facing left, reset to upfield.
//        Gyro2015.getInstance().reset(GyroResetDirection.FACE_LEFT);
        Gyro2016.getInstance();
        addAction("Raise lifter up and turn wheels sideways", 
                () -> forDurationSecs(2.0f), 
                () -> {
                    lifter.driveLifter(LifterDirection.UP, Speed.FAST);
                    claw.stop();
                    drive.arcadeDrive(Math.PI / 2, 0, false);
                });
        addAction("Stop lifting and drive sideways", 
                () -> forDurationSecs(sidewaysSecs),
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.arcadeDrive(Math.PI / 2, 0.5, false);
                });
        addAction("Lower lifter and stop driving", 
                () -> forDurationSecs(0.5f), 
                () -> {
                    lifter.driveLifter(LifterDirection.DOWN, Speed.SLOW);
                    claw.stop();
                    drive.stop();
                });
        addAction("Done",
                () -> forever(), 
                () -> {
                    lifter.stop();
                    claw.stop();
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
        NO_AUTO, DRIVE_ONLY, GRAB_CAN, HOOK_AND_PUSH_OVER_RAMP, HOOK_AND_PUSH, AIM
    }
}
