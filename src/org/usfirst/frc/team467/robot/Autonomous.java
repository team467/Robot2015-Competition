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

    long autonomousStartTime = -999999;

    /**
     * Gets a singleton instance of the Autonomous
     * 
     * @return Autonomous object
     */
    public static Autonomous getInstance()
    {
        if (autonomous == null)
            autonomous = new Autonomous();
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
        autonomousStartTime = -999999;
        autonomousType = DriverStation2015.getInstance().getAutoType();
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
            case DRIVE_ONLY:
                driveOnly(timeSinceStart);
                break;
            case GRAB_CAN:
                grabCan(timeSinceStart);
                break;
            case PUSH_TOTE:
                pushTote(timeSinceStart);
                break;            
            case TEST:
                test(timeSinceStart);
            case GRAB_AND_PUSH:
                grabAndPush(timeSinceStart);
                break;
            case GRAB_AND_PUSH_VAR:
                grabAndPushVariation(timeSinceStart);
                break;
            case PUSH_BOTH:
                pushBoth(timeSinceStart);
                break;
            case NO_AUTO:
                noAuto();
                break;
        }
    }

    public void noAuto()
    {
        drive.noDrive();
    }
    
    long gripTimeElapsed = 0;
    long ungripTimeElapsed = 0;
    long lifterTimeElapsed = 0;

    public void test(long timeSinceStart)
    {
        if (timeSinceStart < 1000)
        {
            drive.crabDrive(0, 0.5, false);
        }
        else if (timeSinceStart < 2000)
        {
            drive.crabDrive(Math.PI / 2, 0.5, false);
        }
        else if (timeSinceStart < 3000)
        {
            drive.crabDrive(Math.PI, 0.5, false);
        }
        else if (timeSinceStart < 4000)
        {
            drive.crabDrive(-Math.PI / 2, 0.5, false);
        }
        else
        {
            drive.crabDrive(0, 0, false);
        }
    }

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
        else if (timeSinceStart < 3500 + gripTimeElapsed) // in milleseconds
        {
            drive.crabDrive(Math.PI, // angle to drive at in radians
                    -0.4,  // speed to drive at in percent
                    false); // no field align
        }
        else
        {
            drive.crabDrive(0, // angle to drive at in radians
                    0,  // speed to drive at in percent
                    false); // no field align
        }
    }

    private void pushTote(long timeSinceStart)
    {
        // Pushes tote into the AUTO zone as we drive ourselves
        // into the auto zone
        if (timeSinceStart < 3000) // in milleseconds
        {
            drive.crabDrive(3*Math.PI/2, // angle to drive at in radians
                    0.5,  // speed to drive at in percent
                    false); // no field align
        }
        else
        {
            drive.crabDrive(0, // angle to drive at in radians
                    0,  // speed to drive at in percent
                    false); // no field align
        }
    }

    private void driveOnly(long timeSinceStart)
    {
        // Drive to auto zone. Starts on the very edge and
        // just creeps into the zone
        if (timeSinceStart < 2000) // in milleseconds
        {
            drive.crabDrive(0, // angle to drive at in radians
                    0.5,  // speed to drive at in percent
                    false); // no field align
        }
        else
        {
            drive.crabDrive(0, // angle to drive at in radians
                    0,  // speed to drive at in percent
                    false); // no field align

        }

    }
    
    private void grabAndPush(long timeSinceStart)
    {
        if (timeSinceStart < 750)
        {
            drive.crabDrive(Math.PI/2, 0.5, false);
            //move right (one second, half speed) to bash past tote and position for lift
        }
        else if (timeSinceStart < 2000)
        {
            lifter.setLift(LiftTypes.LIFT_DOWN_SLOW);
            //move lift/arms around bin (one seconds)
        }
        else if (!claw.isClosed())
        {
            lifter.setLift(LiftTypes.NO_LIFT);
            claw.moveClaw(ClawMoveTypes.GRIP_SLOW);
            gripTimeElapsed = System.currentTimeMillis() - timeSinceStart;
            //stops lift, hugs bin
        }
        else if (timeSinceStart < 2000 + gripTimeElapsed)
        {
            lifter.setLift(LiftTypes.LIFT_UP_SLOW);
            //lift bin upwards (one second)
        }
        else if (timeSinceStart < 3000 + gripTimeElapsed)
        {
            lifter.setLift(LiftTypes.NO_LIFT);
            drive.crabDrive(Math.PI / 2, 0.5, false);
        }
        else
        {
            lifter.setLift(LiftTypes.NO_LIFT);
            drive.crabDrive(0, 0, false);
        }
    }

    private void pushBoth(long timeSinceStart)
    {
        if (timeSinceStart < 3000)
        {
            drive.autoDrive(Direction.RIGHT);
        }
    }

    private void grabAndPushVariation(long timeSinceStart)
    {
        //strafe right
        //drive forward
        //grip bin
        //lift bin
        //strafe right
        if (timeSinceStart < 750)
        {
            drive.crabDrive(Math.PI/2, 0.5, false);
        }
        else if (timeSinceStart < 1750)
        {
            drive.crabDrive(0, 0.5, false);
        }
        else if (!claw.isClosed())
        {
            claw.moveClaw(ClawMoveTypes.GRIP_SLOW);
            gripTimeElapsed = System.currentTimeMillis() - timeSinceStart;
        }
        else if (timeSinceStart < gripTimeElapsed + 2000)
        {
            lifter.setLift(LiftTypes.LIFT_UP_SLOW);
        }
        else if (timeSinceStart < gripTimeElapsed + 4000)
        {
            drive.crabDrive(Math.PI/2, 0.5, false);
        }
        else
        {
            drive.crabDrive(0, 0, false);
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
        NO_AUTO, PUSH_BOTH, GRAB_AND_PUSH, GRAB_AND_PUSH_VAR, TEST, DRIVE_ONLY, GRAB_CAN, PUSH_TOTE
    }
}
