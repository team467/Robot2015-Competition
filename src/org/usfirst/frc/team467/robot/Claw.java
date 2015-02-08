package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.Talon;

public class Claw {
	private static Claw claw = null;
	
	private Talon clawMotor = null;
	
	private final double OPEN_SPEED_SLOW = 0.4;
	private final double OPEN_SPEED_FAST = 0.8;
	private final double CLOSE_SPEED_SLOW = -OPEN_SPEED_SLOW;
	private final double CLOSE_SPEED_FAST = -OPEN_SPEED_FAST;
	
	/**
	 * Singleton instance of the robot.
	 * @return
	 */
	public static Claw getInstance()
	{
		if (claw == null)claw = new Claw();
		return claw;
	}
	
	/**
	 * Private constructor
	 */
	private Claw() {
//		clawMotor = new Talon(6);
	}
	
	/**
	 * Moves the claw given the ClawMoveType
	 * @param clawTypes
	 */
	public void moveClaw(ClawMoveTypes clawTypes)
	{
		if(clawTypes == ClawMoveTypes.GRIP_SLOW)
		{
			clawMotor.set(CLOSE_SPEED_SLOW);
		}
		else if(clawTypes == ClawMoveTypes.GRIP_FAST)
		{
			clawMotor.set(CLOSE_SPEED_FAST);
		}
		else if(clawTypes == ClawMoveTypes.UNGRIP_SLOW)
		{
			clawMotor.set(OPEN_SPEED_SLOW);
		}
		else if(clawTypes == ClawMoveTypes.UNGRIP_FAST)
		{
			clawMotor.set(OPEN_SPEED_FAST);
		}
		else
		{
			clawMotor.set(0);
		}
	}
}

/**
 * Different movement types for the claw.
 * @author kyle
 *
 */
enum ClawMoveTypes
{
	GRIP_SLOW, GRIP_FAST, UNGRIP_SLOW, UNGRIP_FAST, STOP
}