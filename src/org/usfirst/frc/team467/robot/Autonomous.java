package org.usfirst.frc.team467.robot;

import java.util.LinkedList;
import java.util.List;

import org.usfirst.frc.team467.robot.BallRollers.ManipIntent;
import org.usfirst.frc.team467.robot.BallRollers.RollerDirection;
import org.usfirst.frc.team467.robot.DriverStation2016.Speed;
import org.usfirst.frc.team467.robot.TBar.tBarDirection;

import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.apache.log4j.Logger;

public class Autonomous
{
    private static final Logger LOGGER = Logger.getLogger(Autonomous.class);

    private static Autonomous autonomous = null;

    private Gyro2016 gyro = null;    
    private Driveable drive = null;
    private Ultrasonic2016 ultrasonic = null;
    
    private TBar tbar = null;

    private BallRollers roller = null;
    private Shooter467 shooter = null;
    
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
        return !shooter.aim(marginOfError);
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
            autonomous = new Autonomous(Gyro2016.getInstance());
        }
        return autonomous;
    }
    
    public void setDrive(Driveable drive)
    {
        this.drive = drive;
    }

    public void setUltrasonic(Ultrasonic2016 ultra)
    {
        this.ultrasonic = ultra;
    }
    
    public void setRoller(BallRollers roller)
    {
        this.roller = roller;
    }
    
    public void setShooter(Shooter467 shooter)
    {
        this.shooter = shooter;
    }
    
    public boolean shouldTurnRight(double angle){
        return (gyro.getYawAngle() < angle);
    }
    
    public boolean shouldTurnLeft(double angle){
        return (gyro.getYawAngle() > angle);
    }

    /**
     * Private constructor to setup the Autonomous
     * @param gyro2 
     */
    private Autonomous(Gyro2016 gyro)
    {
        // TODO Change drive, claw, and lifter to generics implementing respective interfaces
        this.gyro = gyro;
    }

    /**
     * Sets up the periodic function
     */
    public void initAutonomous()
    {
//        AutoType autonomousType = DriverStation2015.getInstance().getAutoType();
        AutoType autonomousType = AutoType.STAY_IN_PLACE;
        LOGGER.info("AUTO MODE " + autonomousType);

        // Reset actions.
        actions.clear();
        resetActionStartTime();
        
        // Set up gyro and create actions list.
        switch (autonomousType)
        {
            case CROSS_BARRIER:
                initCrossBarrier();
                break;
            case SALLY_PORT:
                initSallyPort();
                break;
            case NEW_PORTCULLIS:
                initNewPortcullis();
                break;
            case PORTCULLIS:
                initPortcullis();
                break;
            case DRAWBRIDGE:
                initDrawBridge();
                break;
            case CHEVAL_DE_FRISE:
                initChevalDeFrise();
                break;
            case DRIVE_ONLY:
                initDriveOnly();
                break;
            case AIM:
                initAim();
                break;
            case HIGH_GOAL:
                initHighGoal();
                break;
            case STAY_IN_PLACE:
                initStayInPlace();
                break;
            default:
                initStayInPlace();
                break;
        }

        LOGGER.info("Beginning action: " + actions.get(0).getDescription());
    }
    
    private void initCrossBarrier()
    {
        //Moves over barriers using the tilt gyrometer to detect when the robot is going up, down, or driving flat
        //Moves towards the walls using the ultrasonic sensor, and turns (counter)clockwise with the yaw gyrometer to line up for the low shoot
        addAction("Move while gyro is up",
                () -> gyro.isUp() || gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                    drive.arcadeDrive(0.0, -0.7);
                });
        addAction("Move while gyro is down",
                () -> gyro.isDown(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, -0.7);
                });
        if (!gyro.isFlat()){
            addAction("go fowards while not flat and for 1 second",
                    () -> !gyro.isFlat() && forDurationSecs(1.0f),
                    () -> {
                        drive.arcadeDrive(0.0, -0.5);
                    });
        }
        if (shouldTurnLeft(10)){
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnLeft(10),
                    () -> {
                        drive.turnDrive(0.4);
                    });
        }else{
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnRight(-10),
                    () -> {
                        drive.turnDrive(-0.4);
                    });
        }
        addAction("Move while the Ultrasonic is more than 5 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (clockwise)",
                () -> shouldTurnRight(80), 
                () -> {
                    drive.turnDrive(-0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 5 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (counterclockwise)",
                () -> shouldTurnLeft(10), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 3 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 36,
                () -> {
                    drive.arcadeDrive(0.0, -0.3);
                });
        addAction("Turn (counterclockwise)",
                () -> shouldTurnLeft(-40), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction ("Drive fowards while robot is not up",
                () -> !gyro.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Shoot the low goal",
                () -> gyro.getYawAngle() <= -40 && gyro.getTiltAngle() > 4.0,
                () -> {
                    drive.stop();
                    roller.runRoller(RollerDirection.OUT);
                });
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }
    
    private void initSallyPort(){
        addAction("turns backwards",
                () -> shouldTurnLeft(170),
                () -> {
                    drive.turnDrive(0.5);
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                    tbar.launchTBar(tBarDirection.UP);
                });
//        addAction("Move while gyro is flat, up, or down",
//                () -> (gyro.isFlat() || gyro.isUp() || gyro.isDown()) && ultrasonic.getBackRangeInches() > 12,
//                () -> {
//                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
//                    drive.arcadeDrive(0.0, 0.7);
//                });
//        addAction("Move TBar arm down while the robot is 12 inches from the port",
//                () -> ultrasonic.getBackRangeInches() <= 12,
//                () -> {
//                    drive.stop();
//                    tbar.launchTBar(tBarDirection.DOWN);
//                });
//        addAction("Move 3 feet away from the port and keep the bar down",
//                () -> ultrasonic.getBackRangeInches() < 36,
//                () -> {
//                    drive.arcadeDrive(0.0, -0.4);
//                    tbar.launchTBar(tBarDirection.DOWN);
//                });
        addAction("Turn to the left to open the door",
                () -> shouldTurnLeft(-60),
                () -> {
                    drive.turnDrive(0.4);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("Move backwards",
                () -> forDurationSecs(1.0f),
                () -> {
                    drive.arcadeDrive(0.0, 0.35);
                });
        addAction("Turn right to straighten up",
                () -> shouldTurnRight(-10),
                () -> {
                    drive.turnDrive(-0.4);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("Move while gyro is flat, up, or down",
                () -> ( gyro.isFlat() || gyro.isUp() || gyro.isDown() ) && forDurationSecs(1.5f),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                });
        if (!gyro.isFlat()){
            addAction("go fowards while not flat and for 1 second",
                    () -> !gyro.isFlat() && forDurationSecs(1.0f),
                    () -> {
                        drive.arcadeDrive(0.0, 0.5);
                    });
        }
        if (shouldTurnLeft(10)){
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnLeft(10),
                    () -> {
                        drive.turnDrive(0.4);
                    });
        }else{
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnRight(-10),
                    () -> {
                        drive.turnDrive(-0.4);
                    });
        }
        addAction("Move while the Ultrasonic is more than 5 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (clockwise)",
                () -> shouldTurnRight(80), 
                () -> {
                    drive.turnDrive(-0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 5 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (counterclockwise)",
                () -> shouldTurnLeft(10), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 3 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 36,
                () -> {
                    drive.arcadeDrive(0.0, -0.3);
                });
        addAction("Turn (counterclockwise)",
                () -> shouldTurnLeft(-40), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction ("Drive fowards while robot is not up",
                () -> !gyro.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Shoot the low goal",
                () -> gyro.getYawAngle() <= -40 && gyro.getTiltAngle() > 4.0,
                () -> {
                    drive.stop();
                    roller.runRoller(RollerDirection.OUT);
                });
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }
    
    private void initNewPortcullis(){
        addAction("turns backwards",
                () -> shouldTurnLeft(170),
                () -> {
                    drive.turnDrive(0.5);
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("Move while gyro is down or flat",
                () -> gyro.isDown() || gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    tbar.launchTBar(tBarDirection.DOWN);
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                    drive.arcadeDrive(0.0, 0.7);
                });
        addAction("Move while gyro is up",
                () -> gyro.isUp(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                });
        if (!gyro.isFlat()){
            addAction("go fowards while not flat and for 1 second",
                    () -> !gyro.isFlat() && forDurationSecs(1.0f),
                    () -> {
                        drive.arcadeDrive(0.0, 0.5);
                    });
        }
        if (shouldTurnLeft(10)){
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnLeft(10),
                    () -> {
                        drive.turnDrive(0.4);
                    });
        }else{
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnRight(-10),
                    () -> {
                        drive.turnDrive(-0.4);
                    });
        }
        addAction("Move while the Ultrasonic is more than 5 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (clockwise)",
                () -> shouldTurnRight(80), 
                () -> {
                    drive.turnDrive(-0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 5 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (counterclockwise)",
                () -> shouldTurnLeft(10), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 3 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 36,
                () -> {
                    drive.arcadeDrive(0.0, -0.3);
                });
        addAction("Turn (counterclockwise)",
                () -> shouldTurnLeft(-40), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction ("Drive fowards while robot is not up",
                () -> !gyro.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Shoot the low goal",
                () -> gyro.getYawAngle() <= -40 && gyro.getTiltAngle() > 4.0,
                () -> {
                    drive.stop();
                    roller.runRoller(RollerDirection.OUT);
                });
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                }); 
    }
    
    private void initPortcullis()
    {
        // Start facing the wall in front of an item (container or tote), pick it up
        // and carry it rolling backwards to the auto zone.
        //.reset(GyroResetDirection.FACE_TOWARD);// reset to upfield
        
        addAction("turns backwards",
                () -> shouldTurnLeft(170),
                () -> {
                    drive.turnDrive(0.5);
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("Move while gyro is flat",
                () -> gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                });
        addAction("while robot is not flat, lower TBar",
                () -> !gyro.isFlat(),
                () -> {
                    drive.stop();
                });
        addAction("move backwards",
                () -> forDurationSecs(0.7f),
                () -> {
                    drive.arcadeDrive(0.0, 0.4);
                });
        addAction("lift bar",
                () -> gyro.isDown() || gyro.isFlat(),
                () -> {
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("Move while gyro is up",
                () -> gyro.isUp(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                });
        addAction("Move while gyro is flat or when gyro is down",
                () -> (gyro.isFlat()|| gyro.isDown()) && forDurationSecs(0.6f),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                });
        if (!gyro.isFlat()){
            addAction("go fowards while not flat and for 1 second",
                    () -> !gyro.isFlat() && forDurationSecs(1.0f),
                    () -> {
                        drive.arcadeDrive(0.0, 0.5);
                    });
        }
        if (shouldTurnLeft(10)){
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnLeft(10),
                    () -> {
                        drive.turnDrive(0.4);
                    });
        }else{
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnRight(-10),
                    () -> {
                        drive.turnDrive(-0.4);
                    });
        }
        addAction("Move while the Ultrasonic is more than 5 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (clockwise)",
                () -> shouldTurnRight(80), 
                () -> {
                    drive.turnDrive(-0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 5 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (counterclockwise)",
                () -> shouldTurnLeft(10), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 3 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 36,
                () -> {
                    drive.arcadeDrive(0.0, -0.3);
                });
        addAction("Turn (counterclockwise)",
                () -> shouldTurnLeft(-40), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction ("Drive fowards while robot is not up",
                () -> !gyro.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Shoot the low goal",
                () -> gyro.getYawAngle() <= -40 && gyro.getTiltAngle() > 4.0,
                () -> {
                    drive.stop();
                    roller.runRoller(RollerDirection.OUT);
                });
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }
    
    private void initDrawBridge()
    {
        addAction("turns backwards",
                () -> shouldTurnLeft(170),
                () -> {
                    drive.turnDrive(0.5);
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                    tbar.launchTBar(tBarDirection.UP);
                });
//        addAction("Move while gyro is flat, up, or down",
//                () -> (gyro.isFlat() || gyro.isUp() || gyro.isDown()) && ultrasonic.getBackRangeInches() > 36,
//                () -> {
//                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
//                    drive.arcadeDrive(0.0, 0.7);
//                });
//        addAction("approach",
//                () -> ultrasonic.getBackRangeInches() <= 36,
//                () -> {
//                    drive.stop();
//                    tbar.launchTBar(tBarDirection.DOWN);
//                });
//        addAction("open door",
//                () -> ultrasonic.getBackRangeInches() > 48,
//                () -> {
//                    drive.arcadeDrive(0.0, -0.5);
//                    tbar.launchTBar(tBarDirection.DOWN);
//                });
        addAction("move ahead",
                () -> gyro.isFlat() || gyro.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, 0.6);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("get off ramp",
                () -> gyro.isDown(),
                () -> {
                    drive.arcadeDrive(0.0, 0.6);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        if (!gyro.isFlat()){
            addAction("go fowards while not flat and for 1 second",
                    () -> !gyro.isFlat() && forDurationSecs(1.0f),
                    () -> {
                        drive.arcadeDrive(0.0, 0.5);
                    });
        }
        if (shouldTurnLeft(10)){
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnLeft(10),
                    () -> {
                        drive.turnDrive(0.4);
                        tbar.launchTBar(tBarDirection.UP);
                    });
        }else{
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnRight(-10),
                    () -> {
                        drive.turnDrive(-0.4);
                        tbar.launchTBar(tBarDirection.UP);
                    });
        }
        addAction("Move while the Ultrasonic is more than 5 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (clockwise)",
                () -> shouldTurnRight(80), 
                () -> {
                    drive.turnDrive(-0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 5 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (counterclockwise)",
                () -> shouldTurnLeft(10), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 3 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 36,
                () -> {
                    drive.arcadeDrive(0.0, -0.3);
                });
        addAction("Turn (counterclockwise)",
                () -> shouldTurnLeft(-40), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction ("Drive fowards while robot is not up",
                () -> !gyro.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Shoot the low goal",
                () -> gyro.getYawAngle() <= -40 && gyro.getTiltAngle() > 4.0,
                () -> {
                    drive.stop();
                    roller.runRoller(RollerDirection.OUT);
                });
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }
    
    private void initChevalDeFrise()
    {
        addAction("turns backwards",
                () -> shouldTurnLeft(170),
                () -> {
                    drive.turnDrive(0.5);
                    roller.runManipulator(ManipIntent.SHOULD_RETRACT);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("move while flat",
                () -> gyro.isFlat(),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                });
        addAction("while up",
                () -> gyro.isDown(),
                () -> {
                    drive.stop();
                });
        addAction("move back while down",
                () -> gyro.isDown(),
                () -> {
                    drive.arcadeDrive(0.0, -0.6);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("Move while robot is up or down",
                () -> gyro.isUp() || gyro.isDown(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("move while flat and raise bar",
                () -> gyro.isFlat(),
                () -> {
                    drive.arcadeDrive(0.0, 0.7);
                    tbar.launchTBar(tBarDirection.UP);
                });
        if (!gyro.isFlat()){
            addAction("go fowards while not flat and for 1 second",
                    () -> !gyro.isFlat() && forDurationSecs(1.0f),
                    () -> {
                        drive.arcadeDrive(0.0, 0.5);
                    });
        }
        if (shouldTurnLeft(0)){
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnLeft(0),
                    () -> {
                        drive.turnDrive(0.4);
                        tbar.launchTBar(tBarDirection.UP);
                    });
        }else{
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnRight(0),
                    () -> {
                        drive.turnDrive(-0.4);
                        tbar.launchTBar(tBarDirection.UP);
                    });
        }
        addAction("Move while the Ultrasonic is more than 5 feet from the castle",
                () -> ultrasonic.getFrontRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (clockwise)",
                () -> shouldTurnRight(80), 
                () -> {
                    drive.turnDrive(-0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 12 inches from the wall",
                () -> ultrasonic.getFrontRangeInches() > 12,
                () -> {
                    drive.arcadeDrive(0.0, -0.3);
                });
        addAction("Turn 90 degrees (counterclockwise)",
                () -> shouldTurnLeft(10), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 3 feet from the wall",
                () -> ultrasonic.getFrontRangeInches() > 36,
                () -> {
                    drive.arcadeDrive(0.0, -0.3);
                });
        addAction("Turn 90 degrees (counterclockwise)",
                () -> shouldTurnLeft(-40), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction ("Drive fowards while robot is not flat",
                () -> !gyro.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Shoot the low goal",
                () -> gyro.getYawAngle() == -40 && gyro.getTiltAngle() > 4.0,
                () -> {
                    drive.stop();
                    roller.runRoller(RollerDirection.OUT);
                });
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    tbar.stop();
                    roller.stop();
                });
    }
    
    private void initHighGoal()
    {
        // Move ahead over barrier, align to high goal, take shot
        final int marginOfError = 30;
        
        addAction("Rotate while square with widest is not centered",
                () -> forever(),
                () -> {
                    seekWidestContour(marginOfError);
                });
        addAction("extend ball roller",
                () -> shooter.aim(marginOfError),
                () -> {
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                });
        addAction("Shoot the high goal",
                () -> shooter.aim(marginOfError),
                ()-> {
                    roller.in(1.0);
                    // TODO Shoot
                });
        addAction("Stop driving",
                () -> forever(),
                () -> {
                    drive.stop();
                });
    }

    private void initDriveOnly()
    {
        // Drive to auto zone. Starts on the very edge and just creeps into the zone
        Gyro2016.getInstance();
        addAction("Drive into auto zone", 
                () -> forDurationSecs(2.0f), 
                () -> {
                    roller.stop();
                    tbar.stop();
                    drive.arcadeDrive(0.0, 0.5);
                });
        addAction("Stop driving", 
                () -> forever(), 
                () -> {
                    roller.stop();
                    tbar.stop();
                    drive.stop();
                });
    }

    private void initStayInPlace()
    {
        // Stay in place. Reset to upfield.
          Gyro2016.getInstance();
        addAction("Stop driving", 
                () -> forever(), 
                () -> {
                    roller.stop();
//                    tbar.stop();
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
        final boolean targetIsCentered = shooter.aim(marginOfError);
        SmartDashboard.putString("DB/String 8", "Centered: " + targetIsCentered);
        if (targetIsCentered)
        {
            approach(40);
        }
    }
    
    /**
     * 
     * @param desiredDistance in inches
     */
    private void approach(double desiredDistance)
    {
        final double measuredDistance = ultrasonic.getFrontRangeInches();
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
        NO_AUTO, AIM, DRIVE_ONLY, STAY_IN_PLACE, PORTCULLIS, NEW_PORTCULLIS, DRAWBRIDGE, SALLY_PORT, CROSS_BARRIER, HIGH_GOAL, CHEVAL_DE_FRISE
    }
}
