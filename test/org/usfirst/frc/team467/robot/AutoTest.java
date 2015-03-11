package org.usfirst.frc.team467.robot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AutoTest
{
    Autonomous auto;
    
    public void init()
    {
        auto = new Autonomous(Drive.getInstance(), Claw.getInstance(), Lifter.getInstance());
    }
}
