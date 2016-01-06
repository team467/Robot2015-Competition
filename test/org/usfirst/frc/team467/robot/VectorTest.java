package org.usfirst.frc.team467.robot;

import static org.junit.Assert.*;

import org.junit.Test;

public class VectorTest
{
    static {
        Logging.initForTest();
    }
    
    @Test
    public void testAdd()
    {
        // Simple XY addition.
        assertEquals(Vector.makeXY(3, 4), Vector.add(Vector.makeXY(1, 3), Vector.makeXY(2, 1)));

        // ...
        assertEquals(Math.PI/2, Vector.add(Vector.makeUnit(Math.PI/2), Vector.makeUnit(Math.PI/2)).getAngle(), 0);

        // ...
        assertEquals(Vector.makeSpeedAngle(Math.sqrt(2.0), Math.PI/2),
                Vector.add(Vector.makeUnit(Math.PI/4), Vector.makeUnit(Math.PI * (3.0/4.0))));
    }
}
