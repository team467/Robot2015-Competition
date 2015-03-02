package org.usfirst.frc.team467.robot;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class Autonomous
{
    private static final Logger LOGGER = Logger.getLogger(Autonomous.class);

    private static Autonomous autonomous = null;

    private Drive drive = null;
    private Claw claw = null;
    private Lifter lifter = null;

    long actionStartTimeMS = -1;

    private interface DoneCondition
    {
        boolean call();
    }

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
            try
            {
                return doneCondition.call();
            }
            catch (Exception e)
            {
                LOGGER.error("Done condition threw exception, assuming done: " + e.getMessage());
                return true;
            }
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
        if (actionStartTimeMS < 0)
        {
            actionStartTimeMS = System.currentTimeMillis();
        }

        final long now = System.currentTimeMillis();
        final long durationMS = (long) (1000 * durationSecs);
        return now < actionStartTimeMS + durationMS;
    }

    // Always returns false, indicates is never done.
    private boolean forever()
    {
        // Never done.
        return false;
    }

    // Always returns true, will claim done first time called.
    private boolean once()
    {
        // Always done.
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
            autonomous = new Autonomous();
        }
        return autonomous;
    }

    /**
     * Private constructor to setup the Autonomous
     */
    private Autonomous()
    {
        drive = Drive.getInstance();
        claw = Claw.getInstance();
        lifter = Lifter.getInstance();
    }

    /**
     * Sets up the periodic function
     */
    public void initAutonomous()
    {
        AutoType autonomousType = DriverStation2015.getInstance().getAutoType();
        LOGGER.info("AUTO MODE " + autonomousType);

        // Set up gyro and create actions list.
        switch (autonomousType)
        {
            case GRAB_CAN:
                initGrabCan();
                break;
            case DRIVE_ONLY:
                initDriveOnly();
                break;
            case HOOK_AND_PUSH:
                initHookAndPush();
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
                () -> !claw.isClosed(),
                () -> claw.moveClaw(ClawMoveDirection.CLOSE, false));
        addAction("Lift container or tote", 
                () -> forDurationSecs(2.0f),
                () -> lifter.driveLifter(LifterDirection.UP, false));
        addAction("Drive backwards", 
                () -> forDurationSecs(3.0f), 
                () -> {
                    drive.crabDrive(Math.PI, 0.4, false);
                    lifter.driveLifter(LifterDirection.STOP, false);
                });
        addAction("Turn in place", 
                () -> forDurationSecs(1.1f), 
                () -> {
                    drive.turnDrive(0.5);
                    lifter.driveLifter(LifterDirection.STOP, false);
                });
        addAction("Stop lifting and driving", 
                () -> once(), 
                () -> {
                    lifter.driveLifter(LifterDirection.STOP, false);
                    drive.noDrive();
                });
        addAction("Stop driving", 
                () -> forever(), 
                () -> drive.noDrive());
    }

    private void initDriveOnly()
    {
        Gyro2015.getInstance().reset(GyroResetDirection.FACE_LEFT);// reset to upfield
        // Drive to auto zone. Starts on the very edge and just creeps into the zone
        addAction("Drive into auto zone", 
                () -> forDurationSecs(2.0f), 
                () -> drive.crabDrive(Math.PI / 2, 0.5, false));
        addAction("Stop driving", 
                () -> forever(), 
                () -> drive.noDrive());
    }

    private void initHookAndPush()
    {
        // Starts facing left, reset to upfield.
        Gyro2015.getInstance().reset(GyroResetDirection.FACE_LEFT);
        addAction("Raise lifter up and turn wheels sideways", 
                () -> forDurationSecs(2.0f), 
                () -> {
                    lifter.driveLifter(LifterDirection.UP, false);
                    drive.crabDrive(Math.PI / 2, 0, false);
                });
        addAction("Stop lifting and drive sideeways", 
                () -> forDurationSecs(4.0f), 
                () -> {
                    lifter.driveLifter(LifterDirection.STOP, false);
                    drive.crabDrive(Math.PI / 2, 0.5, false);
                });
        addAction("Lower lifter and stop driving", 
                () -> forDurationSecs(1.5f), 
                () -> {
                    lifter.driveLifter(LifterDirection.DOWN, false);
                    drive.crabDrive(Math.PI / 2, 0, false);
                });
        addAction("Stop lifter", 
                () -> once(), 
                () -> lifter.driveLifter(LifterDirection.STOP, false));
        addAction("Stop driving", 
                () -> forever(), 
                () -> drive.crabDrive(Math.PI / 2, 0, false));
    }

    private void initStayInPlace()
    {
        // Stay in place. Reset to upfield.
        Gyro2015.getInstance().reset();
        addAction("Stop driving", 
                () -> forever(), 
                () -> drive.noDrive());
    }

    /**
     * Updates the Autonomous routine. Called by Robot.autonomousPeriodic().
     */
    public void updateAutonomousPeriodic()
    {
        // Make sure there something to do.
        if (actions.isEmpty())
        {
            return;
        }

        Activity currentAction = actions.get(0);
        if (currentAction.isDone())
        {
            // This action is done. Remove it from the list and prepare the next one.
            LOGGER.info("Finished action: " + currentAction.getDescription());
            actions.remove(0);
            if (actions.isEmpty())
            {
                // Ran out of actions. We're done.
                return;
            }

            // Move on to the next action.
            currentAction = actions.get(0);
            LOGGER.info("Beginning action: " + currentAction.getDescription());
        }

        currentAction.doIt();
    }

    /**
     * Type of Autonomous drive to use.
     * 
     * @author kyle
     *
     */
    enum AutoType
    {
        NO_AUTO, DRIVE_ONLY, GRAB_CAN, HOOK_AND_PUSH
    }
}
