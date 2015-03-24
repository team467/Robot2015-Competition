package org.usfirst.frc.team467.robot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class RateLimiterTest
{
    @Test
    public void zeroToOne()
    {
        assertEquals(0.2, RateLimiter.limit(0.0, 1.0, 0.2), 0.0001);
        assertEquals(0.4, RateLimiter.limit(0.2, 1.0, 0.2), 0.0001);
        assertEquals(0.6, RateLimiter.limit(0.4, 1.0, 0.2), 0.0001);
        assertEquals(0.8, RateLimiter.limit(0.6, 1.0, 0.2), 0.0001);
        assertEquals(1.0, RateLimiter.limit(0.8, 1.0, 0.2), 0.0001);
        assertEquals(1.0, RateLimiter.limit(1.0, 1.0, 0.2), 0.0001);
    }
    
    @Test
    public void zeroToNegativeOne()
    {
        assertEquals(-0.1, RateLimiter.limit(-0.0, -1.0, 0.1), 0.0001);
        assertEquals(-0.2, RateLimiter.limit(-0.1, -1.0, 0.1), 0.0001);
        assertEquals(-0.3, RateLimiter.limit(-0.2, -1.0, 0.1), 0.0001);
        assertEquals(-0.4, RateLimiter.limit(-0.3, -1.0, 0.1), 0.0001);
        assertEquals(-0.5, RateLimiter.limit(-0.4, -1.0, 0.1), 0.0001);
        assertEquals(-0.6, RateLimiter.limit(-0.5, -1.0, 0.1), 0.0001);
        assertEquals(-0.7, RateLimiter.limit(-0.6, -1.0, 0.1), 0.0001);
        assertEquals(-0.8, RateLimiter.limit(-0.7, -1.0, 0.1), 0.0001);
        assertEquals(-0.9, RateLimiter.limit(-0.8, -1.0, 0.1), 0.0001);
        assertEquals(-1.0, RateLimiter.limit(-0.9, -1.0, 0.1), 0.0001);
        assertEquals(-1.0, RateLimiter.limit(-1.0, -1.0, 0.1), 0.0001);
        
        // Negative ramp-rate also works
        assertEquals(-1.0, RateLimiter.limit(-1.0, -1.0, -0.1), 0.0001);
    }
    
    @Test
    public void changeNotTooBig()
    {
        assertEquals(1.0, RateLimiter.limit(0.9, 1.0, 0.3), 0.0001);
        assertEquals(0.7, RateLimiter.limit(0.6, 0.7, 0.5), 0.0001);
        
        assertEquals(0.6, RateLimiter.limit(0.7, 0.6, 0.3), 0.0001);
        assertEquals(0.8, RateLimiter.limit(1.0, 0.8, 0.3), 0.0001);
    }
}
