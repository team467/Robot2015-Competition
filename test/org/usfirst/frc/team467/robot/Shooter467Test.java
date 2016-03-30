package org.usfirst.frc.team467.robot;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class Shooter467Test
{
    @BeforeClass
    public static void setupBeforeClass()
    {
        Logging.setupUnitTestLogging();
    }
    
    @Test
    public void test() throws InterruptedException
    {
        Shooter467 shooter = new Shooter467(null, null, null, null, null);
        final long started = System.currentTimeMillis();
        while (!shooter.prime(3.0))
        {
            Thread.sleep(20);
        }
        
        final long ended = System.currentTimeMillis();

        Assert.assertTrue(Math.abs(started + 3000 - ended) < 100);
    }
}
