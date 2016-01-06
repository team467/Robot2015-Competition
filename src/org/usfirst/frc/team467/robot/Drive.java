/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

/**
 * 
 */
public class Drive
{
    private static final Logger LOGGER = Logger.getLogger(Drive.class);

    // Single instance of this class
    private static Drive instance = null;
    
    public WheelPod frontLeft;
    public WheelPod frontRight;
    public WheelPod backLeft;
    public WheelPod backRight;

    // Data storage object
    private static DataStorage data;
    private Gyro2015 gyro;

    // Angle to turn at when rotating in place - initialized in constructor
    // takes the arctan of width over length in radians
    // Length is the wide side
    private static final double TURN_IN_PLACE_ANGLE = Math.atan(RobotMap.LENGTH / RobotMap.WIDTH);

    // Invert the drive motors to allow for wiring.
    private static final boolean FRONT_LEFT_DRIVE_INVERT = false;
    private static final boolean FRONT_RIGHT_DRIVE_INVERT = true;
    private static final boolean BACK_LEFT_DRIVE_INVERT = false;
    private static final boolean BACK_RIGHT_DRIVE_INVERT = true;

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
    private Drive(WheelPod frontLeft, WheelPod backLeft, WheelPod frontRight, WheelPod backRight)
    {
//        super(frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor);

        // Make objects
        gyro = Gyro2015.getInstance();

        this.frontLeft = frontLeft;
        this.frontRight = frontRight;
        this.backLeft = backLeft;
        this.backRight = backRight;
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
            data = DataStorage.getInstance();
            
            double steeringCenterFL = data.getDouble(RobotMap.STEERING_KEYS[0], RobotMap.STEERING_RANGE / 2);
            double steeringCenterFR = data.getDouble(RobotMap.STEERING_KEYS[1], RobotMap.STEERING_RANGE / 2);
            double steeringCenterBL = data.getDouble(RobotMap.STEERING_KEYS[2], RobotMap.STEERING_RANGE / 2);
            double steeringCenterBR = data.getDouble(RobotMap.STEERING_KEYS[3], RobotMap.STEERING_RANGE / 2);

            // First usage - create Drive object
            WheelPod frontleft = new WheelPod("FL", RobotMap.FRONT_LEFT_MOTOR_CHANNEL,
                    RobotMap.FRONT_LEFT_STEERING_MOTOR_CHANNEL,
                    RobotMap.FRONT_LEFT_STEERING_SENSOR_CHANNEL,
                    RobotMap.PIDvalues[0], steeringCenterFL, FRONT_LEFT_DRIVE_INVERT);
            WheelPod frontright = new WheelPod("FR", RobotMap.FRONT_RIGHT_MOTOR_CHANNEL,
                    RobotMap.FRONT_RIGHT_STEERING_MOTOR_CHANNEL,
                    RobotMap.FRONT_RIGHT_STEERING_SENSOR_CHANNEL,
                    RobotMap.PIDvalues[1], steeringCenterFR, FRONT_RIGHT_DRIVE_INVERT);
            WheelPod backleft = new WheelPod("BL", RobotMap.BACK_LEFT_MOTOR_CHANNEL,
                    RobotMap.BACK_LEFT_STEERING_MOTOR_CHANNEL,
                    RobotMap.BACK_LEFT_STEERING_SENSOR_CHANNEL,
                    RobotMap.PIDvalues[2], steeringCenterBL, BACK_LEFT_DRIVE_INVERT);
            WheelPod backright = new WheelPod("BR", RobotMap.BACK_RIGHT_MOTOR_CHANNEL,
                    RobotMap.BACK_RIGHT_STEERING_MOTOR_CHANNEL,
                    RobotMap.BACK_RIGHT_STEERING_SENSOR_CHANNEL,
                    RobotMap.PIDvalues[2], steeringCenterBR, BACK_RIGHT_DRIVE_INVERT);
            instance = new Drive(frontleft, backleft, frontright, backright);
        }
        return instance;
    }
    
    public WheelPod getWheelByIndex(int index)
    {
        switch (index)
        {
            case RobotMap.FRONT_LEFT:
                return frontLeft;
            case RobotMap.FRONT_RIGHT:
                return frontRight;
            case RobotMap.BACK_LEFT:
                return backLeft;
            case RobotMap.BACK_RIGHT:
                return backRight;
            default:
                LOGGER.error("INVALID INDEX");
                return null;
        }
    }
    
    public int getWheelPodIndex(WheelPod pod)
    {
        if (pod == frontLeft) return RobotMap.FRONT_LEFT;
        if (pod == frontRight) return RobotMap.FRONT_RIGHT;
        if (pod == backLeft) return RobotMap.BACK_LEFT;
        if (pod == backRight) return RobotMap.BACK_RIGHT;
        LOGGER.error("Couldn't get index: Invalid Pod");
        return -1;

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
        frontLeft.drive(Vector.makeSpeedAngle(-speed, TURN_IN_PLACE_ANGLE));
        frontRight.drive(Vector.makeSpeedAngle(speed, -TURN_IN_PLACE_ANGLE));
        backLeft.drive(Vector.makeSpeedAngle(-speed, -TURN_IN_PLACE_ANGLE));
        backRight.drive(Vector.makeSpeedAngle(speed, TURN_IN_PLACE_ANGLE));
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
        if (fieldAlign)
            LOGGER.debug("Field Aligned");
        double gyroAngle = (fieldAlign) ? gyro.getAngle() : 0; // if field aligned use gyro.getAngle(), else 0        

        double gyroAngleRad = Math.toRadians(gyroAngle);
        // Calculate the wheel angle necessary to drive in the required direction.
        double steeringAngle = (fieldAlign) ? (angle - gyroAngleRad) : angle;

        LOGGER.debug("ANGLE: " + steeringAngle + " SPEED: " + speed);
        
        frontLeft.drive(Vector.makeSpeedAngle(speed, steeringAngle));
        frontRight.drive(Vector.makeSpeedAngle(speed, steeringAngle));
        backLeft.drive(Vector.makeSpeedAngle(speed, steeringAngle));
        backRight.drive(Vector.makeSpeedAngle(speed, steeringAngle));
    }
    
    public void vectorDrive(double angle, double speed, double turnSpeed)
    {
        double gyroAngle = gyro.getAngle();       

        double gyroAngleRad = Math.toRadians(gyroAngle);
        // Calculate the wheel angle necessary to drive in the required direction.
        double fieldAngle = angle - gyroAngleRad;
        Vector straightVector = Vector.makeSpeedAngle(speed, fieldAngle);
        
        frontLeft.drive(Vector.average(straightVector, Vector.makeSpeedAngle(-turnSpeed, TURN_IN_PLACE_ANGLE)));
        frontRight.drive(Vector.average(straightVector, Vector.makeSpeedAngle(turnSpeed, -TURN_IN_PLACE_ANGLE)));
        backLeft.drive(Vector.average(straightVector, Vector.makeSpeedAngle(-turnSpeed, -TURN_IN_PLACE_ANGLE)));
        backRight.drive(Vector.average(straightVector, Vector.makeSpeedAngle(turnSpeed, TURN_IN_PLACE_ANGLE)));
    }

    /**
     * Does not drive drive motors and keeps steering angle at previous position.
     */
    public void stop()
    {
        LOGGER.debug("NO DRIVE CALLED"); // no drive for you!
        frontLeft.stopMotor();
        frontRight.stopMotor();
        backLeft.stopMotor();
        backRight.stopMotor();
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
        
        Vector v = Vector.makeSpeedAngle(SPEED_STRAFE, angle);
        
        frontLeft.drive(v);
        frontRight.drive(v);
        backLeft.drive(v);
        backRight.drive(v);
    }

    /**
     * @param direction
     */
    public void revolveDriveLarge(Direction direction)
    {        
        // TODO Add back speed
        double speed = (direction == Direction.LEFT) ? REVOLVE_LARGE_FRONT_SPEED : -REVOLVE_LARGE_FRONT_SPEED;
        frontLeft.drive(Vector.makeSpeedAngle(speed, -REVOLVE_LARGE_FRONT_ANGLE));
        frontRight.drive(Vector.makeSpeedAngle(-speed, REVOLVE_LARGE_FRONT_ANGLE));
        backLeft.drive(Vector.makeSpeedAngle(speed, -REVOLVE_LARGE_BACK_ANGLE));
        backRight.drive(Vector.makeSpeedAngle(-speed, REVOLVE_LARGE_BACK_ANGLE));  
    }

    /**
     * @param direction
     */
    public void revolveDriveSmall(Direction direction)
    {
        // TODO Add back speed
        double speed = (direction == Direction.LEFT) ? REVOLVE_SMALL_FRONT_SPEED : -REVOLVE_SMALL_FRONT_SPEED;
        frontLeft.drive(Vector.makeSpeedAngle(speed, -REVOLVE_SMALL_FRONT_ANGLE));
        frontRight.drive(Vector.makeSpeedAngle(-speed, REVOLVE_SMALL_FRONT_ANGLE));
        backLeft.drive(Vector.makeSpeedAngle(speed, -REVOLVE_SMALL_BACK_ANGLE));
        backRight.drive(Vector.makeSpeedAngle(-speed, REVOLVE_SMALL_BACK_ANGLE));
    }

    /**
     * Set the steering center to a new value
     *
     * @param pod
     *            The wheelpod of the steering motor (0 = FL, 1 = FR, 2 = BL, 3 = BR)
     * @param value
     *            The new center value
     */
    public void setSteeringCenter(WheelPod pod, double value)
    {
        pod.getSteering().setCenter(value);
    }

    /**
     * Get the steering angle of the corresponding steering motor
     *
     * @param steeringId
     *            The id of the steering motor
     * @return
     */
    public double getSteeringAngle(WheelPod pod)
    {
        return pod.getSteering().getSensorValue();
    }

    /**
     * Get the normalized steering angle of the corresponding steering motor
     *
     * @param steeringId
     *            The id of the steering motor
     * @return
     */
    public double getNormalizedSteeringAngle(WheelPod pod)
    {
        return pod.getSteering().getSteeringAngle();
    }

}
