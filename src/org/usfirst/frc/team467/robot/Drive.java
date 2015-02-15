/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.*;

/**
 * @author Team 467
 */
public class Drive extends RobotDrive
{
    private static final Logger LOGGER = Logger.getLogger(Drive.class);

    // Single instance of this class
    private static Drive instance = null;

    // Steering objects
    public Steering[] steering;

    // Data storage object
    private DataStorage data;

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

    // Speed modifier constants
    private static final double SPEED_SLOW_MODIFIER = 0.5;
    private static final double SPEED_TURBO_MODIFIER = 2.0;
    private static final double SPEED_MAX_MODIFIER = 0.8;
    private static final double SPEED_MAX_CHANGE = 0.2;

    // Speed to use for Strafe and Revolve Drive
    private static final double SPEED_STRAFE = 0.4;

    private static final double REVOLVE_FRONT_RADIUS = 65;
    private static final double REVOLVE_BACK_RADIUS = REVOLVE_FRONT_RADIUS + RobotMap.LENGTH;
    private static final double REVOLVE_BACK_SPEED = 0.4;
    private static final double REVOLVE_FRONT_SPEED = REVOLVE_BACK_SPEED * (REVOLVE_FRONT_RADIUS / REVOLVE_BACK_RADIUS);
    private static final double REVOLVE_FRONT_ANGLE = (Math.atan((2 * REVOLVE_FRONT_RADIUS) / RobotMap.WIDTH));
    private static final double REVOLVE_BACK_ANGLE = (Math.atan((2 * REVOLVE_BACK_RADIUS) / RobotMap.WIDTH));

