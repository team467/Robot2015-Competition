/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.*;

/**
 * 
 */
public class SwerveDrive implements Driveable
{
    private static final Logger LOGGER = Logger.getLogger(SwerveDrive.class);

    // Steering objects
    public Steering[] steering;

    // Data storage object
    private DataStorage data;
    private Gyro2015 gyro;

    // Angle to turn at when rotating in place - initialized in constructor
    // takes the arctan of width over length in radians
    // Length is the wide side
    private static final double TURN_IN_PLACE_ANGLE = Math.atan(RobotMap.LENGTH / RobotMap.WIDTH);

    // Magic number copied from WPI code
    private static final byte SYNC_GROUP = (byte) 0x80;

    // Invert the drive motors to allow for wiring.
    private static final boolean FRONT_LEFT_DRIVE_INVERT = false;
    private static final boolean FRONT_RIGHT_DRIVE_INVERT = true;
    private static final boolean BACK_LEFT_DRIVE_INVERT = false;
    private static final boolean BACK_RIGHT_DRIVE_INVERT = true;
    
    private final CANTalon frontLeftMotor;
    private final CANTalon frontRightMotor;
    private final CANTalon backLeftMotor;
    private final CANTalon backRightMotor;
    private final MotorSafetyHelper FLsafety;
    private final MotorSafetyHelper FRsafety;
    private final MotorSafetyHelper BLsafety;
    private final MotorSafetyHelper BRsafety;

    // Speed modifier constants
    private static final double SPEED_SLOW_MODIFIER = 0.5;
    private static final double SPEED_TURBO_MODIFIER = 2.0;
    private static final double SPEED_MAX_MODIFIER = 0.8;
    private static final double SPEED_MAX_CHANGE = 0.15;

    // Speed to use for Strafe and Revolve Drive
    private static final double SPEED_STRAFE = 0.6;

    private static final double REVOLVE_LARGE_FRONT_RADIUS = 65;
    private static final double REVOLVE_LARGE_BACK_RADIUS = REVOLVE_LARGE_FRONT_RADIUS + RobotMap.LENGTH;
    private static final double REVOLVE_LARGE_BACK_SPEED = 0.5;
    private static final double REVOLVE_LARGE_FRONT_SPEED = REVOLVE_LARGE_BACK_SPEED * (REVOLVE_LARGE_FRONT_RADIUS / REVOLVE_LARGE_BACK_RADIUS);
    private static final double REVOLVE_LARGE_FRONT_ANGLE = (Math.atan((2 * REVOLVE_LARGE_FRONT_RADIUS) / RobotMap.WIDTH));
    private static final double REVOLVE_LARGE_BACK_ANGLE = (Math.atan((2 * REVOLVE_LARGE_BACK_RADIUS) / RobotMap.WIDTH));
    
    private static final double REVOLVE_SMALL_FRONT_RADIUS = 25;
    private static final double REVOLVE_SMALL_BACK_RADIUS = REVOLVE_SMALL_FRONT_RADIUS + RobotMap.LENGTH;
    private static final double REVOLVE_SMALL_BACK_SPEED = 0.5;
    private static final double REVOLVE_SMALL_FRONT_SPEED = REVOLVE_SMALL_BACK_SPEED * (REVOLVE_SMALL_FRONT_RADIUS / REVOLVE_SMALL_BACK_RADIUS);
    private static final double REVOLVE_SMALL_FRONT_ANGLE = (Math.atan((2 * REVOLVE_SMALL_FRONT_RADIUS) / RobotMap.WIDTH));
    private static final double REVOLVE_SMALL_BACK_ANGLE = (Math.atan((2 * REVOLVE_SMALL_BACK_RADIUS) / RobotMap.WIDTH));

