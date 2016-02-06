package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.Joystick;

public interface Joystick467
{

    /**
     * Returns the jaw joystick object inside Joystick467
     * 
     * @return
     */
    Joystick getJoystick();

    /**
     * Read all inputs from the underlying joystick object.
     */
    void readInputs();

    /**
     * Check if a specific button is being held down. Ignores first button
     * press, but the robot loops too quickly for this to matter.
     *
     * @param button
     * @return
     */
    boolean buttonDown(int button);

    /**
     * Check if a specific button has just been pressed. (Ignores holding.)
     *
     * @param button
     * @return
     */
    boolean buttonPressed(int button);

    /**
     * Check if a specific button has just been released.
     *
     * @param button
     * @return
     */
    boolean buttonReleased(int button);

    /**
     * Gets the X position of the stick. Left to right ranges from -1.0 to 1.0,
     * with 0.0 in the middle. This value is accelerated.
     * 
     * @return
     */
    double getTankTurn();

    /**
     * Gets the Y position of the stick. Up to down ranges from -1.0 to 1.0,
     * with 0.0 in the middle. This value is accelerated.
     * 
     * @return
     */
    double getTankSpeed();

    /**
     * 
     * @return the angle of the POV in degrees, or -1 if the POV is not pressed.
     */
    Direction getStrafeDirection();

    /**
     * 
     * 
     * @return
     */
    boolean getFieldAligned();

    double getTurn();
    boolean getTurnButton();

    /**
     * Calculate the distance of this stick from the center position.
     *
     * @return
     */
    double getStickDistance();

    boolean isInDeadzone();

    /**
     * Calculate the angle of this joystick.
     *
     * @return Joystick Angle in range -PI to PI
     */
    double getAngle();
    
    boolean getSlow();
    
    boolean getCalibrateConfirm();
    
    boolean getTurbo();
    
    boolean getResetGyro();
    
    boolean getRevolveLargeLeft();
    boolean getRevolveLargeRight();
    boolean getRevolveSmallLeft();
    boolean getRevolveSmallRight();
}