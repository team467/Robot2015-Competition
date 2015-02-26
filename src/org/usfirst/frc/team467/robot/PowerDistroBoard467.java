package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.PowerDistributionPanel;

public class PowerDistroBoard467
{
    private static PowerDistroBoard467 board = null;

    private final int LIFTER_MOTOR_CHANNEL_BOTTOM = 4;
    private final int LIFTER_MOTOR_CHANNEL_TOP = 6;

    private final int CLAW_MOTOR_CHANNEL = 5;

    private PowerDistributionPanel pdp = null;
    
    private RollingAverage clawAverageCurrent = new RollingAverage(10);
    private RollingAverage lifterAverageCurrentBottom = new RollingAverage(5);
    private RollingAverage lifterAverageCurrentTop = new RollingAverage(5);

    /**
     * Gets the singleton instance of the board.
     * 
     * @return
     */
    public static PowerDistroBoard467 getInstance()
    {
        if (board == null)
        {
            board = new PowerDistroBoard467();
        }
        return board;
    }

    /**
     * Private Constructor
     */
    private PowerDistroBoard467()
    {
        pdp = new PowerDistributionPanel();
    }

    /**
     * Total current of all channels
     * 
     * @return
     */
    public double getTotalCurrent()
    {
        return pdp.getTotalCurrent();
    }
    
    public void update()
    {
        lifterAverageCurrentBottom.add(getCurrent(LIFTER_MOTOR_CHANNEL_BOTTOM));
        lifterAverageCurrentTop.add(getCurrent(LIFTER_MOTOR_CHANNEL_TOP));
        clawAverageCurrent.add(getCurrent(CLAW_MOTOR_CHANNEL));
    }

    /**
     * Current of a specific channel
     * 
     * @param channel
     * @return
     */
    public double getCurrent(int channel)
    {
        return pdp.getCurrent(channel);
    }

    public double getLifterBottomCurrent()
    {
        return lifterAverageCurrentBottom.getAverage();
    }
    
    public double getLifterTopCurrent()
    {
        return lifterAverageCurrentTop.getAverage();
    }

    public double getClawCurrent()
    {
        return clawAverageCurrent.getAverage();
    }

    @Override
    public String toString()
    {
        return String.valueOf(pdp.getVoltage());
    }

}
