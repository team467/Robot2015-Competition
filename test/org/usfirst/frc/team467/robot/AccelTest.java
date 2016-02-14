package org.usfirst.frc.team467.robot;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AccelTest
{    
/*   public void sumTest()
    {
        Tilt tilt = new Tilt(null);
        assertEquals(5, tilt.sum(2, 3));
    }*/
    
    private final double MAX_DELTA = 0.0001;
    
    @Test
    
    public void calc(){
        Tilt tilt = new Tilt ();
        
        double x = 0;
        
        /*while (x >= -0.3){
            double alphaTilt = tilt.calc(x, 8.90, 4.32);
            System.out.println(x + " : " + alphaTilt);
            x = x - 0.050;
        }*/
        
        double var = tilt.calc(-0.3, 0, 0);
        assertEquals( 17.457603123722095, var, MAX_DELTA);
        
        var = tilt.calc(-0.05, 0, 0);
        assertEquals( 2.8659839825988622, var, MAX_DELTA);
        
        var = tilt.calc(0, 0, 0);
        assertEquals( 0.0, var, MAX_DELTA);
        
        var = tilt.calc(-0.2, 0, 0);
        assertEquals( 11.536959032815489, var, MAX_DELTA);
    }
}
