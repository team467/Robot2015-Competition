package org.usfirst.frc.team467.robot;

import java.util.LinkedList;
import java.util.List;

import org.usfirst.frc.team467.robot.DriverStation2015.Speed;

import org.apache.log4j.Logger;

public class Autonomous
{
    private static final Logger LOGGER = Logger.getLogger(Autonomous.class);

    private static Autonomous autonomous = null;

    private Drive drive = null;
    private Claw claw = null;
    private Lifter lifter = null;

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
                    Drive.getInstance(),
                    Claw.getInstance(),
                    Lifter.getInstance());
        }
        return autonomous;
    }

    /**
     * Private constructor to setup the Autonomous
     */
    public Autonomous(Drive drive, Claw claw, Lifter lifter)
    {
        // TODO Change drive, claw, and lifter to generics implementing respective interfaces
        this.drive = drive;
        this.claw = claw;
        this.lifter = lifter;
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
            default:
                initStayInPlace();
                break;
        }

        LOGGER.info("Beginning action: " + actions.get(0).getDescription());
    }

    private void initGrabCan()
    {
        // Start facing the wall in front of an item (container or tote), pick it up
        // and carry it rolling backwards to the auto zone.
        Gyro2015.getInstance().reset(GyroResetDirection.FACE_TOWARD);// reset to upfield
        addAction("Close claw to grip container or tote",
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
                    drive.crabDrive(Math.PI, 0.6, false);
                });
        addAction("Drive backwards", 
                () -> forDurationSecs(1.75f),
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.crabDrive(Math.PI, 0.6, false);
                });
        addAction("Turn in place", 
                () -> forDurationSecs(0.7f),
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.turnDrive(0.6);
                });
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
        Gyro2015.getInstance().reset(GyroResetDirection.FACE_LEFT);// reset to upfield
        // Drive to auto zone. Starts on the very edge and just creeps into the zone
        addAction("Drive into auto zone", 
                () -> forDurationSecs(2.0f), 
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.crabDrive(Math.PI / 2, 0.5, false);
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
        Gyro2015.getInstance().reset(GyroResetDirection.FACE_LEFT);
        addAction("Raise lifter up and turn wheels sideways", 
                () -> forDurationSecs(2.0f), 
                () -> {
                    lifter.driveLifter(LifterDirection.UP, Speed.FAST);
                    claw.stop();
                    drive.crabDrive(Math.PI / 2, 0, false);
                });
        addAction("Stop lifting and drive sideways", 
                () -> forDurationSecs(sidewaysSecs),
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.crabDrive(Math.PI / 2, 0.5, false);
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
        Gyro2015.getInstance().reset();
        addAction("Stop driving", 
                () -> forever(), 
                () -> {
                    lifter.stop();
                    claw.stop();
                    drive.stop();
                });
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
        NO_AUTO, DRIVE_ONLY, GRAB_CAN, HOOK_AND_PUSH_OVER_RAMP, HOOK_AND_PUSH
    }
}
