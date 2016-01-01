package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.MotorSafetyHelper;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.MotorSafety;
import edu.wpi.first.wpilibj.PIDController;

public class WheelPod implements MotorSafety
{
    private static final Logger LOGGER = Logger.getLogger(WheelPod.class);
    final String name;
    private final CANTalon driveMotor;
    public static final double kDefaultExpirationTime = 0.1;
    private MotorSafetyHelper m_safetyHelper;
    private Steering steering;
    
 // Magic number copied from WPI code
    private static final byte SYNC_GROUP = (byte) 0x80;
    
    private static final double SPEED_SLOW_MODIFIER = 0.5;
    private static final double SPEED_TURBO_MODIFIER = 2.0;
    private static final double SPEED_MAX_MODIFIER = 0.8;
    private static final double SPEED_MAX_CHANGE = 0.15;
    private final double MAX_DRIVE_ANGLE = Math.PI / 25;
    private final Boolean INVERT;
    
    private double lastSpeed = 0.0;
    
    public WheelPod(String name, int driveMotorID, int steeringMotorID, int steeringSensorID, PID pid, double center, boolean invert)
    {
        this.name = name;
        steering = new Steering(name, pid, steeringMotorID, steeringSensorID, center);
        driveMotor = new CANTalon(driveMotorID);
        if (driveMotor == null)
        {
            throw new NullPointerException("Null motor provided");
        }
        
        setupMotorSafety();
        INVERT = invert;
    }

    public void drive(Vector v)
    {

        LOGGER.debug(name + " DRIVE(requested): " + v);
        Vector correction = wrapAroundCorrect(v);

        // Don't drive until wheel is close to commanded steering angle
        // TODO Always set steering angle, this if statement should only affect drive speed
        final double delta = steering.getAngleDelta();
        if (delta < MAX_DRIVE_ANGLE)
        {
            // Limit and possibly invert the speed
            final double outSpeed = (INVERT ? -1 : 1) * limitSpeed(correction.getSpeed());
            LOGGER.debug(name + " DRIVE(corrected): " + outSpeed + ", " + Vector.r(correction.getAngle()));

            try
            {
                m_safetyHelper.feed();
                driveMotor.set(outSpeed, SYNC_GROUP);
                steering.setAngle(correction.getAngle());
            }
            catch (Exception e)
            {
                LOGGER.error("Caught exception in drive() method", e);
            }
        }
        else
        {
            LOGGER.debug(name + " NO DRIVE delta=" + Vector.r(delta));
            stopMotor();
        }
    }
    
    public void drive(double speed)
    {
        drive(Vector.makeSpeedAngle(speed, steering.getSteeringAngle()));
    }
    
    public void steer(double angle)
    {
        drive(Vector.makeSpeedAngle(driveMotor.getSpeed(), angle));
    }
    
    public void absoluteSteer(double angle)
    {
        steering.setAbsoluteAngle(angle);
    }
    
    public CANTalon getDriveMotor()
    {
        return driveMotor;
    }
    
    public Steering getSteering()
    {
        return steering;
    }
    
    /**
     * Turns on the PID.
     */
    public void enableSteeringPID()
    {
            steering.enablePID();
    }
    
    /**
     * Turns off the PID.
     */
    public void disableSteeringPID()
    {
            steering.disablePID();
    }

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
        if (name.equals("FL"))
        {
            LOGGER.debug(name + " LIMIT SPEED: " + speed);
        }
        return (speed);
    }
    
    /**
     * Function to determine the wrapped around difference from the joystick
     * angle to the steering angle.
     *
     * @param actualNormalizedAngle
     *            - The actual normalized angle the wheel is facing right now
     * @param targetAngle
     *            - The target angle requested by driver
     * @return The normalized wrap around difference
     */
    double wrapAroundDifference(double actualNormalizedAngle, double targetAngle)
    {
        double diff = Math.abs(actualNormalizedAngle - targetAngle) % (2 * Math.PI);
        while (diff > Math.PI)
        {
            diff = (2.0 * Math.PI) - diff;
        }
        LOGGER.debug(String.format("%s wrapAroundDifference() actualNormalizedAngle=%4.2fpi targetAngle=%4.2fpi diff=%4.2fpi",
                name, actualNormalizedAngle/Math.PI, targetAngle/Math.PI, diff/Math.PI));
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
    private Vector wrapAroundCorrect(Vector input)
    {   
        final double normalizedSteeringAngle = steering.getSteeringAngle() % (Math.PI * 2);
        final double difference = wrapAroundDifference(normalizedSteeringAngle, input.getAngle());
        LOGGER.debug(name + " wrapAroundCorrect() " + " normalizedSteeringAngle=" + Vector.r(normalizedSteeringAngle)
                + " difference=" + Vector.r(difference));

        if (difference > Math.PI / 2)
        {
            // shortest path to desired angle is to reverse speed and adjust angle -PI
            Vector output = Vector.makeSpeedAngle(-input.getSpeed(), input.getAngle() - Math.PI);
            LOGGER.debug(name + " wrapAroundCorrect() FLIPPED " + output);
            return output;
        }
        return input;
    }

    @Override
    public void setExpiration(double timeout)
    {
        m_safetyHelper.setExpiration(timeout);    
    }

    @Override
    public double getExpiration()
    {
        return m_safetyHelper.getExpiration();
    }

    @Override
    public boolean isAlive()
    {
        return m_safetyHelper.isAlive();
    }

    @Override
    public void stopMotor()
    {
        if (driveMotor != null)
        {
            driveMotor.set(0.0);
        }
        if (m_safetyHelper != null)
        {
            m_safetyHelper.feed();
        }
    }

    @Override
    public void setSafetyEnabled(boolean enabled)
    {
        m_safetyHelper.setSafetyEnabled(enabled);
    }

    @Override
    public boolean isSafetyEnabled()
    {
        return m_safetyHelper.isSafetyEnabled();
    }

    @Override
    public String getDescription()
    {
        return "Wheelpod Class";
    }
    
    private void setupMotorSafety() {
        m_safetyHelper = new MotorSafetyHelper(this);
        m_safetyHelper.setExpiration(kDefaultExpirationTime);
        m_safetyHelper.setSafetyEnabled(true);
    }
}
