package org.usfirst.frc.team467.robot;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class Logging
{
    public static void init()
    {
        setupDefaultLogging(true);

        // Enable extra logging for classes you want to debug
//        Logger.getLogger(Robot.class).setLevel(Level.DEBUG);
//        Logger.getLogger(Steering.class).setLevel(Level.INFO);
//        Logger.getLogger(Calibration.class).setLevel(Level.DEBUG);
//        Logger.getLogger(Drive.class).setLevel(Level.DEBUG);
//        Logger.getLogger(Lifter.class).setLevel(Level.DEBUG);
//        Logger.getLogger(Claw.class).setLevel(Level.DEBUG);
//        Logger.getLogger(Autonomous.class).setLevel(Level.DEBUG);
//        Logger.getLogger(Gyro2015.class).setLevel(Level.DEBUG);
        Logger.getLogger(CameraDashboard.class).setLevel(Level.DEBUG);
        Logger.getLogger(WheelPod.class).setLevel(Level.DEBUG);
    }
    
    public static void initForTest()
    {
        setupDefaultLogging(false);
    }
    
    private static void setupDefaultLogging(boolean shouldUseFileLogger)
    {
        // Create a logging appender that writes our pattern to the console.
        // Our pattern looks like the following:
        // 42ms INFO MyClass - This is my info message
        String pattern = "%rms %p %c - %m%n";
        PatternLayout layout = new PatternLayout(pattern);
        Logger.getRootLogger().addAppender(new ConsoleAppender(layout));
        
        if (shouldUseFileLogger) {
            try
            {
                RollingFileAppender rollingFileAppender = new RollingFileAppender(layout, "/home/admin/Robot467.log");
                rollingFileAppender.setMaxBackupIndex(20);
                rollingFileAppender.setMaximumFileSize(1_000_000);
                rollingFileAppender.rollOver();
                Logger.getRootLogger().addAppender(rollingFileAppender);
            }
            catch (IOException e)
            {
                System.out.println("Failed to create log file appender: " + e.getMessage());
            }
        }

        // Set the default log level to INFO.
        Logger.getRootLogger().setLevel(Level.INFO); // changing log level
    }
}
