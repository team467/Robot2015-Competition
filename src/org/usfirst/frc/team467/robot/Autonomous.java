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

    public void setTBar(TBar tbar)
    {
        this.tbar = tbar;
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
        this.gyro = gyro;
    }

    /**
     * Sets up the periodic function
     */
    public void initAutonomous()
    {
//            AutoType autonomousType;
//        try
//        {
//            autonomousType = AutoType.valueOf(SmartDashboard.getString("DB/String 2"));
//            LOGGER.info("AUTO MODE " + autonomousType);
//        }
//        catch (TableKeyNotDefinedException e)
//        {
//            LOGGER.info("Cannot recognize auto mode");
//            LOGGER.info("Doing AutoType STAY_IN_PLACE");
//            autonomousType = AutoType.STAY_IN_PLACE;
//        }

        AutoType autonomousType = DriverStation2016.getInstance().getAutoType();
//        AutoType autonomousType = AutoType.DRIVE_ONLY;
        LOGGER.info("AUTO MODE " + autonomousType);

        // Reset actions.
        actions.clear();
        resetActionStartTime();
        gyro.reset();
        
        // create actions list.
        switch (autonomousType)
        {
            case CROSS_BARRIER_1:
                initCrossBarrier(1);
                break;   
            case CROSS_BARRIER_2:
                initCrossBarrier(2);
                break;
            case CROSS_BARRIER_3:
                initCrossBarrier(3);
                break;
            case CROSS_BARRIER_4:
                initCrossBarrier(4);
                break; 
            case CROSS_BARRIER_5:
                initCrossBarrier(5);
                break;
            case SALLY_PORT_1:
                initSallyPort(1);
                break;
            case SALLY_PORT_2:
                initSallyPort(2);
                break;
            case SALLY_PORT_3:
                initSallyPort(3);
                break;
            case SALLY_PORT_4:
                initSallyPort(4);
                break;
            case SALLY_PORT_5:
                initSallyPort(5);
                break;
            case PORTCULLIS_1:
                initPortcullis(1);
                break;
            case PORTCULLIS_2:
                initPortcullis(2);
                break;
            case PORTCULLIS_3:
                initPortcullis(3);
                break;
            case PORTCULLIS_4:
                initPortcullis(4);
                break;
            case PORTCULLIS_5:
                initPortcullis(5);
                break;
            case DRAWBRIDGE_1:
                initDrawBridge(1);
                break;
            case DRAWBRIDGE_2:
                initDrawBridge(2);
                break;
            case DRAWBRIDGE_3:
                initDrawBridge(3);
                break;
            case DRAWBRIDGE_4:
                initDrawBridge(4);
                break;
            case DRAWBRIDGE_5:
                initDrawBridge(5);
                break;
            case CHEVAL_DE_FRISE_1:
                initChevalDeFrise(1);
                break;
            case CHEVAL_DE_FRISE_2:
                initChevalDeFrise(2);
                break;
            case CHEVAL_DE_FRISE_3:
                initChevalDeFrise(3);
                break;
            case CHEVAL_DE_FRISE_4:
                initChevalDeFrise(4);
                break;
            case CHEVAL_DE_FRISE_5:
                initChevalDeFrise(5);
                break;
            case AIM:
                initAim();
                break;
            case STAY_IN_PLACE:
                initStayInPlace();
                break;
//            case HIGH_GOAL:
//                initHighGoal();
//                break;
            case APPROACH_DEFENSE:
                initApproachDefense();
                break;
            case CROSS_DEFENSE:
                initCrossDefense();
                break;
            case CROSS_LOWBAR_SHOOT:
                initCrossAndShootLow();
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
    
    private void robotTurn(int angle)
    {
        int buffer = 10;
        int max = angle + buffer;
        int min = angle - buffer;
        if (shouldTurnLeft(max)){
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnLeft(max),
                    () -> {
                        drive.turnDrive(0.4);
                    });
        }else{
            addAction("Turn to zero degrees",
                    () -> gyro.isFlat() && shouldTurnRight(min),
                    () -> {
                        drive.turnDrive(-0.4);
                    });
        }
        gyro.reset();
    }
    
    private void robotTurn180(){
        if (shouldTurnLeft(-170)){
            addAction("Turn fowards",
                    () -> gyro.isFlat() && shouldTurnLeft(-170),
                    () -> {
                        drive.turnDrive(0.4);
                    });
        }else{
            addAction("Turn fowards",
                    () -> gyro.isFlat() && shouldTurnRight(170),
                    () -> {
                        drive.turnDrive(-0.4);
                    });
        }
        gyro.reset();
    }
    
    private void shootToRight(int position){
        if(position == 4){
            addAction("turn 90 degrees (clockwise)",
                    () -> shouldTurnRight(80),
                    () -> {
                        drive.turnDrive(-0.5);
                    });
            addAction("Move while the Ultrasonic is more than 3 feet from the wall",
                    () -> ultrasonic.getRangeInches() > 36,
                    () -> {
                        drive.arcadeDrive(0.0, -0.5);
                    });
            addAction("Turn 90 degrees (counterclockwise)",
                    () -> shouldTurnLeft(-10), 
                    () -> {
                        drive.turnDrive(0.5);
                        LOGGER.debug("GYRO YAW ANGLE = " + gyro.getYawAngle());
                    });
        }
            addAction("move while the ultrasound is more than 2 feet from the wall",
                    () -> ultrasonic.getRangeInches() > 24,
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
        addAction("move while the ultrasound is more than 2 feet from the wall",
                () -> ultrasonic.getRangeInches() > 24,
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
    
    private void initCrossBarrier(int position){
        
        //Moves over barriers using the tilt gyrometer to detect when the robot is going up, down, or driving flat
        //Moves towards the walls using the ultrasonic sensor
        //and turns (counter)clockwise with the yaw gyrometer to line up for the low shoot
        
        addAction("Move while gyro is up",
                () -> gyro.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, -0.7);
                });
        addAction("drive for 1 seconds",
                () -> forDurationSecs(1.0f),
                () -> {
                    drive.arcadeDrive(0.0, -0.8);
                });
        addAction("Move while gyro is down",
                () -> gyro.isDown(),
                () -> {
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
//                    roller.stop();
//                    tbar.stop();
                });
    }

    private void initSallyPort(int position){
        
        //drive up to and place tbar arm down on door
        //back up to open door and then turn to the left to allow entrance
        //move in on an angle straighten out; move forwards; align with low goal and shoot
        
        addAction("Move backwards while gyro is flat",
                () -> gyro.isFlat(), //||tbar.isDown(),
                () -> {
                    drive.arcadeDrive(0.0, 0.7);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("lower TBar",
                () -> gyro.isDown() && forDurationSecs(1.0f), //tbar.isUp(),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("drive away",
                () -> forDurationSecs(0.3f), //tbar.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, -0.7);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("Turn left to open the door",
                () -> shouldTurnLeft(-60), //||tbar.isUp(),
                () -> {
                    drive.turnDrive(0.4);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("get into door",
                () -> forDurationSecs(0.5f),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                });
        addAction("Turn right to straighten",
                () -> shouldTurnRight(-10), //||tbar.isDown(),
                () -> {
                    drive.turnDrive(-0.4);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("Move for 0.7 seconds",
                () -> forDurationSecs(0.7f),
                () -> {
                    drive.arcadeDrive(0.0, 0.8);
                });
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
    
    private void initPortcullis(int position){
        
        //move under portcullis with the arm down the whole time
        // return arm to position and align with low goal to shoot
        
        addAction("approach barrier",
                () -> gyro.isFlat(), //||tbar.isUp();
                () -> {
                    drive.arcadeDrive(0.0, 0.7);
                });
        addAction("lower TBar",
                () -> forDurationSecs(1.0f), //tbar.isUp(),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("get TBar under",
                () -> gyro.isDown(),//tbar.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, 0.4);
                    tbar.launchTBar(tBarDirection.DOWN); 
                });
        addAction("lift tbar and drive",
                () -> (gyro.isFlat() || gyro.isUp()) && forDurationSecs(0.5f), //tbar.isDown()
                () -> {
                    drive.arcadeDrive(0.0, 0.8);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("completey cross barrier",
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
    
    private void initDrawBridge(int position){
        
        //drive up to bridge and lower tbar on to it
        //lower tbar as robot moves away
        //go over bridge and align for low goal
        
        addAction("approach",
                () -> gyro.isFlat(),
                () -> {
                    LOGGER.debug("Gyro angle: " + gyro.getTiltAngle());
                    drive.arcadeDrive(0.0, 0.7);
                });
        addAction("lower tbar",
                () -> forDurationSecs(1.0f), //||tbar.isUp(),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("open door",
                () -> gyro.isDown() || forDurationSecs(1.0f), //||tbar.isUp(),
                () -> {
                    drive.arcadeDrive(0.0, -0.5);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("move ahead",
                () -> forDurationSecs(0.6f),
                () -> {
                    drive.arcadeDrive(0.0, 0.6);
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("get off ramp",
                () -> gyro.isUp(), //||tbar.isDown(),
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
    
    private void initChevalDeFrise(int position){
        
        //approach and lower tbar to bring plank down
        //move over barrier and align with low goal to shoot
        
        addAction("approach",
                () -> gyro.isFlat(),
                () -> {
                    drive.arcadeDrive(0.0, 0.5);
                });
        addAction("lower tbar",
                () -> forDurationSecs(0.7f),
                () -> {
                    drive.stop();
                    tbar.launchTBar(tBarDirection.DOWN);
                });
        addAction("cross barrier",
                () -> forDurationSecs(0.7f), //||tbar.isDown(),
                () -> {
                    drive.arcadeDrive(0.0, 0.4);
                    tbar.launchTBar(tBarDirection.UP);
                });
        addAction("raise tbar and continue crossing",
                () -> gyro.isUp(), //||tbar.isDown(),
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
        addAction("distance self from barrier",
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
    
//    private void initHighGoal()
//    {
//        // Move ahead over barrier, align to high goal, take shot
//        final int marginOfError = 30;
//        
//        addAction("Rotate while square with widest is not centered",
//                () -> forever(),
//                () -> {
//                    seekWidestContour(marginOfError);
//                });
//        addAction("extend ball roller",
//                () -> shooter.aim(marginOfError),
//                () -> {
//                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
//                });
//        addAction("Shoot the high goal",
//                () -> shooter.aim(marginOfError),
//                ()-> {
//                    roller.in(1.0);
//                    // TODO Shoot
//                });
//        addAction("Stop driving",
//                () -> forever(),
//                () -> {
//                    drive.stop();
//                });
//    }
//    private void initHighGoal()
//    {
//        // Move ahead over barrier, align to high goal, take shot
//        final int marginOfError = 30;
//        
//        addAction("Rotate while square with widest is not centered",
//                () -> forever(),
//                () -> {
//                    seekWidestContour(marginOfError);
//                });
//        addAction("extend ball roller",
//                () -> seekAngle(marginOfError),
//                () -> {
//                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
//                });
//        addAction("Shoot the high goal",
//                () -> seekAngle(marginOfError),
//                ()-> {
//                    //TODO
//                    //Use the new high shooter method
//                });
//        addAction("Stop driving",
//                () -> forever(),
//                () -> {
//                    drive.stop();
//                });
//    }
    
    private void initApproachDefense()
    {
        // Drive until tilted up; aka on defense ramp
        addAction("Drive to defense ramp", 
                () -> gyro.isFlat(), 
                () -> {
                    roller.stop();
                    tbar.stop();
                    drive.arcadeDrive(0.0, -0.8);
                });
        addAction("Drive up defense ramp", 
                () -> gyro.isUp(), 
                () -> {
                    drive.arcadeDrive(0.0, -0.8);
                });
        addAction("Stop driving", 
                () -> forever(), 
                () -> {
                    roller.stop();
                    tbar.stop();
                    drive.stop();
                });
    }

    private void initCrossDefense()
    {
        crossDefense();
    }
    
    private void initCrossAndShootLow()
    {
        addAction("Lower gamepieces",
                () -> forDurationSecs(1.0f),
                () ->
                {
                    tbar.launchTBar(tBarDirection.DOWN);
                    roller.runManipulator(ManipIntent.SHOULD_EXTEND);
                }
                );
        crossDefense();
        robotTurnZero();
        addAction("Approach Wall",
                () -> untilClose(24.0),
                () -> {
                    approach(24.0);
                }
                );
        robotTurn(90);
        
        addAction("Approach Goal",
                () -> forDurationSecs(2.5f),
                () -> {
                    drive.strafeDrive(Direction.FRONT);
                }
                );
        addAction("Shoot Low Goal",
                () -> forDurationSecs(1f),
                () -> {
                    roller.runRoller(RollerDirection.OUT);
                });
    }
    
    private void crossDefense()
    {
        // Drive until tilted up; aka on defense ramp
        addAction("Drive to defense ramp", 
                () -> gyro.isFlat(), 
                () -> {
                    drive.arcadeDrive(0.0, -0.8);
                });
        addAction("Drive up defense ramp", 
                () -> gyro.isUp(), 
                () -> {
                    drive.arcadeDrive(0.0, -0.8);
                });
        addAction("Cross defense", 
                () -> forDurationSecs(2.0f), 
                () -> {
                    drive.arcadeDrive(0.0, -0.8);
                });
        addAction("Drive down defense ramp", 
                () -> gyro.isDown(), 
                () -> {
                    drive.arcadeDrive(0.0, -0.8);
                });
        addAction("Drive off defense ramp", 
                () -> forDurationSecs(2.0f), 
                () -> {
                    drive.arcadeDrive(0.0, -0.8);
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
        
        addAction("Stop driving", 
                () -> forever(), 
                () -> {
                    roller.stop();
                    tbar.stop();
                    drive.stop();
                });
    }
    
    private void initAim()
    {
        final int marginOfError = 30;
        
//        addAction("Rotate while square with widest is not centered",
////                () -> untilWidestCentered(marginOfError),
//                () -> forever(),
//                () -> {
//                    seekWidestContour(marginOfError);
//                });
        
        addAction("Stop driving",
                () -> forever(),
                () -> {
                    drive.stop();
                });
    }

//    private void seekWidestContour(int marginOfError)
//    {
//        final boolean targetIsCentered = shooter.aim(marginOfError);
//        SmartDashboard.putString("DB/String 8", "Centered: " + targetIsCentered);
//        if (targetIsCentered)
//        {
//            approach(40);
//        }
//    }
    
    boolean untilClose(double desiredDistance)
    {
        final double measuredDistance = ultrasonic.getRangeInches();
        final double delta = Math.abs(measuredDistance - desiredDistance);
        return (delta > 12);
    }
    
    /**
     * 
     * @param desiredDistance in inches
     */
    private void approach(double desiredDistance)
    {
        final double measuredDistance = ultrasonic.getRangeInches();
        if (measuredDistance > desiredDistance)
        {
            drive.strafeDrive(Direction.FRONT);
        }
        else
        {
            drive.strafeDrive(Direction.BACK);
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
        NO_AUTO, AIM, DRIVE_ONLY, STAY_IN_PLACE, HIGH_GOAL, APPROACH_DEFENSE, CROSS_DEFENSE, CROSS_LOWBAR_SHOOT,
        PORTCULLIS_1, PORTCULLIS_2, PORTCULLIS_3, PORTCULLIS_4, PORTCULLIS_5,
        DRAWBRIDGE_1, DRAWBRIDGE_2, DRAWBRIDGE_3, DRAWBRIDGE_4, DRAWBRIDGE_5,
        CROSS_BARRIER_1, CROSS_BARRIER_2, CROSS_BARRIER_3, CROSS_BARRIER_4, CROSS_BARRIER_5,
        CHEVAL_DE_FRISE_1, CHEVAL_DE_FRISE_2, CHEVAL_DE_FRISE_3, CHEVAL_DE_FRISE_4, CHEVAL_DE_FRISE_5,
        SALLY_PORT_1, SALLY_PORT_2, SALLY_PORT_3, SALLY_PORT_4, SALLY_PORT_5
    }
}
