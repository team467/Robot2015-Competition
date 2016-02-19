package org.usfirst.frc.team467.robot;

public interface Driveable
{
    void turnDrive(double speed);
    void oneStickDrive(MainJoystick467 joystick, boolean fieldAlign);
    void twoStickDrive(MainJoystick467 joystickLeft, RightJoystick467 joystickRight);
    void individualSteeringDrive(double angle, int steeringId);
    void cartDrive(MainJoystick467 joystick);
    void splitDrive(MainJoystick467 joystickLeft, RightJoystick467 joystickRight);
    void stop();
    
    void unwind();
    void strafeDrive(Direction direction);
    void revolveDriveLarge(Direction direction);
    void revolveDriveSmall(Direction direction);
    void individualWheelDrive(double speed, int steeringId);
    void arcadeDrive(final double turn, final double speed);
    void feedMotors();
}