    // Private constructor
    public SwerveDrive(CANTalon frontLeftMotor, CANTalon backLeftMotor, CANTalon frontRightMotor, CANTalon backRightMotor)
    {
        this.frontLeftMotor = frontLeftMotor;
        this.frontRightMotor = frontRightMotor;
        this.backLeftMotor = backLeftMotor;
        this.backRightMotor = backRightMotor;
        
        FLsafety = new MotorSafetyHelper(frontLeftMotor);
        FRsafety = new MotorSafetyHelper(frontRightMotor);
        BLsafety = new MotorSafetyHelper(backLeftMotor);
        BRsafety = new MotorSafetyHelper(backRightMotor);


        // Make objects
        data = DataStorage.getInstance();
        gyro = Gyro2015.getInstance();

        // Make steering array
        steering = new Steering[4];

        // Make all steering objects
        for (int i = 0; i < steering.length; i++)
        {
            // Read all steering values from saved robot data(Format = (<data key>, <backup value>))
            double steeringCenter = data.getDouble(RobotMap.STEERING_KEYS[i], RobotMap.STEERING_RANGE / 2);

            // Create Steering Object
            steering[i] = new Steering(RobotMap.PIDvalues[i], RobotMap.STEERING_MOTOR_CHANNELS[i],
                    RobotMap.STEERING_SENSOR_CHANNELS[i], steeringCenter);
        }
    }

//    /**
//     * Gets the single instance of this class.
//     * 
//     * @return The single instance.
//     */
//    public static Drive getInstance()
//    {
//        if (instance == null)
//        {
//            // First usage - create Drive object
//            CANTalon frontleft = new CANTalon(RobotMap.FRONT_LEFT_MOTOR_CHANNEL);
//            CANTalon backleft = new CANTalon(RobotMap.BACK_LEFT_MOTOR_CHANNEL);
//            CANTalon frontright = new CANTalon(RobotMap.FRONT_RIGHT_MOTOR_CHANNEL);
//            CANTalon backright = new CANTalon(RobotMap.BACK_RIGHT_MOTOR_CHANNEL);
//            instance = new Drive(frontleft, backleft, frontright, backright);
//        }
//        return instance;
//    }

    /**
     * Turns on the PID for all wheels.
     */
    public void enableSteeringPID()
    {
        for (int i = 0; i < steering.length; i++)
        {
            steering[i].enablePID();
        }
    }

    /**
     * Turns off the PID for all wheels.
     */
    public void disableSteeringPID()
    {
        for (int i = 0; i < steering.length; i++)
        {
            steering[i].disablePID();
        }
    }

    /**
     * Drives each of the four wheels at different speeds using invert constants
     * to account for wiring.
     *
     * @param frontLeftSpeed
     * @param frontRightSpeed
     * @param backLeftSpeed
     * @param backRightSpeed
     */
    private void fourWheelDrive(double frontLeftSpeed, double frontRightSpeed, double backLeftSpeed, double backRightSpeed)
    {
        // If any of the motors doesn't exist then exit
        if (backLeftMotor == null || backRightMotor == null || frontLeftMotor == null || frontRightMotor == null)
        {
            throw new NullPointerException("Null motor provided");
        }
        
        final double MAX_DRIVE_ANGLE = Math.PI / 25;
        
        // Don't drive until wheels are close to the commanded steering angle
        if (steering[RobotMap.FRONT_LEFT] .getAngleDelta() < MAX_DRIVE_ANGLE ||
            steering[RobotMap.FRONT_RIGHT].getAngleDelta() < MAX_DRIVE_ANGLE ||
            steering[RobotMap.BACK_LEFT]  .getAngleDelta() < MAX_DRIVE_ANGLE ||
            steering[RobotMap.BACK_RIGHT] .getAngleDelta() < MAX_DRIVE_ANGLE)
        {
            LOGGER.debug("DRIVE"); // TODO
            frontLeftMotor.set((FRONT_LEFT_DRIVE_INVERT ? -1 : 1) * limitSpeed((frontLeftSpeed * 1.2), RobotMap.FRONT_LEFT), SYNC_GROUP);
            frontRightMotor.set((FRONT_RIGHT_DRIVE_INVERT ? -1 : 1) * limitSpeed(frontRightSpeed, RobotMap.FRONT_RIGHT), SYNC_GROUP);
            backLeftMotor.set((BACK_LEFT_DRIVE_INVERT ? -1 : 1) * limitSpeed(backLeftSpeed, RobotMap.BACK_LEFT), SYNC_GROUP);
            backRightMotor.set((BACK_RIGHT_DRIVE_INVERT ? -1 : 1) * limitSpeed(backRightSpeed, RobotMap.BACK_RIGHT), SYNC_GROUP);
        } 
        else
        {
            LOGGER.debug("NO DRIVE");
            frontLeftMotor.set(0, SYNC_GROUP);
            frontRightMotor.set(0, SYNC_GROUP);
            backLeftMotor.set(0, SYNC_GROUP);
            backRightMotor.set(0, SYNC_GROUP);
        }

        LOGGER.debug("WHEEL SPEEDS: FL:" + frontLeftSpeed + ", FR:" +  frontRightSpeed + ", BL:" + backLeftSpeed + ", BR:" + backRightSpeed);
        feedMotors();
    }

