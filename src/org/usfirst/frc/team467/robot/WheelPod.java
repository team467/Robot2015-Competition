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
    private CANTalon driveMotor;
    public static final double kDefaultExpirationTime = 0.1;
    private MotorSafetyHelper m_safetyHelper;
    private Steering steering;
    
 // Magic number copied from WPI code
    private static final byte SYNC_GROUP = (byte) 0x80;
    
    private static final double SPEED_SLOW_MODIFIER = 0.5;
    private static final double SPEED_TURBO_MODIFIER = 2.0;
    private static final double SPEED_MAX_MODIFIER = 0.8;
    private static final double SPEED_MAX_CHANGE = 0.15;
    final double MAX_DRIVE_ANGLE = Math.PI / 25;
    private final Boolean INVERT;
    
    private double lastSpeed = 0.0;
    
    public WheelPod(int driveMotorID, int steeringMotorID, int steeringSensorID, PID pid, double center, boolean invert)
    {
        steering = new Steering(pid, steeringMotorID, steeringSensorID, center);
        driveMotor = new CANTalon(driveMotorID);
        setupMotorSafety();
        INVERT = invert;
    }

    public void drive(double speed, double angle)
    {
        if (driveMotor == null)
        {
            throw new NullPointerException("Null motor provided");
        }
        if (steering.getAngleDelta() < MAX_DRIVE_ANGLE)
        {
            // Limit the speed
            speed = (INVERT ? -1 : 1) * limitSpeed(speed);
            
            WheelCorrection correction = wrapAroundCorrect(speed, angle);
            try
            {
                driveMotor.set(correction.speed, SYNC_GROUP);
                m_safetyHelper.feed();
                steering.setAngle(correction.angle);
                LOGGER.debug("DRIVE");
            }
            catch (Exception e)
            {
                LOGGER.error(e.getMessage());
            }
        }
        else
        {
            LOGGER.debug("NO DRIVE");
            stopMotor();
        }
    }
    
    public void drive(double speed)
    {
        drive(speed, steering.getSteeringAngle());
    }
    
    public void steer(double angle)
    {
        drive(driveMotor.getSpeed(), angle);
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
        LOGGER.debug("LIMIT SPEED: " + speed);
        return (speed);
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
    private WheelCorrection wrapAroundCorrect(double targetAngle, double targetSpeed)
    {
        WheelCorrection corrected = new WheelCorrection(targetAngle, targetSpeed);

        double normalizedSteeringAngle = steering.getSteeringAngle() % (Math.PI * 2);
        if (wrapAroundDifference(normalizedSteeringAngle, targetAngle) > Math.PI / 2)
        {
            // shortest path to desired angle is to reverse speed and adjust angle -PI
            corrected.speed *= -1;

            corrected.angle -= Math.PI;
            LOGGER.debug(corrected);
        }
        return corrected;
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
        if (driveMotor != null) driveMotor.set(0.0);
        if (m_safetyHelper != null) m_safetyHelper.feed();
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
