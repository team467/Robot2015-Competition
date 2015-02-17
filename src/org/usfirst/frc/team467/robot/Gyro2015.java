package org.usfirst.frc.team467.robot;

import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.SerialPort;

/**
 * @author nathan
 *
 */
public class Gyro2015
{
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
        sp = new SerialPort(BAUD_RATE, SerialPort.Port.kUSB);
    }

    private double getSerialPortAngle()
    {
        String data = sp.readString();
        String[] valueArray = data.split("\n");

        if (valueArray.length > 0)
        {
            String value = valueArray[valueArray.length - 1];
            try
            {
                return Double.parseDouble(value);
            }
            catch (Exception ex)
            {
                // eat exception
            }
        }
        return trustedAngle;
    }

    public void update()
    {
        trustedAngle = wrapAngle(getSerialPortAngle() - resetSubtractAngle);
    }
    
    public double getAngle()
    {
        return trustedAngle;
    }

    public void reset()
    {
        resetSubtractAngle = wrapAngle(trustedAngle + resetSubtractAngle);
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