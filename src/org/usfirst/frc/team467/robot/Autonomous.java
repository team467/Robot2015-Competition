package org.usfirst.frc.team467.robot;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.usfirst.frc.team467.robot.BallRollers.ManipIntent;
import org.usfirst.frc.team467.robot.BallRollers.RollerDirection;
import org.usfirst.frc.team467.robot.DriverStation2015.Speed;
import org.usfirst.frc.team467.robot.TBar.tBarDirection;

import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.apache.log4j.Logger;

public class Autonomous
{
    private static final Logger LOGGER = Logger.getLogger(Autonomous.class);

    private static Autonomous autonomous = null;

    private Gyro2016 gyro = null;    
    private Driveable drive = null;
    private VisionProcessor vision = null;
    private Ultrasonic2016 ultrasonic = null;
    
    private TBar tbar = null;

    BallRollers roller = null;
    
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

    public void setUltrasonic(Ultrasonic2016 ultra)
    {
        this.ultrasonic = ultra;
    }
    
    public void setRoller(BallRollers roller)
    {
        this.roller = roller;
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
    private Autonomous(VisionProcessor vision, Gyro2016 gyro)
    {
        // TODO Change drive, claw, and lifter to generics implementing respective interfaces
        this.vision = vision;
        this.gyro = gyro;
    }

    /**
     * Sets up the periodic function
     */
    public void initAutonomous()
    {
        //AutoType autonomousType = (AutoType) SmartDashboard.getString("DB/String 6");
        AutoType autonomousType = AutoType.valueOf(SmartDashboard.getString("DB/String 6"));
        LOGGER.info("AUTO MODE " + autonomousType);

        // Reset actions.
        actions.clear();
        resetActionStartTime();
        
        // Set up gyro and create actions list.
        switch (autonomousType)
        {
            case CROSS_BARRIER_RIGHT:
                initCrossBarrier("right");
                break;   
            case CROSS_BARRIER_LEFT:
                initCrossBarrier("left");
                break; 
            case SALLY_PORT_RIGHT:
                initSallyPort("right");
                break;
            case SALLY_PORT_LEFT:
                initSallyPort("left");
                break;
//            case NEW_PORTCULLIS:
//                initNewPortcullis();
//                break;
            case PORTCULLIS_RIGHT:
                initPortcullis("right");
                break;
            case PORTCULLIS_LEFT:
                initPortcullis("left");
                break;
            case DRAWBRIDGE_RIGHT:
                initDrawBridge("right");
                break;
            case DRAWBRIDGE_LEFT:
                initDrawBridge("left");
                break;
            case CHEVAL_DE_FRISE_RIGHT:
                initChevalDeFrise("right");
                break;
            case CHEVAL_DE_FRISE_LEFT:
                initChevalDeFrise("left");
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
    
    private void shootToRight(){
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
                () -> ultrasonic.getRangeInches() > 60,
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
                    () -> ultrasonic.getRangeInches() > 60,
                    () -> {
                        drive.arcadeDrive(0.0, -0.5);
                    });
            addAction("Turn 90 degrees (counterclockwise)",
                    () -> shouldTurnLeft(-10), 
                    () -> {
                        drive.turnDrive(0.5);
                        LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
            addAction("Move ahead while the robot is more than 3 feet from the wall",
                    () -> ultrasonic.getRangeInches() > 36,
                    () -> {
                        drive.arcadeDrive(0.0, -0.3);
                    });
            addAction("Turn (counterclockwise)",
                    () -> shouldTurnLeft(-40), 
                    () -> {
                        drive.turnDrive(0.5);
                        LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                    });
    }
    
    private void shootToLeft(){
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
                () -> ultrasonic.getRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (counterclockwise)",
                () -> shouldTurnLeft(-80), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 5 feet from the wall",
                () -> ultrasonic.getRangeInches() > 60,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn 90 degrees (clockwise)",
                () -> shouldTurnLeft(-10), 
                () -> {
                    drive.turnDrive(0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction("Move ahead while the robot is more than 3 feet from the wall",
                () -> ultrasonic.getRangeInches() > 36,
                () -> {
                    drive.arcadeDrive(0.0, -0.3);
                });
        addAction("Turn (clockwise)",
                () -> shouldTurnRight(40), 
                () -> {
                    drive.turnDrive(-0.5);
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
    }
    private void initCrossBarrier(String shoot)
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
        addAction("move fowards a little bit",
                () -> forDurationSecs(0.7f),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        if(shoot.equals("right"))
        {
          shootToRight();
        }
        if(shoot.equals("left")){
            shootToLeft();
        }
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }

    private void initSallyPort(String shoot){
        addAction("turns backwards",
                () -> shouldTurnLeft(170),
                () -> {
                    drive.turnDrive(0.5);
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("Move while gyro is flat",
                () -> gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                });
        addAction("Move TBar arm down",
                () -> gyro.isDown(),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
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
            addAction("go fowards while not flat",
                    () -> !gyro.isFlat(),
                    () -> {
                        drive.arcadeDrive(0.0, 0.5);
                    });
        }
        addAction("move ahead",
                () -> forDurationSecs(07.f),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                });
        if(shoot.equals("right"))
        {
          shootToRight();
        }
        if(shoot.equals("left")){
            shootToLeft();
        }
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }
//    
//    private void initNewPortcullis(String shoot){
//        addAction("turns backwards",
//                () -> shouldTurnLeft(170),
//                () -> {
//                    drive.turnDrive(0.5);
//                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
//                    tbar.launchTBar(tBarDirection.UP);
//                });
//        addAction("Move while gyro is down or flat",
//                () -> gyro.isDown() || gyro.isFlat(),
//                () -> {
//                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
//                    tbar.launchTBar(tBarDirection.DOWN);
//                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
//                    drive.arcadeDrive(0.0, 0.7);
//                });
//        addAction("Move while gyro is up",
//                () -> gyro.isUp(),
//                () -> {
//                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
//                    drive.arcadeDrive(0.0, 0.7);
//                });
//        if (!gyro.isFlat()){
//            addAction("go fowards while not flat and for 1 second",
//                    () -> !gyro.isFlat() && forDurationSecs(1.0f),
//                    () -> {
//                        drive.arcadeDrive(0.0, 0.5);
//                    });
//        }
//        if (shouldTurnLeft(10)){
//            addAction("Turn to zero degrees",
//                    () -> gyro.isFlat() && shouldTurnLeft(10),
//                    () -> {
//                        drive.turnDrive(0.4);
//                    });
//        }else{
//            addAction("Turn to zero degrees",
//                    () -> gyro.isFlat() && shouldTurnRight(-10),
//                    () -> {
//                        drive.turnDrive(-0.4);
//                    });
//        }
//        if(shoot.equals("right"))
//    {
//        shootToRight();
//      }
//      if(shoot.equals("left")){
//          shootToLeft();
//      }
//        addAction("Done",
//                () -> forever(), 
//                () -> {
//                    drive.stop();
//                    roller.stop();
//                    tbar.stop();
//                });
//    }
//    
    private void initPortcullis(String shoot)
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
        addAction("while robot is down, lower TBar",
                () -> gyro.isDown(),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("move backwards",
                () -> gyro.isDown() || gyro.isFlat(),
                () -> {
                    drive.arcadeDrive(0.0, 0.4);
                    tbar.launchTBar(tBarDirection.DOWN); 
                });
        addAction("lift bar",
                () -> gyro.isUp(),
                () -> {
                    tbar.launchTBar(tBarDirection.UP);
                    drive.arcadeDrive(0.0, 0.5);
                });
        if (!gyro.isFlat()){
            addAction("go fowards while not flat and for 1 second",
                    () -> !gyro.isFlat() && forDurationSecs(1.0f),
                    () -> {
                        drive.arcadeDrive(0.0, 0.5);
                    });
        }
        addAction("go ahead",
                () -> forDurationSecs(0.7f),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                });
        if(shoot.equals("right"))
        {
          shootToRight();
        }
        if(shoot.equals("left")){
            shootToLeft();
        }
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }
    
    private void initDrawBridge(String shoot)
    {
        addAction("turns backwards",
                () -> shouldTurnLeft(170),
                () -> {
                    drive.turnDrive(0.5);
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("Move while gyro is flat",
                () -> gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                });
        addAction("approach",
                () -> gyro.isDown(),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("open door",
                () -> gyro.isDown() || forDurationSecs(1.0f),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("move ahead",
                () -> gyro.isFlat() || gyro.isDown(),
                () -> {
                    drive.arcadeDrive(0.0, 0.6);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("get off ramp",
                () -> gyro.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                    tbar.launchTBar(tBarDirection.UP);
                });
        if (!gyro.isFlat()){
            addAction("go fowards while not flat and for 1 second",
                    () -> !gyro.isFlat() && forDurationSecs(1.0f),
                    () -> {
                        drive.arcadeDrive(0.0, 0.5);
                    });
        }
        addAction("move ahead",
                () -> forDurationSecs(0.7f),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                });
        if(shoot.equals("right"))
        {
          shootToRight();
        }
        if(shoot.equals("left")){
            shootToLeft();
        }
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }
    
    private void initChevalDeFrise(String shoot)
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
        addAction("while down",
                () -> gyro.isDown(),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("Move while robot is down",
                () -> gyro.isDown(),
                () -> {
                    drive.arcadeDrive(0.0, 0.4);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("move arm up and move ahead while up",
                () -> gyro.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, 0.4);
                    tbar.launchTBar(tBarDirection.UP);
                });
        if (!gyro.isFlat()){
            addAction("go fowards while not flat",
                    () -> !gyro.isFlat(),
                    () -> {
                        drive.arcadeDrive(0.0, 0.5);
                    });
        }
        addAction("move ahead",
                () -> forDurationSecs(0.7f),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                });
        if(shoot.equals("right"))
        {
          shootToRight();
        }
        if(shoot.equals("left")){
            shootToLeft();
        }
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
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
                () -> seekAngle(marginOfError),
                () -> {
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                });
        addAction("Shoot the high goal",
                () -> seekAngle(marginOfError),
                ()-> {
                    //TODO
                    //Use the new high shooter method
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
    enum  AutoType
    {
        NO_AUTO, AIM, DRIVE_ONLY, STAY_IN_PLACE, PORTCULLIS_LEFT, PORTCULLIS_RIGHT, NEW_PORTCULLIS_LEFT, NEW_PORTCULLIS_RIGHT, DRAWBRIDGE_LEFT, DRAWBRIDGE_RIGHT, SALLY_PORT_LEFT, SALLY_PORT_RIGHT, CROSS_BARRIER_LEFT, CROSS_BARRIER_RIGHT, HIGH_GOAL, CHEVAL_DE_FRISE_LEFT, CHEVAL_DE_FRISE_RIGHT
    }
}