    private void feedMotors()
    {
        if (FLsafety != null)
        {
            FLsafety.feed();
        }
        if (FRsafety != null)
        {
            FRsafety.feed();
        }
        if (BLsafety != null)
        {
            BLsafety.feed();
        }
        if (BRsafety != null)
        {
            BRsafety.feed();
        }
    }

    /**
     * @param frontLeft
     * @param frontRight
     * @param backLeft
     * @param backRight
     */
    private void fourWheelSteer(double frontLeft, double frontRight, double backLeft, double backRight)
    {
        // set the angles to steer
        steering[RobotMap.FRONT_LEFT].setAngle(frontLeft);
        steering[RobotMap.FRONT_RIGHT].setAngle(frontRight);
        steering[RobotMap.BACK_LEFT].setAngle(backLeft);
        steering[RobotMap.BACK_RIGHT].setAngle(backRight);
    }

    /**
     * Set angles in "turn in place" position
     * Wrap around will check whether the closest angle is facing forward or backward
     * 
     * Front Left- / \ - Front Right<br>
     * Back Left - \ / - Back Right
     * 
     * @param speed
     */
    @Override
    public void turnDrive(double speed)
    {
        LOGGER.info("Start turnDrive()");
        WheelCorrection frontLeft = wrapAroundCorrect(RobotMap.FRONT_LEFT, TURN_IN_PLACE_ANGLE, -speed);
        WheelCorrection frontRight = wrapAroundCorrect(RobotMap.FRONT_RIGHT, -TURN_IN_PLACE_ANGLE, speed);
        WheelCorrection backLeft = wrapAroundCorrect(RobotMap.BACK_LEFT, -TURN_IN_PLACE_ANGLE, -speed);
        WheelCorrection backRight = wrapAroundCorrect(RobotMap.BACK_RIGHT, TURN_IN_PLACE_ANGLE, speed);               
        LOGGER.info("Calculated wheel corrections"); 
        this.fourWheelSteer(frontLeft.angle, frontRight.angle, backLeft.angle, backRight.angle);
        this.fourWheelDrive(frontLeft.speed, frontRight.speed, backLeft.speed, backRight.speed);
        LOGGER.info("Done turnDrive()");
 }
    
    // Previous speeds for the four wheels
    private double lastSpeed[] = new double[]{0.0,0.0,0.0,0.0};

    /**
     * Limit the rate at which the robot can change speed once driving fast.
     * This is to prevent causing mechanical damage - or tipping the robot
     * through stopping too quickly.
     *
     * @param speed
     *            desired speed for robot
     * @return returns rate-limited speed
     */
    private double limitSpeed(double speed, int wheelID)
    {
        // Apply speed modifiers first

        if (DriverStation2015.getInstance().getSlow())
        {
            speed *= SPEED_SLOW_MODIFIER;
        }
        else if (DriverStation2015.getInstance().getTurbo())
        {
            speed *= SPEED_TURBO_MODIFIER;
        }
        else
        {
            // Limit maximum regular speed to specified Maximum.
            speed *= SPEED_MAX_MODIFIER;
        }

        // Limit the rate at which robot can change speed once driving over 0.6
        if (Math.abs(speed - lastSpeed[wheelID]) > SPEED_MAX_CHANGE && Math.abs(lastSpeed[wheelID]) > 0.6)
        {
            if (speed > lastSpeed[wheelID])
            {
                speed = lastSpeed[wheelID] + SPEED_MAX_CHANGE;
            }
            else
            {
                speed = lastSpeed[wheelID] - SPEED_MAX_CHANGE;
            }
        }
        lastSpeed[wheelID] = speed;
        LOGGER.debug("LIMIT SPEED: " + speed);
        return (speed);
    }

    /**
     * Field aligned drive. Assumes Gyro angle 0 is facing downfield
     * @param fieldAlign
     *            Whether or not to use field align drive
     */
    public void oneStickDrive(MainJoystick467 joystick, boolean fieldAlign)
    {
        crabDrive(joystick.getAngle(), joystick.getStickDistance(), fieldAlign);
    }

    @Override
    public void twoStickDrive(MainJoystick467 joystickLeft, RightJoystick467 joystickRight)
    {
        // TODO Auto-generated method stub
        LOGGER.debug("twoStickDrive leftSpeed=" + joystickLeft.getTankSpeed() +
                                " rightSpeed=" + joystickRight.getTankSpeed());
    }

