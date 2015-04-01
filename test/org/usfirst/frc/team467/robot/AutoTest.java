package org.usfirst.frc.team467.robot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AutoTest
{
    Autonomous auto;
    
    public void init()
    {
        // TODO Make dummy classes for drive, claw, and lifter implementing respective interfaces
        auto = new Autonomous(Drive.getInstance(), Claw.getInstance(), Lifter.getInstance());
    }
}
