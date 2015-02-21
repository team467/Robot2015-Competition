package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.SerialPort;

/**
 * @author nathan
 *
 */
public class Gyro2015
{
    private static final Logger LOGGER = Logger.getLogger(Gyro2015.class);

    private static Gyro2015 gyro2015 = null;

    private double trustedAngle = 0.0;

    private double resetSubtractAngle = 0;

    private final int BAUD_RATE = 57600;

    private SerialPort sp = null;

    /**
     * Singleton instance of the Gyro
     * 
     * @return
     */
    public static Gyro2015 getInstance()
    {
        if (gyro2015 == null)
        {
            gyro2015 = new Gyro2015(0);
        }
        return gyro2015;
    }

    /**
     * 2015 Analog Gyro with filtering.
     * 
     * @param port
     *            : Analog port gyro is connected to
     */
    private Gyro2015(int port)
    {
        try
        {
            sp = new SerialPort(BAUD_RATE, SerialPort.Port.kUSB);
        }
        catch (Exception ex)
        {
            // eaten
        }
    }

    StringBuilder stringBuffer = new StringBuilder();

    private double getSerialPortAngle()
    {
        // catch in case Serial Port not working.
        if (sp == null)
        {
            try
            {
                sp = new SerialPort(BAUD_RATE, SerialPort.Port.kUSB);
            }
            catch (Exception ex)
            {
                // eaten
            }
            return trustedAngle;
        }
        else
        {
            String data = sp.readString();
            stringBuffer.append(data);
            int startIndex = 0;
            int endIndex = 0;

            // if contains first \n, assign to startIndex
            if ((startIndex = stringBuffer.indexOf("\n")) >= 0
            // if startIndex is not the last index of the string
                    && stringBuffer.length() - 1 > startIndex
                    // if contains second \n, assign to endIndex
                    && (endIndex = stringBuffer.indexOf("\n", startIndex + 1)) >= 0)
            {
                String dataSubstring = stringBuffer.substring(startIndex, endIndex);
                stringBuffer.delete(0, stringBuffer.length() - 1);
                try
                {
                    double val = Double.parseDouble(dataSubstring);
                    LOGGER.debug("RAW DATA: " + data + " PARSED: " + val);
                    return val - resetSubtractAngle;
                }
                catch (Exception ex)
                {
                    LOGGER.debug("RAW DATA: " + stringBuffer.toString() + " PARSED: FAILED!!!!!!!!!!!!!!!!!");
                    return trustedAngle;
                }
            }
            else
            {
                LOGGER.debug("RAW DATA: " + stringBuffer.toString() + " PARSED: good data not found");
                return trustedAngle;
            }
        }

    }

    public void update()
    {
        trustedAngle = wrapAngle(getSerialPortAngle());
    }

    public double getAngle()
    {
        return trustedAngle;
    }

    /**
     * Resets the angle to the gyro to upfield.
     */
    public void reset()
    {
        reset(GyroResetDirection.FACE_AWAY);
    }
    
    /**
     * Resets the angle of the gyro to the given orientation.
     * @param dir - Direction the robot is *CURRENTLY* facing relative to the teleop location.
     */
    public void reset(GyroResetDirection dir)
    {
        double additionalResetAngle = 0;
        
        switch(dir)
        {
            case FACE_AWAY:
                additionalResetAngle = 0;
                break;
            case FACE_TOWARD:
                additionalResetAngle = 180;
                break;
            case FACE_LEFT:
                additionalResetAngle = 90;
                break;                
            case FACE_RIGHT:
                additionalResetAngle = 270;
                break;
        }
        resetSubtractAngle = wrapAngle(trustedAngle + resetSubtractAngle + additionalResetAngle);
    }

    public double wrapAngle(double val)
    {
        double newVal = val % 360;
        return (newVal < 0) ? 360 + newVal : newVal;
    }

    @Override
    public String toString()
    {
        return "GYRO";
//        return String.format("Gyro2015 [trustedAngle=%-7.2f deg, deltaAngle=%-7.3f deg/sec]", trustedAngle, deltaAngle / deltaTime);
    }

}

enum GyroResetDirection
{
    FACE_AWAY, FACE_TOWARD, FACE_LEFT, FACE_RIGHT
}