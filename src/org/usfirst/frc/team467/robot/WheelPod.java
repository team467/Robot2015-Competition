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
    
    public WheelPod(int driveMotorID, int steeringMotorID, int steeringSensorID, PID pid, double center)
    {
        steering = new Steering(pid, steeringMotorID, steeringSensorID, center);
        driveMotor = new CANTalon(driveMotorID);
        setupMotorSafety();
    }
    
    public void drive(double speed, double angle)
    {
        WheelCorrection correction = wrapAroundCorrect(speed, angle);
        try
        {
            driveMotor.set(correction.speed);
            m_safetyHelper.feed();
            steering.setAngle(correction.angle);
        }
        catch (Exception e)
        {
            LOGGER.error(e.getMessage());
        }
    }
    
    public CANTalon getDriveMotor()
    {
        return driveMotor;
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
