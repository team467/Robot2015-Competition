package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;

public class XBoxJoystickMain implements MainJoystick467
{
    private Joystick joystick;
    private double stickX = 0.0;
    private double stickY = 0.0;
    private double leftBumper;
    private double rightBumper;
    private int pov = 0;
    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int LEFT_BUMPER = 2;
    private static final int RIGHT_BUMPER = 3;
    private static final int POV_INDEX = 0;
    private static final double DEADZONE = 0.1;

    private boolean[] buttons = new boolean[10];     // array of current button states
    private boolean[] prevButtons = new boolean[10]; // array of previous button states, involved in edge detection.

    public XBoxJoystickMain(int stick)
    {
        joystick = new Joystick(stick);
    }

    @Override
    public Joystick getJoystick()
    {
        return joystick;
    }

    @Override
    public void readInputs()
    {
        for (int i = 0; i < 9; i++)
        {
            prevButtons[i] = buttons[i];
            buttons[i] = joystick.getRawButton(i + 1);
        }
        stickY = accelerateJoystickInput(joystick.getRawAxis(AXIS_Y));
        stickX = accelerateJoystickInput(joystick.getRawAxis(AXIS_X));
        leftBumper = accelerateJoystickInput(joystick.getRawAxis(LEFT_BUMPER));
        rightBumper = accelerateJoystickInput(joystick.getRawAxis(RIGHT_BUMPER));
        pov = joystick.getPOV(POV_INDEX);
    }

    /**
     * Implement a dead zone for Joystick centering - and a non-linear
     * acceleration as the user moves away from the zero position.
     *
     * @param input
     * @return processed input
     */
    private double accelerateJoystickInput(double input)
    {
        // Ensure that there is a dead zone around zero
        if (Math.abs(input) < DEADZONE)
        {
            return 0.0;
        }
        // Simply square the input to provide acceleration
        // ensuring that the sign of the input is preserved
//      return input * Math.abs(input);
    return input;
    }

    @Override
    public boolean buttonDown(int button)
    {
        return buttons[(button) - 1];
    }

    @Override
    public boolean buttonPressed(int button)
    {
        return buttons[button - 1] && !prevButtons[button - 1];
    }

    @Override
    public boolean buttonReleased(int button)
    {
        return !buttons[button - 1] && prevButtons[button - 1];
    }

    @Override
    public void setRumble(float rumble)
    {
        joystick.setRumble(RumbleType.kLeftRumble,  rumble);
        joystick.setRumble(RumbleType.kRightRumble, rumble);
    }

    @Override
    public double getTankTurn()
    {
        return stickX;
    }

    @Override
    public double getTankSpeed()
    {
        // TODO Auto-generated method stub
        return stickY;
    }

    @Override
    public Direction getStrafeDirection()
    {
        for (Direction dir : Direction.values())
        {
            if (pov == dir.angleDeg)
            {
                return dir;
            }
        }
        return Direction.NONE;
    }

    @Override
    public boolean getFieldAligned()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getTurn()
    {
        return rightBumper - leftBumper;
    }

    @Override
    public boolean getTurnButton()
    {
        return false;
    }

    @Override
    public double getStickDistance()
    {
        return Math.sqrt(stickX * stickX + stickY * stickY);
    }

    @Override
    public boolean isInDeadzone()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getAngle()
    {
     // This shouldn't be necessary, deadzone filtering should already
        // be performed - however it doesn't hurt to make sure.
        if (isInDeadzone())
        {
            return 0.0;
        }

        if (stickY == 0.0)
        {
            // In Y deadzone avoid divide by zero error
            return (stickX > 0.0) ? Math.PI / 2 : -Math.PI / 2;
        }

        // Return value in range -PI to PI
        double stickAngle = Math.atan(stickX / -stickY);

        if (stickY > 0)
        {
            stickAngle += (stickX > 0) ? Math.PI : -Math.PI;
        }

        return (stickAngle);
    }

    @Override
    public boolean getSlow()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getCalibrateConfirm()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getTurbo()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getResetGyro()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getRevolveLargeLeft()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getRevolveLargeRight()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getRevolveSmallLeft()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getRevolveSmallRight()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getKartForward()
    {
        return buttonDown(2);
    }

    @Override
    public boolean getKartBackward()
    {
        return buttonDown(1);
    }

    @Override
    public boolean getKartBrake()
    {
        return buttonDown(3) || buttonDown(4);
    }

}
