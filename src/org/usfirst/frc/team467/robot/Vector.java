package org.usfirst.frc.team467.robot;

public class Vector
{
    private double angle;
    private double speed;
    private double x;
    private double y;

    public static Vector makeSpeedAngle(double speed, double angle)
    {
        Vector v = new Vector();
        v.setSpeedAngle(speed, angle);
        return v;
    }
    
    public static Vector makeXY(double x, double y)
    {
        Vector v = new Vector();
        v.setXY(x, y);
        return v;
    }

    public static Vector makeUnit(double angle)
    {
        Vector v = new Vector();
        v.setSpeedAngle(1.0, angle);
        return v;
    }
    
    private void setSpeedAngle(double speed, double angle)
    {
        this.speed = speed;
        this.angle = angle;
        x = Math.cos(angle) * speed;
        y = Math.sin(angle) * speed;
    }
    
    private void setXY(double x, double y)
    {
        this.x = x;
        this.y = y;
        angle = Math.atan2(y, x);
        speed = Math.sqrt(x*x + y*y);
    }
    
    public static Vector add(Vector v1, Vector v2)
    {
        return makeXY(v1.getX() + v2.getX(), v1.getY() + v2.getY());
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