    /**
     * Field aligned drive. Assumes Gyro angle 0 is facing downfield
     *
     * @param angle
     *            value corresponding to the field direction to move in
     * @param speed
     *            Speed to drive at
     * @param fieldAlign
     *            Whether or not to use field align drive
     */
    private void crabDrive(double angle, double speed, boolean fieldAlign)
    {
        double gyroAngle = (fieldAlign) ? gyro.getAngle() : 0; // if field aligned use gyro.getAngle(), else 0        

        double gyroAngleRad = Math.toRadians(gyroAngle);
        // Calculate the wheel angle necessary to drive in the required direction.
        double steeringAngle = (fieldAlign) ? (angle - gyroAngleRad) : angle;

        WheelCorrection corrected = wrapAroundCorrect(RobotMap.BACK_RIGHT, steeringAngle, speed);
        LOGGER.debug("ANGLE: " + corrected.angle + " SPEED: " + corrected.speed);
        fourWheelSteer(corrected.angle, corrected.angle, corrected.angle, corrected.angle);
        fourWheelDrive(corrected.speed, corrected.speed, corrected.speed, corrected.speed);
    }

    /**
     * Individually controls a specific steering motor
     *
     * @param angle
     *            Angle to drive to
     * @param speed
     *            Speed to drive at
     * @param steeringId
     *            Id of steering motor to drive
     */
    public void individualSteeringDrive(double angle, int steeringId)
    {
        // Set steering angle
        steering[steeringId].setAngle(angle);
    }

    /**
     * Does not drive drive motors and keeps steering angle at previous position.
     */
    public void stop()
    {
        LOGGER.debug("NO DRIVE CALLED");
        this.fourWheelDrive(0, 0, 0, 0);// no drive for you!
    }
    
    public void unwind()
    {
        for (Steering wheelpod : steering)
        {
            wheelpod.setAbsoluteAngle(0);
        }
    }

    /**
     * Drive left or right at a fixed speed.
     * 
     * @param direction
     * @param speed
     */
    public void strafeDrive(Direction direction)
    {
        double angle = 0.0;
        switch (direction)
        {
            case FRONT:
                // Not used, here to be thorough
                angle = 0.0;
                break;
            case LEFT:
                angle = -Math.PI / 2;
                break;
            case RIGHT:
                angle = Math.PI / 2;
                break;
            case BACK:
                angle = Math.PI;
                break;
        }
        
        double speed = SPEED_STRAFE;

        WheelCorrection corrected = wrapAroundCorrect(RobotMap.BACK_RIGHT, angle, speed);
        fourWheelSteer(corrected.angle, corrected.angle, corrected.angle, corrected.angle);
        fourWheelDrive(corrected.speed, corrected.speed, corrected.speed, corrected.speed);
    }

    /**
     * @param direction
     */
    public void revolveDriveLarge(Direction direction)
    {
        WheelCorrection flCorrected = wrapAroundCorrect(RobotMap.FRONT_LEFT, -REVOLVE_LARGE_FRONT_ANGLE, REVOLVE_LARGE_FRONT_SPEED);
        WheelCorrection frCorrected = wrapAroundCorrect(RobotMap.FRONT_RIGHT, REVOLVE_LARGE_FRONT_ANGLE, REVOLVE_LARGE_FRONT_SPEED);
        WheelCorrection blCorrected = wrapAroundCorrect(RobotMap.BACK_LEFT, -REVOLVE_LARGE_BACK_ANGLE, REVOLVE_LARGE_BACK_SPEED);
        WheelCorrection brCorrected = wrapAroundCorrect(RobotMap.BACK_RIGHT, REVOLVE_LARGE_BACK_ANGLE, REVOLVE_LARGE_BACK_SPEED);

        if (direction == Direction.RIGHT)
        {
            fourWheelDrive(-flCorrected.speed, frCorrected.speed, -blCorrected.speed, brCorrected.speed);
        }
        else
        {
            fourWheelDrive(flCorrected.speed, -frCorrected.speed, blCorrected.speed, -brCorrected.speed);
        }
        fourWheelSteer(flCorrected.angle, frCorrected.angle, blCorrected.angle, brCorrected.angle);
    }

