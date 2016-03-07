package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.PowerDistributionPanel;

public class PowerDistroBoard467
{
    private static final Logger LOGGER = Logger.getLogger(PowerDistroBoard467.class);

    private static PowerDistroBoard467 board = null;

    private PowerDistributionPanel pdp = null;
    
    private RollingAverage manipAverageCurrent = new RollingAverage(5);
    private RollingAverage tBarAverageCurrent = new RollingAverage(5);

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
        manipAverageCurrent.add(getCurrent(RobotMap.MANIPULATOR_POWER_CHANNEL));

        tBarAverageCurrent.add(getCurrent(RobotMap.TBAR_POWER_CHANNEL));

        LOGGER.info("manipAvgCurrent=" + manipAverageCurrent.getAverage()
            + " tBarAverageCurrent=" + tBarAverageCurrent.getAverage());
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
    
    public double getManipCurrent()
    {
        return manipAverageCurrent.getAverage();
    }
    
    public double getTBarCurrent()
    {
        return tBarAverageCurrent.getAverage();
    }

    @Override
    public String toString()
    {
        return String.valueOf(pdp.getVoltage());
    }

}
