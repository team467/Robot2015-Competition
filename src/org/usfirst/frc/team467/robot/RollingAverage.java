package org.usfirst.frc.team467.robot;

public class RollingAverage {

    private int size;
    private double total = 0d;
    private int index = 0;
    private double samples[];
    
    /**
     * Creates a new averaging list of a certain size
     * 
     * @param size - Amount of values in the averaging list
     */
    public RollingAverage(int size)
    {
        // Increase size to class scope
        this.size = size;
        
        samples = new double[size];
        
        // Fill every index with 0
        for (int i = 0; i < size; i++)
        {
            samples[i] = 0d;
        }
    }
    
    /**
     * Adds a value to the averaging list
     * 
     * @param x - The value to add
     */
    public void add(double x)
    {
        // Remove last value from total
        total -= samples[index];
        
        // Swap old value with new value
        samples[index] = x;
        
        // Add new value to total
        total += x;
        if (++index == size)
        {
            index = 0; // cheaper than modulus
        }
    }
    
    /**
     * @return The current rolling average
     */
    public double getAverage()
    {
        return total / size;
    }   
}
