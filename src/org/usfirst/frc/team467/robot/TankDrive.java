package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.MotorSafety;
import edu.wpi.first.wpilibj.MotorSafetyHelper;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TankDrive implements Driveable
{
    private static final Logger LOGGER = Logger.getLogger(TankDrive.class);
        
    SpeedController fl;
    SpeedController fr;
    SpeedController bl;
    SpeedController br;
    
    private MotorSafetyHelper FLsafety = null;
    private MotorSafetyHelper FRsafety = null;
    private MotorSafetyHelper BLsafety = null;
    private MotorSafetyHelper BRsafety = null;
    
    private double cartSpeed;
    private double ACCELERATION = 0.1;
    private final double SPEED_SLOW_MODIFIER = 0.7;
    private final double SPEED_MAX_MODIFIER = 1.0;
    private final double SPEED_TURBO_MODIFIER = 1.0;
    private double MIN_SPEED = 0.1;
    private double prevLeft;
    private double prevRight;

    private TankDrive(SpeedController fl, SpeedController fr, SpeedController bl, SpeedController br)
    {
        this.fl = fl;
        this.fr = fr;
        this.bl = bl;
        this.br = br;
        
        FLsafety = new MotorSafetyHelper((MotorSafety)fl);
        FRsafety = new MotorSafetyHelper((MotorSafety)fr);
        BLsafety = new MotorSafetyHelper((MotorSafety)bl);
        BRsafety = new MotorSafetyHelper((MotorSafety)br);
    }
    
    public static TankDrive makeTalonTank(int fl, int fr, int bl, int br)
    {
        Talon flMotor = new Talon(fl);
        Talon frMotor = new Talon(fr);
        Talon blMotor = new Talon(bl);
        Talon brMotor = new Talon(br);

        return new TankDrive(flMotor, frMotor, blMotor, brMotor);
    }
    
    public static TankDrive makeCANTalonTank(int fl, int fr, int bl, int br)
    {
        CANTalon flMotor = new CANTalon(fl);
        CANTalon frMotor = new CANTalon(fr);
        CANTalon blMotor = new CANTalon(bl);
        CANTalon brMotor = new CANTalon(br);
        
        return new TankDrive(flMotor, frMotor, blMotor, brMotor);        
    }

    
    public static TankDrive makeJaguarTank(int fl, int fr, int bl, int br)
    {
        Jaguar flMotor = new Jaguar(fl);
        Jaguar frMotor = new Jaguar(fr);
        Jaguar blMotor = new Jaguar(bl);
        Jaguar brMotor = new Jaguar(br);
        return new TankDrive(flMotor, frMotor, blMotor, brMotor);
    }
    
    @Override
    public void feedMotorSafety()
    {
        if (FLsafety != null)
        {
            FLsafety.feed();
        }
        if (FRsafety != null)
        {
            FRsafety.feed();
        }
        if (BLsafety != null)
        {
            BLsafety.feed();
        }
        if (BRsafety != null)
        {
            BRsafety.feed();
        }
    }
    
    private double square(double number)
    {
        if (number >= 0.0)
        {
            return number * number;
        }
        else
        {
            return -(number * number);
        }
    }
    
    /**
     * Limit the rate at which the robot can change speed once driving fast.
     * This is to prevent causing mechanical damage - or tipping the robot
     * through stopping too quickly.
     *
     * @param newSpeed
     *            desired speed for robot
     * @param lastSpeed
     *            the last speed
     * @return returns rate-limited speed
     */
    private double limitSpeed(double newSpeed, double lastSpeed)
    {
        // Apply speed modifiers first
//        String strAccel = SmartDashboard.getString("DB/String 4", "");
//        try
//        {
//            ACCELERATION = strAccel.equals("") ? 0.1 : Double.valueOf(strAccel);
//        }
//        catch (NumberFormatException e)
//        {
//            LOGGER.info("Invalid Acceleration in String 4, assuming 0.1");
//            SmartDashboard.putString("DB/String 4", "0.1");
//        }
        
        if (DriverStation2016.getInstance().getSlow())
        {
            newSpeed *= SPEED_SLOW_MODIFIER;
        }
        else if (DriverStation2016.getInstance().getTurbo())
        {
            newSpeed *= SPEED_TURBO_MODIFIER;
        }
        else
        {
            // Limit maximum regular speed to specified Maximum.
            newSpeed *= SPEED_MAX_MODIFIER;
        }
        // Limit the rate at which robot can change speed once driving over 0.6
        if (Math.abs(newSpeed - lastSpeed) > ACCELERATION && Math.abs(lastSpeed) > 0.6)
        {
            if (newSpeed > lastSpeed) // Forwards
            {
                newSpeed = lastSpeed + ACCELERATION;
            }
            else // Backwards
            {
                newSpeed = lastSpeed - ACCELERATION;
            }
        }
        LOGGER.debug("LIMITED SPEED: " + newSpeed);
        
        // If speed is too slow just stop
        newSpeed = (Math.abs(newSpeed) >= MIN_SPEED) ? newSpeed: 0.0;
        return newSpeed;
    }
    
    private void drive(double leftSpeed, double rightSpeed)
    {
        LOGGER.debug("leftSpeed=" + (int)(100*leftSpeed) + " rightSpeed=" + (int)(100*rightSpeed));

        leftSpeed = limitSpeed(leftSpeed, prevLeft);
        rightSpeed = limitSpeed(rightSpeed, prevRight);
        prevLeft = leftSpeed;
        prevRight = rightSpeed;

        final double LEFT_SCALE = 1; // Double.valueOf((SmartDashboard.getString("DB/String 2"))) / 100.0;
        LOGGER.debug(LEFT_SCALE);
        final double RIGHT_SCALE = 0.88; // Double.valueOf((SmartDashboard.getString("DB/String 3"))) / 100.0;
        LOGGER.debug(RIGHT_SCALE);
        fl.set(square(-leftSpeed) * LEFT_SCALE);
        bl.set(square(-leftSpeed) * LEFT_SCALE);
        fr.set(square(rightSpeed) * RIGHT_SCALE);
        br.set(square(rightSpeed) * RIGHT_SCALE);
        
        feedMotorSafety();
    }


    @Override
    public void turnDrive(double speed)
    {
        drive(speed, -speed);
    }

    @Override
    public void oneStickDrive(MainJoystick467 joystick, boolean fieldAlign)
    {
        final double turn = joystick.getTankTurn();
        final double speed = joystick.getTankSpeed();
        arcadeDrive(turn, speed);
    }

    public void arcadeDrive(double turn, double speed)
    {
        final double left;
        final double right;
        final double maxTurn = 0.9; // Double.valueOf(SmartDashboard.getString("DB/String 1", "0.9"));
        final double minTurn = 0.5; // Double.valueOf(SmartDashboard.getString("DB/String 2", "0.5"));
        SmartDashboard.putString("DB/String 6", String.valueOf(maxTurn));
        SmartDashboard.putString("DB/String 7", String.valueOf(minTurn));

        turn *= (1.0 - Math.abs(speed)) * (maxTurn - minTurn) + minTurn;
        turn = square(turn);
        SmartDashboard.putString("DB/String 8", String.valueOf(turn));
        
        // turn;
        LOGGER.debug("turn=" + turn + " speed=" + speed);
        if (speed > 0.0) {
            if (turn > 0.0)
            {
              left = speed - turn;
              right = Math.max(speed, turn);
            }
            else
            {
              left = Math.max(speed, -turn);
              right = speed + turn;
            }
        }
        else
        {
            if (turn > 0.0) {
              left = -Math.max(-speed, turn);
              right = speed + turn;
            } else {
              left = speed - turn;
              right = -Math.max(-speed, -turn);
            }
        }
        drive(left, right);
    }

    @Override
    public void twoStickDrive(MainJoystick467 joystickLeft, RightJoystick467 joystickRight)
    {
        final double speedLeft = joystickLeft.getTankSpeed();
        final double speedRight = joystickRight.getTankSpeed();
        LOGGER.debug("twoStickDrive speedLeft=" + speedLeft + " speedRight=" + speedRight);
        drive(speedLeft, speedRight);
    }

    @Override
    public void individualSteeringDrive(double angle, int steeringId)
    {
        ; // Not applicable.
    }

    @Override
    public void stop()
    {
        drive(0.0, 0.0);
    }

    @Override
    public void unwind()
    {
        ; // Not applicable.
    }

    @Override
    public void strafeDrive(Direction direction)
    {
        double speed = 0.6;
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
        drive(-speed, -speed);
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

    @Override
    public void cartDrive(MainJoystick467 joystick)
    {
        Direction direction = Direction.NONE;
        if (joystick.getKartForward())
        {
            direction = Direction.FRONT;
        }
        else if (joystick.getKartBackward())
        {
            direction = Direction.BACK;
        }
        double turn = joystick.getTankTurn();
        boolean brake = joystick.getKartBrake();
        LOGGER.info("cartDrive direction=" + direction + " turn=" + turn + " brake=" + brake);
        double acceleration = 0.02; //Double.valueOf(SmartDashboard.getString("DB/String 1", "INVALID"));
        double breakIncrement = 0.1; //Double.valueOf(SmartDashboard.getString("DB/String 2", "INVALID"));
        SmartDashboard.putString("DB/String 6", String.valueOf(acceleration));
        SmartDashboard.putString("DB/String 7", String.valueOf(breakIncrement));
        double minDiff = 0.02;
        switch (direction)
        {
            case BACK:
                if (brake)
                {
                    cartSpeed = (cartSpeed > 0.0) ? cartSpeed - breakIncrement : cartSpeed + breakIncrement;
                    break;
                }
                cartSpeed = (cartSpeed < 1.0) ? cartSpeed + acceleration : 1.0;
                LOGGER.info("Front cartSpeed=" + cartSpeed);
                break;
            case FRONT:
                if (brake)
                {
                    cartSpeed = (cartSpeed > 0.0) ? cartSpeed - breakIncrement : cartSpeed + breakIncrement;
                    break;
                }
                cartSpeed = (cartSpeed > -1.0) ? cartSpeed - acceleration : -1.0;
                LOGGER.info("Back cartSpeed=" + cartSpeed);
                break;
            case NONE:
                if (Math.abs(cartSpeed) > minDiff)
                {
                    if (brake)
                    {
                        cartSpeed = (cartSpeed > 0.0) ? cartSpeed - breakIncrement : cartSpeed + breakIncrement;
                    }
                    cartSpeed = (cartSpeed > 0.0) ? cartSpeed - acceleration : cartSpeed + acceleration;
                }
                else
                {
                    cartSpeed = 0.0;
                }
                LOGGER.info("None cartSpeed=" + cartSpeed);
                break;
            default:
                cartSpeed = 0.0;
                LOGGER.info("Default: Stop");
                break;
        }
        joystick.setRumble((float)(cartSpeed / 2 + 0.5));
        arcadeDrive(turn, cartSpeed);
    }

    @Override
    public void splitDrive(MainJoystick467 joystickLeft, RightJoystick467 joystickRight)
    {
        arcadeDrive(joystickRight.getTankTurn(), joystickLeft.getTankSpeed());
    }

}
