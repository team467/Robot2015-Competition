package org.usfirst.frc.team467.robot;

//import java.util.Comparator;
//import java.util.Vector;
//
//import org.apache.log4j.Logger;
//
//import com.ni.vision.NIVision;
//import com.ni.vision.NIVision.Image;
//import com.ni.vision.NIVision.ImageType;
//
//import edu.wpi.first.wpilibj.CameraServer;
//import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

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
            claw.moveClaw(ClawMoveTypes.GRIP_SLOW);
            gripTimeElapsed = System.currentTimeMillis() - autonomousStartTime;
        }
        else if (timeSinceStart < 500 + gripTimeElapsed)
        {
            lifter.setLift(LiftTypes.LIFT_UP_SLOW);
        }
        else if (timeSinceStart < 3500 + gripTimeElapsed) // in milliseconds
        {
            drive.crabDrive(0, // angle to drive at in radians
                    -0.4,      // speed to drive at in percent
                    false);    // no field align
        }
        else
        {
            drive.crabDrive(0, // angle to drive at in radians
                    0,         // speed to drive at in percent
                    false);    // no field align
        }
    }

    private void driveOnly(long timeSinceStart)
    {
        // Drive to auto zone. Starts on the very edge and
        // just creeps into the zone
        if (timeSinceStart < 2000) // in milliseconds
        {
            drive.crabDrive(0, // angle to drive at in radians
                    0.5,       // speed to drive at in percent
                    false);    // no field align
        }
        else
        {
            drive.crabDrive(0, // angle to drive at in radians
                    0,         // speed to drive at in percent
                    false);    // no field align

        }
    }

    private void hookAndPush(long timeSinceStart)
    {
        if (timeSinceStart < 2000)
        {
            lifter.setLift(LiftTypes.LIFT_UP_SLOW);
            drive.crabDrive(Math.PI / 2, 0, false);
        }
        else if (timeSinceStart < 5750)
        {
            lifter.setLift(LiftTypes.NO_LIFT);
            drive.crabDrive(Math.PI / 2, 0.5, false);
        }
        else if (timeSinceStart < 7250)
        {
            lifter.setLift(LiftTypes.LIFT_DOWN_SLOW);
            drive.crabDrive(Math.PI / 2, 0, false);
        }
        else
        {
            lifter.setLift(LiftTypes.NO_LIFT);
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
