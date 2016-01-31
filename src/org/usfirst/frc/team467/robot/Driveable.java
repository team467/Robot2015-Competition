package org.usfirst.frc.team467.robot;

public interface Driveable
{
    void turnDrive(double speed);
    void arcadeDrive(Joystick467 joystick, boolean fieldAlign);
    void individualSteeringDrive(double angle, int steeringId);
    void stop();
    void unwind();
    void strafeDrive(Direction direction);
    void revolveDriveLarge(Direction direction);
    void revolveDriveSmall(Direction direction);
    void individualWheelDrive(double speed, int steeringId);
}
