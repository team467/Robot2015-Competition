package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.CANTalon;

public class TestWheelPod implements WheelPod
{
    private static final Logger LOGGER = Logger.getLogger(TestWheelPod.class);

    double expirationTime;
    boolean alive;
    boolean safetyEnabled;

    @Override
    public void setExpiration(double timeout)
    {
        expirationTime = timeout;
        LOGGER.debug("Set expiration:" + expirationTime);
    }

    @Override
    public double getExpiration()
    {
        LOGGER.debug("Expiration: " + expirationTime);
        return expirationTime;
    }

    @Override
    public boolean isAlive()
    {
        LOGGER.debug("Is Alive: " + alive);
        return alive;
    }

    @Override
    public void stopMotor()
    {
        LOGGER.debug("Stop Motor");
    }

    @Override
    public void setSafetyEnabled(boolean enabled)
    {
        safetyEnabled = enabled;
        LOGGER.debug("Set safety enabled: " + safetyEnabled);
    }

    @Override
    public boolean isSafetyEnabled()
    {
        return safetyEnabled;
    }

    @Override
    public String getDescription()
    {
        return "Test WheelPod to log commands.";
    }

    @Override
    public void drive(double speed, double angle)
    {
        LOGGER.info("Speed: " + speed + ", angle: " + angle);
    }

    @Override
    public void drive(double speed)
    {
        LOGGER.info("Speed: " + speed);
    }

    @Override
    public void steer(double angle)
    {
        LOGGER.info("Angle: " + angle);
    }

    @Override
    public void absoluteSteer(double angle)
    {
        LOGGER.info("Angle: " + angle);
    }

    @Override
    public Steering getSteering()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
