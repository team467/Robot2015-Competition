package org.usfirst.frc.team467.robot;

public class Vector
{
    private double angle;
    private double speed;
    private double x;
    private double y;

    public Vector(double angle, double speed)
    {
        setAngleandSpeed(angle, speed);
    }
    
    public void setAngleandSpeed(double angle, double speed)
    {
        this.angle = angle;
        this.speed = speed;
        x = Math.cos(angle) * speed;
        x = Math.sin(angle) * speed;
    }
    
    public void setXandY(double x, double y)
    {
        this.x = x;
        this.y = y;
        angle = Math.atan2(y, x);
        speed = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }
    
    public double getAngle()
    {
        return angle;
    }

    public double getSpeed()
    {
        return speed;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }
    
    /**
     * 
     * @param value
     * @return value expressed as multiple of pi
     */
    static public String r(double value)
    {
        return String.format("%4.2fpi", value / Math.PI);
    }
    
    @Override
    public String toString()
    {
        return "WheelCorrection [angle=" + r(angle) + " speed=" + speed + "]";
    }
    
}