    /**
     * @param direction
     */
    public void revolveDriveSmall(Direction direction)
    {
        WheelCorrection flCorrected = wrapAroundCorrect(RobotMap.FRONT_LEFT, -REVOLVE_SMALL_FRONT_ANGLE, REVOLVE_SMALL_FRONT_SPEED);
        WheelCorrection frCorrected = wrapAroundCorrect(RobotMap.FRONT_RIGHT, REVOLVE_SMALL_FRONT_ANGLE, REVOLVE_SMALL_FRONT_SPEED);
        WheelCorrection blCorrected = wrapAroundCorrect(RobotMap.BACK_LEFT, -REVOLVE_SMALL_BACK_ANGLE, REVOLVE_SMALL_BACK_SPEED);
        WheelCorrection brCorrected = wrapAroundCorrect(RobotMap.BACK_RIGHT, REVOLVE_SMALL_BACK_ANGLE, REVOLVE_SMALL_BACK_SPEED);

        if (direction == Direction.RIGHT)
        {
            fourWheelDrive(-flCorrected.speed, frCorrected.speed, -blCorrected.speed, brCorrected.speed);
        }
        else
        {
            fourWheelDrive(flCorrected.speed, -frCorrected.speed, blCorrected.speed, -brCorrected.speed);
        }
        fourWheelSteer(flCorrected.angle, frCorrected.angle, blCorrected.angle, brCorrected.angle);
    }
    
    /**
     * Individually controls a specific driving motor
     *
     * @param speed
     *            Speed to drive at
     * @param steeringId
     *            Id of driving motor to drive
     */
    public void individualWheelDrive(double speed, int steeringId)
    {
        double frontLeftSpeed = 0.0;
        double frontRightSpeed = 0.0;
        double rearLeftSpeed = 0.0;
        double rearRightSpeed = 0.0;

        switch (steeringId)
        {
            case RobotMap.FRONT_LEFT:
                frontLeftSpeed = 1.0;
                break;
            case RobotMap.FRONT_RIGHT:
                frontRightSpeed = speed * 1.0;
                break;
            case RobotMap.BACK_LEFT:
                rearLeftSpeed = speed * 1.0;
                break;
            case RobotMap.BACK_RIGHT:
                rearRightSpeed = speed * 1.0;
                break;
        }

        fourWheelDrive(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
    }

    /**
     * Function to determine the wrapped around difference from the joystick
     * angle to the steering angle.
     *
     * @param value1
     *            - The first angle to check against
     * @param value2
     *            - The second angle to check against
     * @return The normalized wrap around difference
     */
    static double wrapAroundDifference(double value1, double value2)
    {
        double diff = Math.abs(value1 - value2) % (2 * Math.PI);
        while (diff > Math.PI)
        {
            diff = (2.0 * Math.PI) - diff;
        }
        LOGGER.debug(String.format("wrapAroundDifference v1=%f v2=%f diff=%f", value1, value2, diff));
        return diff;
    }

    /**
     * Only used for steering
     * 
     * @param steeringIndex
     *            - which wheel pod
     * @param targetAngle
     *            - in radians
     * @param targetSpeed
     * @return corrected
     */
    private WheelCorrection wrapAroundCorrect(int steeringIndex, double targetAngle, double targetSpeed)
    {
        WheelCorrection corrected = new WheelCorrection(targetAngle, targetSpeed);

        double normalizedSteeringAngle = steering[steeringIndex].getSteeringAngle() % (Math.PI * 2);
        if (wrapAroundDifference(normalizedSteeringAngle, targetAngle) > Math.PI / 2)
        {
            // shortest path to desired angle is to reverse speed and adjust angle -PI
            corrected.speed *= -1;

            corrected.angle -= Math.PI;
            LOGGER.debug(corrected);
        }
        return corrected;
    }

    /**
     * Set the steering center to a new value
     *
     * @param steeringMotor
     *            The id of the steering motor (0 = FL, 1 = FR, 2 = BL, 3 = BR)
     * @param value
     *            The new center value
     */
    public void setSteeringCenter(int steeringMotor, double value)
    {
        steering[steeringMotor].setCenter(value);
    }

    /**
     * Get the steering angle of the corresponding steering motor
     *
     * @param steeringId
     *            The id of the steering motor
     * @return
     */
    public double getSteeringAngle(int steeringMotor)
    {
        return steering[steeringMotor].getSensorValue();
    }

    /**
     * Get the normalized steering angle of the corresponding steering motor
     *
     * @param steeringId
     *            The id of the steering motor
     * @return
     */
    public double getNormalizedSteeringAngle(int steeringMotor)
    {
        return steering[steeringMotor].getSteeringAngle();
    }

    @Override
    public void arcadeDrive( double turn, double speed)
    {
        // TODO Auto-generated method stub
    }

}

class WheelCorrection
{
    public double speed;
    public double angle;

    public WheelCorrection(double angleIn, double speedIn)
    {
        angle = angleIn;
        speed = speedIn;
    }

    @Override
    public String toString()
    {
        return "WheelCorrection [speed=" + speed + ", angle=" + angle + "]";
    }
}
