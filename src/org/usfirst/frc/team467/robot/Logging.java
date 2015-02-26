package org.usfirst.frc.team467.robot;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Logging
{
    public static void init()
    {
        setupDefaultLogging();

        // Enable extra logging for classes you want to debug
//        Logger.getLogger(Robot.class).setLevel(Level.DEBUG);
//        Logger.getLogger(Steering.class).setLevel(Level.INFO);
//        Logger.getLogger(Calibration.class).setLevel(Level.DEBUG);
//        Logger.getLogger(Drive.class).setLevel(Level.DEBUG);
        Logger.getLogger(Lifter.class).setLevel(Level.DEBUG);
//        Logger.getLogger(Claw.class).setLevel(Level.DEBUG);
//        Logger.getLogger(Gyro2015.class).setLevel(Level.DEBUG);
    }

    private static void setupDefaultLogging()
    {
        // Create a logging appender that writes our pattern to the console.
        // Our pattern looks like the following:
        // 42ms INFO MyClass - This is my info message
        String pattern = "%rms %p %c - %m%n";
        PatternLayout layout = new PatternLayout(pattern);
        Logger.getRootLogger().addAppender(new ConsoleAppender(layout));

        // Set the default log level to INFO.
        Logger.getRootLogger().setLevel(Level.INFO); // changing log level
    }
}