    // Private constructor
    private Drive(CANTalon frontLeftMotor, CANTalon backLeftMotor, CANTalon frontRightMotor, CANTalon backRightMotor)
    {
        super(frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor);

        // Make objects
        data = DataStorage.getInstance();

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

    /**
     * Gets the single instance of this class.
     * 
     * @return The single instance.
     */
    public static Drive getInstance()
    {
        if (instance == null)
        {
            // First usage - create Drive object
            CANTalon frontleft = new CANTalon(RobotMap.FRONT_LEFT_MOTOR_CHANNEL);
            CANTalon backleft = new CANTalon(RobotMap.BACK_LEFT_MOTOR_CHANNEL);
            CANTalon frontright = new CANTalon(RobotMap.FRONT_RIGHT_MOTOR_CHANNEL);
            CANTalon backright = new CANTalon(RobotMap.BACK_RIGHT_MOTOR_CHANNEL);
            instance = new Drive(frontleft, backleft, frontright, backright);
        }
        return instance;
    }

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
        if (m_rearLeftMotor == null || m_rearRightMotor == null || m_frontLeftMotor == null || m_rearLeftMotor == null)
        {
            throw new NullPointerException("Null motor provided");
        }
        m_frontLeftMotor.set((FRONT_LEFT_DRIVE_INVERT ? -1 : 1) * limitSpeed(frontLeftSpeed), SYNC_GROUP);
        m_frontRightMotor.set((FRONT_RIGHT_DRIVE_INVERT ? -1 : 1) * limitSpeed(frontRightSpeed), SYNC_GROUP);
        m_rearLeftMotor.set((BACK_LEFT_DRIVE_INVERT ? -1 : 1) * limitSpeed(backLeftSpeed), SYNC_GROUP);
        m_rearRightMotor.set((BACK_RIGHT_DRIVE_INVERT ? -1 : 1) * limitSpeed(backRightSpeed), SYNC_GROUP);

//        m_frontLeftMotor.set(0, SYNC_GROUP);
//        m_frontRightMotor.set(0, SYNC_GROUP);
//        m_rearLeftMotor.set(0, SYNC_GROUP);
//        m_rearRightMotor.set(0, SYNC_GROUP);

        if (m_safetyHelper != null)
        {
            m_safetyHelper.feed();
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
//        steering[RobotMap.FRONT_LEFT].setAngle(frontLeft);
//        steering[RobotMap.FRONT_RIGHT].setAngle(frontRight);
//        steering[RobotMap.BACK_LEFT].setAngle(backLeft);
//        steering[RobotMap.BACK_RIGHT].setAngle(backRight);

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
    public void turnDriveOld(double speed)
    {
        if (wrapAroundDifference(TURN_IN_PLACE_ANGLE, steering[RobotMap.FRONT_RIGHT].getSteeringAngle()) <= Math.PI / 2)
        {
            // log "turnDriveA"
            LOGGER.debug("turnDriveA");
            // Front facing angles
            fourWheelSteer(TURN_IN_PLACE_ANGLE, -TURN_IN_PLACE_ANGLE, -TURN_IN_PLACE_ANGLE, TURN_IN_PLACE_ANGLE);
        }
        else
        {
            // log "turnDriveB"
            LOGGER.debug("turnDriveB");
            // Rear facing angles
            fourWheelSteer(TURN_IN_PLACE_ANGLE - Math.PI, -TURN_IN_PLACE_ANGLE + Math.PI, -TURN_IN_PLACE_ANGLE + Math.PI, TURN_IN_PLACE_ANGLE - Math.PI);

            // Reverse direction
            speed = -speed;
        }

        // Drive motors with left side motors inverted
        this.drive(limitSpeed(speed), new boolean[]
        {
                true,
                false,
                true,
                false
        });
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
    public void turnDrive(double speed)
    {
        WheelCorrection frontLeft = wrapAroundCorrect(RobotMap.FRONT_LEFT, TURN_IN_PLACE_ANGLE, speed);
        WheelCorrection frontRight = wrapAroundCorrect(RobotMap.FRONT_RIGHT, -TURN_IN_PLACE_ANGLE, speed);
        WheelCorrection backLeft = wrapAroundCorrect(RobotMap.BACK_LEFT, -TURN_IN_PLACE_ANGLE, speed);
        WheelCorrection backRight = wrapAroundCorrect(RobotMap.BACK_RIGHT, TURN_IN_PLACE_ANGLE, speed);
        
        this.fourWheelSteer(frontLeft.angle, frontRight.angle, backLeft.angle, backRight.angle);
        this.fourWheelDrive(-frontLeft.speed, frontRight.speed, -backLeft.speed, backRight.speed);
    }

    private double lastSpeed = 0.0;

    /**
     * Limit the rate at which the robot can change speed once driving fast.
     * This is to prevent causing mechanical damage - or tipping the robot
     * through stopping too quickly.
     *
     * @param speed
     *            desired speed for robot
     * @return returns rate-limited speed
     */
    private double limitSpeed(double speed)
    {
        // Apply speed modifiers first

        if (DriverStation467.getInstance().getSlow())
        {
            speed *= SPEED_SLOW_MODIFIER;
        }
        else if (DriverStation467.getInstance().getTurbo())
        {
            speed *= SPEED_TURBO_MODIFIER;
        }
        else
        {
            // Limit maximum regular speed to 80%.
            speed *= SPEED_MAX_MODIFIER;
        }

        // Limit the rate at which robot can change speed once driving over 0.6
        if (Math.abs(speed - lastSpeed) > SPEED_MAX_CHANGE && Math.abs(lastSpeed) > 0.6)
        {
            if (speed > lastSpeed)
            {
                speed = lastSpeed + SPEED_MAX_CHANGE;
            }
            else
            {
                speed = lastSpeed - SPEED_MAX_CHANGE;
            }
        }
        lastSpeed = speed;
        return (speed);
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
    public void crabDrive(double angle, double speed, boolean fieldAlign)
    {
        double gyroAngle = 0; // if gyro exists use gyro.getAngle()

        // Calculate the wheel angle necessary to drive in the required direction.
        double steeringAngle = (fieldAlign) ? angle - gyroAngle / (2 * Math.PI) : angle;

        WheelCorrection corrected = wrapAroundCorrect(RobotMap.BACK_RIGHT, steeringAngle, speed);

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
    public void individualSteeringDrive(double angle, double speed, int steeringId)
    {
        // Set steering angle
        steering[steeringId].setAngle(angle);

        this.drive(limitSpeed(speed), null);
    }

    /**
     * Drive left or right at a fixed speed.
     * 
     * @param direction
     * @param speed
     */
    public void strafeDrive(Direction direction)
    {
        double angle = (direction == Direction.RIGHT) ? Math.PI / 2 : -Math.PI / 2;
        double speed = SPEED_STRAFE;

        WheelCorrection corrected = wrapAroundCorrect(RobotMap.BACK_RIGHT, angle, speed);
        fourWheelSteer(corrected.angle, corrected.angle, corrected.angle, corrected.angle);
        fourWheelDrive(corrected.speed, corrected.speed, corrected.speed, corrected.speed);
    }

    /**
     * @param direction
     */
    public void revolveDrive(Direction direction)
    {
        WheelCorrection flCorrected = wrapAroundCorrect(RobotMap.FRONT_LEFT, -REVOLVE_FRONT_ANGLE, REVOLVE_FRONT_SPEED);
        WheelCorrection frCorrected = wrapAroundCorrect(RobotMap.FRONT_RIGHT, REVOLVE_FRONT_ANGLE, REVOLVE_FRONT_SPEED);
        WheelCorrection blCorrected = wrapAroundCorrect(RobotMap.BACK_LEFT, -REVOLVE_BACK_ANGLE, REVOLVE_BACK_SPEED);
        WheelCorrection brCorrected = wrapAroundCorrect(RobotMap.BACK_RIGHT, REVOLVE_BACK_ANGLE, REVOLVE_BACK_SPEED);

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
                frontLeftSpeed = speed * -1.0;
                break;
            case RobotMap.FRONT_RIGHT:
                frontRightSpeed = speed * 1.0;
                break;
            case RobotMap.BACK_LEFT:
                rearLeftSpeed = speed * -1.0;
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
     * New drive function. Allows for wheel correction using speed based on a
     * specified correction angle
     *
     * @param speed
     *            The speed to drive at
     * @param inverts
     *            Array of which motors to invert in form {FL, FR, BL, BR}
     */
    public void drive(double speed, boolean[] inverts)
    {
        double frontLeftSpeed = speed;
        double frontRightSpeed = speed;
        double rearLeftSpeed = speed;
        double rearRightSpeed = speed;

        // If the inverts parameter is fed in, invert the specified motors

        if (inverts != null)
        {
            frontLeftSpeed *= inverts[0] ? -1.0 : 1.0;
            frontRightSpeed *= inverts[1] ? -1.0 : 1.0;
            rearLeftSpeed *= inverts[2] ? -1.0 : 1.0;
            rearRightSpeed *= inverts[3] ? -1.0 : 1.0;
        }

        fourWheelDrive(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
    }

    /**
     * Stops the motors
     */
    public void stop()
    {
        drive(0, null);
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
    public double getSteeringAngle(int steeringId)
    {
        return steering[steeringId].getSensorValue();
    }

    /**
     * Get the normalized steering angle of the corresponding steering motor
     *
     * @param steeringId
     *            The id of the steering motor
     * @return
     */
    public double getNormalizedSteeringAngle(int steeringId)
    {
        return steering[steeringId].getSteeringAngle();
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
