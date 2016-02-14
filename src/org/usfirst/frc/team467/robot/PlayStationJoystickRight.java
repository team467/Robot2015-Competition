package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.Joystick;

public class PlayStationJoystickRight implements RightJoystick467
{
    private Joystick joystick;
    private double stickY = 0.0;
    private double stickX = 0.0;
    private static final int AXIS_Y = 3;
    private static final int AXIS_X = 2;
    private static final double DEADZONE = 0.1;

    
    public PlayStationJoystickRight(int stick)
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
        stickY = accelerateJoystickInput(joystick.getRawAxis(AXIS_Y));
        stickX = accelerateJoystickInput(joystick.getRawAxis(AXIS_X));
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
//        return input * Math.abs(input);
      return input;
    }

    @Override
    public double getTankSpeed()
    {
        // TODO Auto-generated method stub
        return stickY;
    }

    @Override
    public double getTankTurn()
    {
        // TODO Auto-generated method stub
        return stickX;
    }
}