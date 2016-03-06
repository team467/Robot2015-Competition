package org.usfirst.frc.team467.robot;
import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.Ultrasonic;

public class Ultrasonic2016
{
    private static final Logger LOGGER = Logger.getLogger(Ultrasonic2016.class);

    private static Ultrasonic2016 instance = null;
    private Ultrasonic frontUltra;
    
    public Ultrasonic2016()
    {
        frontUltra = new Ultrasonic(1, 0);
    }
    
    public static Ultrasonic2016 getInstance()
    {
        if (instance == null)
        {
            instance = new Ultrasonic2016();
        }
        return instance;
    }
    
    public double getFrontRangeInches()
    {
        return frontUltra.getRangeInches();
    }
}
