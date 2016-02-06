package org.usfirst.frc.team467.robot;

public enum Direction
{
    FRONT (0), LEFT (90), BACK (180), RIGHT (270), NONE (-1);
    
    public int angleDeg;
    public double angleRad;
    
    Direction(int angleDeg)
    {
        this.angleDeg = angleDeg;
        angleRad = Math.toRadians(angleDeg);
    }
}