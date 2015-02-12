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

	//Single instance of this class
    private static Drive instance = null;

    //Steering objects
    public Steering[] steering;

    //Data storage object
    private DataStorage data;

    // Angle to turn at when rotating in place - initialized in constructor
    private static double turnAngle;

    // Magic number copied from WPI code
    private static final byte SYNC_GROUP = (byte) 0x80;

    // Invert the drive motors to allow for wiring.
    private static final boolean FRONT_LEFT_DRIVE_INVERT = false;
    private static final boolean FRONT_RIGHT_DRIVE_INVERT = true;
    private static final boolean BACK_LEFT_DRIVE_INVERT = false;
    private static final boolean BACK_RIGHT_DRIVE_INVERT = true;

    // Speed modifier constants
    private static final double SPEED_SLOW_MODIFIER = 1.0/3.0;
    private static final double SPEED_TURBO_MODIFIER = 2.0;
    private static final double SPEED_MAX_MODIFIER = 0.8;
    private static final double SPEED_MAX_CHANGE = 0.2;
    
    // Speed to use for Strafe and Revolve Drive
    private static final double SPEED_STRAFE = 0.4;
    
    // Private constructor
    private Drive(Talon frontLeftMotor, Talon backLeftMotor,
            	  Talon frontRightMotor, Talon backRightMotor)
    {
        super(frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor);

        // Make objects
        data = DataStorage.getInstance();
        
        // takes the arctan of width over length in radians
        // Length is the wide side
        turnAngle = Math.atan(RobotMap.LENGTH / RobotMap.WIDTH);

        // Make steering array
        steering = new Steering[4];

        // Make all steering objects
        for (int i = 0; i < steering.length; i++)
        {
            // Read all steering values from saved robot data(Format = (<data key>, <backup value>))
            double steeringCenter = data.getDouble(RobotMap.STEERING_KEYS[i], 0.0);
            
            // Create Steering Object
            steering[i] = new Steering(RobotMap.PIDvalues[i],
                    RobotMap.STEERING_MOTOR_CHANNELS[i],
                    RobotMap.STEERING_SENSOR_CHANNELS[i],
                    steeringCenter);
        }
    }

    /**
     * Gets the single instance of this class.
     * @return The single instance.
     */
    public static Drive getInstance()
    {
        if (instance == null)
        {
        	// First usage - create Drive object
            Talon frontleft = new Talon(RobotMap.FRONT_LEFT_MOTOR_CHANNEL);
            Talon backleft = new Talon(RobotMap.BACK_LEFT_MOTOR_CHANNEL);
            Talon frontright = new Talon(RobotMap.FRONT_RIGHT_MOTOR_CHANNEL);
            Talon backright = new Talon(RobotMap.BACK_RIGHT_MOTOR_CHANNEL);
            instance = new Drive(frontleft, backleft, frontright, backright);
        }
        return instance;
    }
    
    /**
     * Turns on the PID for all wheels.
     */
    public void enableSteeringPID()
    {
        for(int i = 0; i < steering.length ; i++)
        {
            steering[i].enablePID();
        }
    }
    
    /**
     * Turns off the PID for all wheels.
     */
    public void disableSteeringPID()
    {
        for(int i = 0; i < steering.length ; i++)
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
    private void fourWheelDrive(double frontLeftSpeed, double frontRightSpeed,
                               double backLeftSpeed, double backRightSpeed)
    {
        // If any of the motors doesn't exist then exit
        if (m_rearLeftMotor == null || m_rearRightMotor == null
                || m_frontLeftMotor == null || m_rearLeftMotor == null)
        {
            throw new NullPointerException("Null motor provided");
        }
//        m_frontLeftMotor.set((FRONT_LEFT_DRIVE_INVERT ? -1 : 1) * limitSpeed(frontLeftSpeed), SYNC_GROUP);
//        m_frontRightMotor.set((FRONT_RIGHT_DRIVE_INVERT ? -1 : 1) * limitSpeed(frontRightSpeed), SYNC_GROUP);
//        m_rearLeftMotor.set((BACK_LEFT_DRIVE_INVERT ? -1 : 1) * limitSpeed(backLeftSpeed), SYNC_GROUP);
//        m_rearRightMotor.set((BACK_RIGHT_DRIVE_INVERT ? -1 : 1) * limitSpeed(backRightSpeed), SYNC_GROUP);

        m_frontLeftMotor.set(0, SYNC_GROUP);
        m_frontRightMotor.set(0, SYNC_GROUP);
        m_rearLeftMotor.set(0, SYNC_GROUP);
        m_rearRightMotor.set(0, SYNC_GROUP);

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
        //set the angles to steer
//        steering[RobotMap.FRONT_LEFT].setAngle(frontLeft);
//        steering[RobotMap.FRONT_RIGHT].setAngle(frontRight);
//        steering[RobotMap.BACK_LEFT].setAngle(backLeft);
//        steering[RobotMap.BACK_RIGHT].setAngle(backRight);
        
        steering[RobotMap.FRONT_LEFT].setAngle(0);
        steering[RobotMap.FRONT_RIGHT].setAngle(frontRight);
        steering[RobotMap.BACK_LEFT].setAngle(0);
        steering[RobotMap.BACK_RIGHT].setAngle(0);
    }

    /**
     * When we need to update without changing the steering
     */
    private void fourWheelNoSteer()
    {
		LOGGER.debug("fourWheelNoSteer value=" + steering[RobotMap.FRONT_RIGHT].getSensorValue());
		LOGGER.trace("fourWheelNoSteer angle=" + steering[RobotMap.FRONT_RIGHT].getSteeringAngle());
    	for (Steering wheelpod : steering) {
    		// TODO Figure out negative angle.
    		wheelpod.setAngle(-wheelpod.getSteeringAngle());
    	}
    }

//    /**
//     * Get the Talon drive motor object for the specified motor (use RobotMap
//     * constants)
//     *
//     * @param motor The motor to get
//     * @return One of the four Talon drive motors
//     */
//    private Talon getDriveMotor(int motor)
//    {
//        Talon returnMotor;
//        switch (motor)
//        {
//            case RobotMap.FRONT_LEFT:
//                returnMotor = (Talon) m_frontLeftMotor;
//                break;
//            case RobotMap.FRONT_RIGHT:
//                returnMotor = (Talon) m_frontRightMotor;
//                break;
//            case RobotMap.BACK_LEFT:
//                returnMotor = (Talon) m_rearLeftMotor;
//                break;
//            case RobotMap.BACK_RIGHT:
//                returnMotor = (Talon) m_rearRightMotor;
//                break;
//            default:
//                returnMotor = null;
//        }
//        return returnMotor;
//    }
    
    /**
     * @param speed
     */
    public void turnDrive(double speed)
    {
        // Set angles in "turn in place" position
        // Wrap around will check whether the closest angle is facing forward or backward
        //  
        //  Front Left- / \ - Front Right
        //
        //  Back Left - \ / - Back Right
        //  
        if (wrapAroundDifference(turnAngle, steering[RobotMap.FRONT_LEFT].getSteeringAngle()) <= Math.PI/2)
        {
            // Front facing angles
            fourWheelSteer(turnAngle, -turnAngle, -turnAngle, turnAngle);
        }
        else
        {
            // Rear facing angles
            fourWheelSteer(turnAngle - Math.PI, -turnAngle + Math.PI, 
            			  -turnAngle + Math.PI, turnAngle - Math.PI);

            // Reverse direction
            speed = -speed;
        }

        // Drive motors with left side motors inverted
        this.drive(limitSpeed(speed), new boolean[]
        {
            true, false, true, false
        });
    }

    private double lastSpeed = 0.0;

    /**
     * Limit the rate at which the robot can change speed once driving fast.
     * This is to prevent causing mechanical damage - or tipping the robot
     * through stopping too quickly.
     *
     * @param speed desired speed for robot
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
     * @param angle value corresponding to the field direction to move in
     * @param speed Speed to drive at
     * @param fieldAlign Whether or not to use field align drive
     */
    public void crabDrive(double angle, double speed, boolean fieldAlign)
    {
    	// If joystick is near center, don't move or steer. We're done.
    	if (speed < 0.1) {
    		LOGGER.debug("speed=" + speed);
    		fourWheelNoSteer();
            fourWheelDrive(0.0, 0.0, 0.0, 0.0);
            return;
    	}
    	
    	double gyroAngle = 0;  // if gyro exists use gyro.getAngle()

        // Calculate the wheel angle necessary to drive in the required direction.
        double steeringAngle = (fieldAlign) ? angle - gyroAngle / (2 * Math.PI) : angle;
        
        
        WheelCorrection corrected = wrapAroundCorrect(RobotMap.FRONT_RIGHT, steeringAngle, speed);

        fourWheelSteer(corrected.angle, corrected.angle, corrected.angle, corrected.angle);
        fourWheelDrive(corrected.speed, corrected.speed, corrected.speed, corrected.speed);
    }
    
//    /**
//     * 
//     * @param frontLeftSpeed
//     * @param frontRightSpeed
//     * @param backLeftSpeed
//     * @param backRightSpeed
//     * @param frontLeftAngle
//     * @param frontRightAngle
//     * @param backLeftAngle
//     * @param backRightAngle
//     */
//    public void wrapAroundDrive(double frontLeftSpeed, double frontRightSpeed,
//            double backLeftSpeed, double backRightSpeed,
//            double frontLeftAngle, double frontRightAngle,
//            double backLeftAngle, double backRightAngle)
//    {
//        WheelCorrection frontLeft = wrapAroundCorrect(RobotMap.FRONT_LEFT, frontLeftAngle, frontLeftSpeed);
//        WheelCorrection frontRight = wrapAroundCorrect(RobotMap.FRONT_RIGHT, frontRightAngle, frontRightSpeed);
//        WheelCorrection backLeft = wrapAroundCorrect(RobotMap.BACK_LEFT, backLeftAngle, backRightSpeed);
//        WheelCorrection backRight = wrapAroundCorrect(RobotMap.BACK_RIGHT, backRightAngle, backRightSpeed);
//
////        System.out.println("[DRIVE] FRONTLEFT" + steering[RobotMap.FRONT_LEFT].getSteeringAngle());
////        System.out.println("[DRIVE] FRONTRIGHT" + steering[RobotMap.FRONT_RIGHT].getSteeringAngle());
////        System.out.println("[DRIVE] BACKLEFT" + steering[RobotMap.BACK_LEFT].getSteeringAngle());
////        System.out.println("[DRIVE] BACKRIGHT" + steering[RobotMap.BACK_RIGHT].getSteeringAngle());
//        fourWheelSteer(frontLeft.angle, frontRight.angle, backLeft.angle, backRight.angle);
//        fourWheelDrive(frontLeft.speed, frontRight.speed, backLeft.speed, backLeft.speed);
//    }
    
    /**
     * Individually controls a specific steering motor
     *
     * @param angle Angle to drive to
     * @param speed Speed to drive at
     * @param steeringId Id of steering motor to drive
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
    	double angle = (direction == Direction.RIGHT) ? Math.PI/2 : -Math.PI/2;
    	
    	fourWheelSteer(angle, angle, angle, angle);    	
    	fourWheelDrive(SPEED_STRAFE, SPEED_STRAFE, SPEED_STRAFE, SPEED_STRAFE);
    }

	/**
     * @param direction
     */
    public void revolveDrive(Direction direction)
    {
    	final double FRONT_RADIUS = 65;
    	final double BACK_RADIUS = FRONT_RADIUS + RobotMap.LENGTH;
    	final double BACK_SPEED = 0.4;
    	final double FRONT_SPEED = BACK_SPEED * (FRONT_RADIUS / BACK_RADIUS);

    	double frontAngle = (Math.atan((2 * FRONT_RADIUS) / RobotMap.WIDTH)); 
    	double backAngle = (Math.atan((2 * BACK_RADIUS) / RobotMap.WIDTH));   
//    	 System.out.println("Front Angle=" + frontAngle + ", Back Angle=" + backAngle);
    	
    	if (direction == Direction.RIGHT)
    	{
    		fourWheelDrive(-FRONT_SPEED, FRONT_SPEED, -BACK_SPEED, BACK_SPEED);
    	} 
    	else 
    	{
    		fourWheelDrive(FRONT_SPEED, -FRONT_SPEED, BACK_SPEED, -BACK_SPEED);
    	}
		fourWheelSteer(-frontAngle, frontAngle, -backAngle, backAngle);
    }

    /**
     * Individually controls a specific driving motor
     *
     * @param speed Speed to drive at
     * @param steeringId Id of driving motor to drive
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
     * @param value1 - The first angle to check against
     * @param value2 - The second angle to check against
     * @return The normalized wrap around difference
     */
    static double wrapAroundDifference(double value1, double value2)
    {
        double diff = Math.abs(value1 - value2) % (2*Math.PI);
        while (diff > Math.PI)
        {
            diff = (2.0 * Math.PI) - diff;
        }
        LOGGER.debug(String.format("wrapAroundDifference v1=%f v2=%f diff=%f",
        		value1, value2, diff));
        return diff;
    }
	/**
	 * Only used for steering
	 * @param steeringIndex - which wheel pod
	 * @param targetAngle - in radians
	 * @param targetSpeed
	 * @return corrected
	 */
	private WheelCorrection wrapAroundCorrect(int steeringIndex, double targetAngle, double targetSpeed)
	{
	    WheelCorrection corrected = new WheelCorrection(targetAngle, targetSpeed);
	    
	    if (!steering[steeringIndex].approachingMaxTurns())
	    {
	    	double normalizedSteeringAngle = steering[steeringIndex].getSteeringAngle() % (Math.PI * 2);
		    if (wrapAroundDifference(normalizedSteeringAngle, targetAngle) > Math.PI / 2)
		    {
		    	// shortest path to desired angle is to reverse speed and adjust angle -PI
		        corrected.speed *= -1;
		
		        corrected.angle -= Math.PI;
//		        if (corrected.angle < -Math.PI)
//		        {
//		            corrected.angle += Math.PI * 2;
//		        }
		    }
	    }
	    LOGGER.debug(corrected);
	    return corrected;
	}

    /**
     * New drive function. Allows for wheel correction using speed based on a
     * specified correction angle
     *
     * @param speed The speed to drive at
     * @param inverts Array of which motors to invert in form {FL, FR, BL, BR}
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
     * @param steeringMotor The id of the steering motor (0 = FL, 1 = FR, 2 =
     * BL, 3 = BR)
     * @param value The new center value
     */
    public void setSteeringCenter(int steeringMotor, double value)
    {
        steering[steeringMotor].setCenter(value);
    }

    /**
     * Get the steering angle of the corresponding steering motor
     *
     * @param steeringId The id of the steering motor
     * @return
     */
    public double getSteeringAngle(int steeringId)
    {
        return steering[steeringId].getSensorValue();
    }

    /**
     * Get the normalized steering angle of the corresponding steering motor
     *
     * @param steeringId The id of the steering motor
     * @return
     */
    public double getNormalizedSteeringAngle(int steeringId)
    {
        return steering[steeringId].getSteeringAngle();
    }

}

class WheelCorrection {	
	public double speed;
	public double angle;
	
	public WheelCorrection(double angleIn, double speedIn)
	{
		angle = angleIn;
		speed = speedIn;
	}

	@Override
	public String toString() {
		return "WheelCorrection [speed=" + speed + ", angle=" + angle + "]";
	}
}
