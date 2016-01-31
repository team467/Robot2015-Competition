package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.RobotDrive;

public class TankDrive implements Driveable
{
    RobotDrive front;
    RobotDrive back;

    public TankDrive(int frontLeftMotor, int frontRightMotor, int backLeftMotor, int backRightMotor)
    {
        front = new RobotDrive(frontLeftMotor, frontRightMotor);
        back = new RobotDrive(backLeftMotor, backRightMotor);
    }

    @Override
    public void turnDrive(double speed)
    {
        front.tankDrive(-speed, speed);
        back.tankDrive(-speed, speed);
    }

    @Override
    public void arcadeDrive(double angle, double speed, boolean fieldAlign)
    {
        final double rotation = angle/(2*Math.PI);
        front.drive(speed, rotation);
        back.drive(speed, rotation);
    }

    @Override
    public void individualSteeringDrive(double angle, int steeringId)
    {
        ; // Not applicable.
    }

    @Override
    public void stop()
    {
        front.tankDrive(0.0, 0.0);
        back.tankDrive(0.0, 0.0);
    }

    @Override
    public void unwind()
    {
        ; // Not applicable.
    }

    @Override
    public void strafeDrive(Direction direction)
    {
        double speed = -0.8;
        switch (direction)
        {
            case FRONT:
                // Nothing to do, speed good as is.
                break;
            case BACK:
                speed = -speed;
                break;
            case LEFT:
            case RIGHT:
                speed = 0.0;
                return;
        }
        front.tankDrive(-speed, -speed);
        back.tankDrive(speed, speed);
    }

    @Override
    public void revolveDriveLarge(Direction direction)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void revolveDriveSmall(Direction direction)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void individualWheelDrive(double speed, int steeringId)
    {
        // TODO Auto-generated method stub

    }

}
