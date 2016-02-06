package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.RobotDrive;

public class TankDrive implements Driveable
{
    private static final Logger LOGGER = Logger.getLogger(TankDrive.class);
    
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
    public void oneStickDrive(Joystick467 joystick, boolean fieldAlign)
    {
        final double turn = joystick.getTankTurn();
        final double speed = joystick.getTankSpeed();
        LOGGER.debug("turn=" + turn + " speed=" + speed);
        front.arcadeDrive(-speed, -turn);
        back.arcadeDrive(speed, -turn);
    }

    @Override
    public void twoStickDrive(Joystick467 joystickLeft, Joystick467 joystickRight)
    {
        final double speedLeft = joystickLeft.getTankSpeed();
        final double speedRight = joystickRight.getTankSpeed();
        LOGGER.debug("twoStickDrive speedLeft=" + speedLeft + " speedRight=" + speedRight);
//        front.tankDrive(-speedLeft, -speedRight);
        back.tankDrive(speedLeft, speedRight);
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
        double speed = -0.6;
        switch (direction)
        {
            case FRONT:
                // Nothing to do, speed good as is.
                break;
            case BACK:
                speed = -speed;
                break;
            default:
                speed = 0.0;
                return;
        }
        front.tankDrive(-speed, -speed);
        back.tankDrive(speed, speed);
    }

    @Override
    public void revolveDriveLarge(Direction direction)
    {
        // Not applicable

    }

    @Override
    public void revolveDriveSmall(Direction direction)
    {
        // Not applicable

    }

    @Override
    public void individualWheelDrive(double speed, int steeringId)
    {
        // Not Applicable

    }

}
