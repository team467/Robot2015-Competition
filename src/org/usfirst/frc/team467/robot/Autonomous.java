package org.usfirst.frc.team467.robot;

public class Autonomous
{

    private static Autonomous autonomous = null;

    private Drive drive = null;
    private Claw claw = null;
    private Lifter lifter = null;

    private AutoType autonomousType = AutoType.NO_AUTO;

    long autonomousStartTime = -1;

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
     * Sets what autonomous routine to run. Please do not call this after
     * updateAutonomousPeriodic() has been run.
     * 
     * @param autoType
     */
    public void setAutoType(AutoType autoType)
    {
        this.autonomousType = autoType;
    }

    /**
     * Sets up the periodic function
     */
    public void initAutonomous()
    {
        autonomousStartTime = -1;
        autonomousType = DriverStation2015.getInstance().getAutoType();
        switch (autonomousType)
        {
            case GRAB_CAN:
                // starts facing the wall
                Gyro2015.getInstance().reset(GyroResetDirection.FACE_TOWARD);// reset to upfield
                break;
            case DRIVE_ONLY:
                Gyro2015.getInstance().reset(GyroResetDirection.FACE_LEFT);// reset to upfield
                break;
            case HOOK_AND_PUSH:
                // starts facing left
                Gyro2015.getInstance().reset(GyroResetDirection.FACE_LEFT);// reset to upfield
                break;
            default:
                Gyro2015.getInstance().reset();// reset to upfield
                break;
        }
        System.out.println("AUTO MODE " + autonomousType.toString());
    }

    /**
     * Updates the Autonomous routine.
     */
    public void updateAutonomousPeriodic()
    {
        if (autonomousStartTime < 0)
        {
            autonomousStartTime = System.currentTimeMillis();
        }

        // in milliseconds
        long timeSinceStart = System.currentTimeMillis() - autonomousStartTime;

        //
        // Possible Auto Types:
        //
        // Drive to AUTO zone,
        // Grab box and drive to AUTO zone
        // Grab container and drive to AUTO zone
        // Grab box and grab container and drive to AUTO zone
        switch (this.autonomousType)
        {
            case NO_AUTO:
                noAuto();
                break;

            case DRIVE_ONLY:
                driveOnly(timeSinceStart);
                break;

            case GRAB_CAN:
                grabCan(timeSinceStart);
                break;

            case HOOK_AND_PUSH:
                hookAndPush(timeSinceStart);
                break;
        }
    }

    public void noAuto()
    {
        drive.noDrive();
    }

    long gripTimeElapsed = 0;

    private void grabCan(long timeSinceStart)
    {
        // Start behind an item (container or tote), and pick it up
        // and carry it to the auto zone
        
        if (!claw.isClosed())
        {
            // close motor around box
            claw.moveClaw(ClawMoveDirection.CLOSE, false);
            gripTimeElapsed = timeSinceStart;
        }
        else if (timeSinceStart < 2000 + gripTimeElapsed)
        {
            lifter.driveLifter(LifterDirection.UP, false);
        }
        else if (timeSinceStart < 5000 + gripTimeElapsed) // in milliseconds
        {
            drive.crabDrive(0, // angle to drive at in radians
                    -0.4,      // speed to drive at in percent
                    false);    // no field align
            lifter.driveLifter(LifterDirection.STOP, false);
        }
        else if (timeSinceStart < 6100 + gripTimeElapsed)
        {
            drive.turnDrive(0.5);
            lifter.driveLifter(LifterDirection.STOP, false);
        }
        else
        {
            drive.crabDrive(0, // angle to drive at in radians
                    0,         // speed to drive at in percent
                    false);    // no field align
            lifter.driveLifter(LifterDirection.STOP, false);
        }
    }

    private void driveOnly(long timeSinceStart)
    {
        // Drive to auto zone. Starts on the very edge and
        // just creeps into the zone
        if (timeSinceStart < 2000) // in milliseconds
        {
            drive.crabDrive(Math.PI / 2, // angle to drive at in radians
                    0.5,       // speed to drive at in percent
                    false);    // no field align
        }
        else
        {
            drive.noDrive();

        }
    }

    private void hookAndPush(long timeSinceStart)
    {
        if (timeSinceStart < 2000)
        {
            lifter.driveLifter(LifterDirection.UP,false);
            drive.crabDrive(Math.PI / 2, 0, false);
        }
        else if (timeSinceStart < 6500)
        {
            lifter.driveLifter(LifterDirection.STOP, false);
            drive.crabDrive(Math.PI / 2, 0.5, false);
        }
        else if (timeSinceStart < 8000)
        {
            lifter.driveLifter(LifterDirection.DOWN, false);
            drive.crabDrive(Math.PI / 2, 0, false);
        }
        else
        {
            lifter.driveLifter(LifterDirection.STOP, false);
            drive.crabDrive(Math.PI / 2, 0, false);
        }
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
