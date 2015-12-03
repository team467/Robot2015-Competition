package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.CANTalon;

public class CANTalonSpeedControl extends CANTalon
{

    private double MAX_SPEED_COMMANDED = 600;

    /**
     * CANTalon motor controller that implements speed control.
     * 
     * @param deviceNumber
     *            - CAN ID of this CANTalon
     * @param reverseSensor
     *            - if to reverse sensor (passes into "this.reverseSensor()")
     */
    public CANTalonSpeedControl(int deviceNumber, boolean reverseSensor)
    {
        super(deviceNumber);
        this.changeControlMode(CANTalon.TalonControlMode.Speed);
        this.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
        this.ClearIaccum();
        this.reverseSensor(reverseSensor);
        this.setPID(2.5// 4 /*P*/
                , 0.00/* I */
                , 0 /* D */
                , 0 /* FeedForward constant */
                , 0 /* iZone - leave zero */
                , 5 /* close loop ramp in volts/sec */
                , 0)/*
                     * Profile (0 or 1) - allows the CANTalon
                     * to switch between two sets of values
                     */;
        this.enableBrakeMode(false);
        this.enableControl();
    }

    @Override
    public void set(double outputValue)
    {
        double feedInSpeed = outputValue * MAX_SPEED_COMMANDED;
        super.set(feedInSpeed);
    }

    @Override
    public void set(double outputValue, byte thisValueDoesNotDoAnything)
    {
        this.set(outputValue);
    }

}
