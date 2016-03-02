/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc.team467.robot;

import java.lang.Math;
import edu.wpi.first.wpilibj.Joystick;

/**
 *
 */
public class LogitechJoystick implements MainJoystick467, RightJoystick467
{
    private Joystick joystick;
    private boolean[] buttons = new boolean[12];     // array of current button states
    private boolean[] prevButtons = new boolean[12]; // array of previous button states, involved in edge detection.
    private double stickX = 0.0;
    private double stickY = 0.0;
    private int pov = 0;
    private double twist = 0.0;
    private boolean flap = false;

    private static final int TRIGGER = 1;
    private static final double DEADZONE = 0.1;

    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int TWIST_AXIS = 2;
    private static final int FLAP_AXIS = 3;
    private static final int POV_INDEX = 0;

    /**
     * Create a new joystick on a given channel
     *
     * @param stick
     */
    public LogitechJoystick(int stick)
    {
        joystick = new Joystick(stick);
    }

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#getJoystick()
     */
    @Override
    public Joystick getJoystick()
    {
        return joystick;
    }

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#readInputs()
     */
    @Override
    public void readInputs()
    {
        // read all buttons
        for (int i = 0; i < 12; i++)
        {
            prevButtons[i] = buttons[i];
            buttons[i] = joystick.getRawButton(i + 1);
        }

        // Read Joystick Axes
        flap = joystick.getRawAxis(FLAP_AXIS) < 0.0;
        stickY = accelerateJoystickInput(joystick.getRawAxis(AXIS_Y));
        stickX = accelerateJoystickInput(joystick.getRawAxis(AXIS_X));
        twist = accelerateJoystickInput(joystick.getRawAxis(TWIST_AXIS));
        pov = joystick.getPOV(POV_INDEX);
    }

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#buttonDown(int)
     */
    @Override
    public boolean buttonDown(int button)
    {
        return buttons[(button) - 1];
    }

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#buttonPressed(int)
     */
    @Override
    public boolean buttonPressed(int button)
    {
        return buttons[button - 1] && !prevButtons[button - 1];
    }

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#buttonReleased(int)
     */
    @Override
    public boolean buttonReleased(int button)
    {
        return !buttons[button - 1] && prevButtons[button - 1];
    }

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#getPOV()
     */
    @Override
    public Direction getPOV()
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

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#getFlap()
     */
    @Override
    public boolean getFieldAligned()
    {
        return flap;
    }

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#getTwist()
     */
    @Override
    public double getTurn()
    {
        return twist;
    }

    @Override
    public boolean getTurnButton()
    {
        return buttonDown(2);
    }

    @Override
    public boolean getRevolveLargeLeft()
    {
        return buttonDown(3);
    }

    @Override
    public boolean getRevolveLargeRight()
    {
        return buttonDown(4);
    }

    @Override
    public boolean getRevolveSmallLeft()
    {
        return buttonDown(5);
    }

    @Override
    public boolean getRevolveSmallRight()
    {
        return buttonDown(6);
    }

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#getStickDistance()
     */
    @Override
    public double getStickDistance()
    {
        return Math.sqrt(stickX * stickX + stickY * stickY);
    }

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#getStickAngle()
     */
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
    public double getTankTurn()
    {
        return stickX;
    }

    @Override
    public double getTankSpeed()
    {
        return stickY;
    }

    /* (non-Javadoc)
     * @see org.usfirst.frc.team467.robot.Joystick467#isInDeadzone()
     */
    @Override
    public boolean isInDeadzone()
    {
        return (Math.abs(stickX) < DEADZONE) && (Math.abs(stickY) < DEADZONE);
    }

    @Override
    public boolean getSlow()
    {
        return buttonDown(TRIGGER);
    }

    @Override
    public boolean getTurbo()
    {
        return buttonDown(7);
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
        return input * Math.abs(input);
    }

    @Override
    public boolean getCalibrateConfirm()
    {
        return buttonDown(TRIGGER);
    }

    @Override
    public boolean getResetGyro()
    {
        return buttonDown(8);
    }

    @Override
    public void setRumble(float rumble)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean getKartForward()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getKartBackward()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getKartBrake()
    {
        // TODO Auto-generated method stub
        return false;
    }
}