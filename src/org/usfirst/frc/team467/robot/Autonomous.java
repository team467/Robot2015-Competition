package org.usfirst.frc.team467.robot;

import java.util.LinkedList;
import java.util.List;

import org.usfirst.frc.team467.robot.BallRollers.ManipIntent;
import org.usfirst.frc.team467.robot.BallRollers.RollerDirection;
import org.usfirst.frc.team467.robot.DriverStation2016.Speed;
import org.usfirst.frc.team467.robot.TBar.tBarDirection;

import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;

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
        AutoType autonomousType = AutoType.NO_AUTO;
        int pos = 1;
        try
        {
            String sAutoType = SmartDashboard.getString("DB/String 9");
            for (AutoType auto : AutoType.values())
            {
                if (sAutoType.contains(auto.key))
                {
                    autonomousType = auto;
                }
            }
            for (int i = 1; i <= 5; i++)
            {
                if (sAutoType.contains(String.valueOf(i)))
                {
                    pos = i;
                }
            }
            
//            autonomousType = AutoType.valueOf(SmartDashboard.getString("DB/String 9"));
            LOGGER.info("AUTO MODE " + autonomousType);
        }
        catch (TableKeyNotDefinedException e)
        {
            LOGGER.info("Cannot recognize auto mode");
            autonomousType = AutoType.STAY_IN_PLACE;
        }

        // Reset actions.
        actions.clear();
        resetActionStartTime();
        
        // Set up gyro and create actions list.
        switch (autonomousType)
        {
            case CROSS_BARRIER:
                initCrossBarrier(pos);
                break;   
            case SALLY_PORT:
                initSallyPort(pos);
                break;
            case PORTCULLIS:
                initPortcullis(pos);
                break;
            case DRAWBRIDGE:
                initDrawBridge(pos);
                break;
            case CHEVAL_DE_FRISE:
                initChevalDeFrise(pos);
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
    
    private void robotTurnZero(){
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
        gyro.reset();
    }
    
    private void robotTurn180(){
        if (shouldTurnLeft(-170)){
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnLeft(-170),
                    () -> {
                        drive.turnDrive(0.4);
                    });
        }else{
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnRight(170),
                    () -> {
                        drive.turnDrive(-0.4);
                    });
        }
        gyro.reset();
    }
    
    private void shootToRight(int position){
        if(position == 4){
            addAction("turn 90 degrees (counterclockwise)",
                    () -> shouldTurnLeft(-80),
                    () -> {
                        drive.turnDrive(0.5);
                    });
            addAction("Move while the Ultrasonic is more than 3 feet from the wall",
                    () -> ultrasonic.getRangeInches() > 36,
                    () -> {
                        drive.arcadeDrive(0.0, -0.5);
                    });
            addAction("Turn 90 degrees (clockwise)",
                    () -> shouldTurnLeft(-10), 
                    () -> {
                        drive.turnDrive(0.5);
                        LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                    });
        }
            addAction("move while the ultrasound is more than 12 inches from the wall",
                    () -> ultrasonic.getRangeInches() > 12,
                    () -> {
                        drive.arcadeDrive(0.0, -0.6);
                    });
            addAction("Turn (counterclockwise)",
                    () -> shouldTurnLeft(-40), 
                    () -> {
                        drive.turnDrive(0.5);
                    });
            addAction ("Drive fowards while robot is flat",
                    () -> gyro.isFlat(),
                    () -> {
                        drive.arcadeDrive(0.0, -0.5);
                    });
            addAction("Shoot the low goal",
                    () -> !gyro.isFlat(),
                    () -> {
                        drive.stop();
                        roller.runRoller(RollerDirection.OUT);
                    });
    }
    
    private void shootToLeft(int position){
        if(position == 3){
            addAction("turn 90 degrees (counterclockwise)",
                    () -> shouldTurnLeft(-80),
                    () -> {
                        drive.turnDrive(0.5);
                    });
            addAction("Move while the Ultrasonic is more than 3 feet from the wall",
                    () -> ultrasonic.getRangeInches() > 36,
                    () -> {
                        drive.arcadeDrive(0.0, -0.5);
                    });
            addAction("Turn 90 degrees (clockwise)",
                    () -> shouldTurnLeft(-10), 
                    () -> {
                        drive.turnDrive(0.5);
                        LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                    });
        }
        addAction("move while the ultrasound is more than 12 inches from the wall",
                () -> ultrasonic.getRangeInches() > 12,
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Turn (clockwise)",
                () -> shouldTurnRight(40), 
                () -> {
                    drive.turnDrive(-0.5);
                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                });
        addAction ("Drive fowards while robot is not flat",
                () -> gyro.isFlat(),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        addAction("Shoot the low goal",
                () -> !gyro.isFlat(),
                () -> {
                    drive.stop();
                    roller.runRoller(RollerDirection.OUT);
                });
    }
    
//    private void pos3() {
//        addAction("turn 90 degrees (counterclockwise)",
//                () -> shouldTurnLeft(-80),
//                () -> {
//                    drive.turnDrive(0.5);
//                });
//        addAction("Move while the Ultrasonic is more than 3 feet from the wall",
//                () -> ultrasonic.getRangeInches() > 36,
//                () -> {
//                    drive.arcadeDrive(0.0, -0.5);
//                });
//        addAction("Turn 90 degrees (clockwise)",
//                () -> shouldTurnLeft(-80), 
//                () -> {
//                    drive.turnDrive(0.5);
//                });
//        addAction("move while the robot is more than 6 feet from the wall",
//                () -> ultrasonic.getRangeInches() > 72,
//                () -> {
//                    drive.arcadeDrive(0.0, -0.6);
//                });
//        addAction("Turn (clockwise)",
//                () -> shouldTurnRight(40), 
//                () -> {
//                    drive.turnDrive(-0.5);
//                });
//        addAction ("Drive fowards while robot is more than 12 inches from the wall",
//                () -> ultrasonic.getRangeInches() > 12,
//                () -> {
//                    drive.arcadeDrive(0.0, -0.5);
//                });
//        addAction("Turn (clockwise)",
//                () -> shouldTurnRight(40), 
//                () -> {
//                    drive.turnDrive(-0.5);
//                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
//                });
//        addAction ("Drive fowards while robot is not flat",
//                () -> !gyro.isFlat(),
//                () -> {
//                    drive.arcadeDrive(0.0, -0.5);
//                });
//        addAction("Shoot the low goal",
//                () -> gyro.getYawAngle() <= -40 && gyro.getTiltAngle() > 4.0,
//                () -> {
//                    drive.stop();
//                    roller.runRoller(RollerDirection.OUT);
//                });
//    }
//    
//    private void pos4(){
//        addAction("Move while the Ultrasonic is more than 3 feet from the wall",
//                () -> ultrasonic.getRangeInches() > 36,
//                () -> {
//                    drive.arcadeDrive(0.0, -0.5);
//                });
//        addAction("Turn 90 degrees (clockwise)",
//                () -> shouldTurnRight(80), 
//                () -> {
//                    drive.turnDrive(-0.5);
//                });
//        addAction("move while the robot is more than 6 feet from the wall",
//                () -> ultrasonic.getRangeInches() > 72,
//                () -> {
//                    drive.arcadeDrive(0.0, -0.6);
//                });
//        addAction("Turn (counter-clockwise)",
//                () -> shouldTurnLeft(-80), 
//                () -> {
//                    drive.turnDrive(0.5);
//                });
//        addAction ("Drive fowards while robot is more than 12 inches from the wall",
//                () -> ultrasonic.getRangeInches() > 12,
//                () -> {
//                    drive.arcadeDrive(0.0, -0.5);
//                });
//        addAction("Turn (counter-clockwise)",
//                () -> shouldTurnLeft(-40), 
//                () -> {
//                    drive.turnDrive(0.5);
//                    LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
//                });
//        addAction ("Drive fowards while robot is not flat",
//                () -> !gyro.isFlat(),
//                () -> {
//                    drive.arcadeDrive(0.0, -0.5);
//                });
//        addAction("Shoot the low goal",
//                () -> gyro.getYawAngle() <= -40 && gyro.getTiltAngle() > 4.0,
//                () -> {
//                    drive.stop();
//                    roller.runRoller(RollerDirection.OUT);
//                });
//    }
    
    private void initCrossBarrier(int position)
    {
        //Moves over barriers using the tilt gyrometer to detect when the robot is going up, down, or driving flat
        //Moves towards the walls using the ultrasonic sensor, and turns (counter)clockwise with the yaw gyrometer to line up for the low shoot
        addAction("Move while gyro is up or flat",
                () -> gyro.isUp() || gyro.isFlat(),
                () -> {
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                    drive.arcadeDrive(0.0, -0.7);
                });
        addAction("Move while gyro is down",
                () -> gyro.isDown(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, -0.7);
                });
        addAction("move fowards for 0.7 seconds",
                () -> forDurationSecs(0.7f),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                });
        robotTurnZero();

        if(position == 1 || position == 2){
            shootToLeft(1);
        }
        if(position == 3){
            shootToLeft(3);
        }
        if (position == 4){
            shootToRight(4);
        }
        if (position == 5){
            shootToRight(5);
        }
        
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }

    private void initSallyPort(int position){
        addAction("Move while gyro is flat",
                () -> gyro.isFlat(),
                () -> {
                    drive.arcadeDrive(0.0, 0.7);
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("Move TBar arm down",
                () -> gyro.isDown() && forDurationSecs(1.0f),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("move forwards",
                () -> forDurationSecs(0.3f),
                () -> {
                    drive.arcadeDrive(0.0, -0.7);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("Turn to the left to open the door",
                () -> shouldTurnLeft(-60),
                () -> {
                    drive.turnDrive(0.4);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("Move backwards",
                () -> forDurationSecs(0.5f),
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
        robotTurn180();
        if(position == 1 || position == 2){
            shootToLeft(1);
        }
        if(position == 3){
            shootToLeft(3);
        }
        if (position == 4){
            shootToRight(4);
        }
        if (position == 5){
            shootToRight(5);
        }
        
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }
    
    private void initPortcullis(int position)
    {        
        addAction("Move while gyro is flat",
                () -> gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                });
        addAction("for 1 sec lower TBar",
                () -> forDurationSecs(1.0f),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("move backwards",
                () -> gyro.isDown(),
                () -> {
                    drive.arcadeDrive(0.0, 0.4);
                    tbar.launchTBar(tBarDirection.DOWN); 
                });
        addAction("lift tbar",
                () -> (gyro.isFlat() || gyro.isUp()) && forDurationSecs(0.5f),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("go ahead",
                () -> forDurationSecs(0.5f),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                });
        robotTurn180();
        if(position == 1 || position == 2){
            shootToLeft(1);
        }
        if(position == 3){
            shootToLeft(3);
        }
        if (position == 4){
            shootToRight(4);
        }
        if (position == 5){
            shootToRight(5);
        }
        
        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }
    
    private void initDrawBridge(int position)
    {
        addAction("Move while gyro is flat",
                () -> gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                });
        addAction("approach",
                () -> forDurationSecs(1.0f),
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
        robotTurn180();
        if(position == 1 || position == 2){
            shootToLeft(1);
        }
        if(position == 3){
            shootToLeft(3);
        }
        if (position == 4){
            shootToRight(4);
        }
        if (position == 5){
            shootToRight(5);
        }
        

        addAction("Done",
                () -> forever(), 
                () -> {
                    drive.stop();
                    roller.stop();
                    tbar.stop();
                });
    }
    
    private void initChevalDeFrise(int position)
    {
        addAction("move while flat",
                () -> gyro.isFlat(),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                });
        addAction("for 0.7 secs ",
                () -> forDurationSecs(0.7f),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("Move while robot is down",
                () -> forDurationSecs(0.7f),
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
        robotTurn180();
        if(position == 1 || position == 2){
            shootToLeft(1);
        }
        if(position == 3){
            shootToLeft(3);
        }
        if (position == 4){
            shootToRight(4);
        }
        if (position == 5){
            shootToRight(5);
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
        NO_AUTO("NO_AUTO"), AIM("AIM"), DRIVE_ONLY("DRIVE"), STAY_IN_PLACE("STAY"), HIGH_GOAL("HIGH"),
        PORTCULLIS("PORT"), DRAWBRIDGE("BRIDGE"), CROSS_BARRIER("CROSS"), CHEVAL_DE_FRISE("CHEVAL"), SALLY_PORT("SALLY");
        
        public String key;
        
        private AutoType(String key)
        {
            this.key = key;
        }
    }
}
