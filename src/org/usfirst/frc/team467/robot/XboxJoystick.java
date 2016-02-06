package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.Joystick;

public class XboxJoystick implements Joystick467
{
    private Joystick joystick;
    
    private boolean[] buttons = new boolean[12];     // array of current button states
    private boolean[] prevButtons = new boolean[12]; // array of previous button states, involved in edge detection.
    
    public XboxJoystick(int stick)
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
        for (int i = 0; i < 12; i++)
        {
            prevButtons[i] = buttons[i];
            buttons[i] = joystick.getRawButton(i + 1);
        }

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
    public Direction getStrafeDirection()
    {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getStickDistance()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getAngle()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getTankTurn()
    {
        // TODO return x
        return 0;
    }

    @Override
    public double getTankSpeed()
    {
        // TODO return y;
        return 0;
    }

    @Override
    public boolean isInDeadzone()
    {
        // TODO Auto-generated method stub
        return false;
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
    public boolean getTurnButton()
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

}
