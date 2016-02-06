package org.usfirst.frc.team467.robot;

public interface Driveable
{
    void turnDrive(double speed);
    void oneStickDrive(Joystick467 joystick, boolean fieldAlign);
    void twoStickDrive(Joystick467 joystickLeft, Joystick467 joystickRight);
    void individualSteeringDrive(double angle, int steeringId);
    void stop();
    void unwind();
    void strafeDrive(Direction direction);
    void revolveDriveLarge(Direction direction);
    void revolveDriveSmall(Direction direction);
    void individualWheelDrive(double speed, int steeringId);
}
