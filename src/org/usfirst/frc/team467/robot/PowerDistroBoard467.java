package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.PowerDistributionPanel;

public class PowerDistroBoard467
{

    private static PowerDistroBoard467 board = null;

    private PowerDistributionPanel pdp = null;

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

    @Override
    public String toString()
    {
        return String.valueOf(pdp.getVoltage());
    }

}
