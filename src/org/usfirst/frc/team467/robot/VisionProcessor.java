package org.usfirst.frc.team467.robot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class VisionProcessor
{
    private static final Logger LOGGER = Logger.getLogger(VisionProcessor.class);
    private NetworkTable contourTable;
    private NetworkTable sizeTable;
    private static VisionProcessor instance;
    private List<Contour> list;
//    private double height;
    private double width;
    
    private VisionProcessor()
    {
        LOGGER.info("Starting GRIP");
        try
        {
            // TODO Fix or remove
            Runtime.getRuntime().exec(new String[]
                    {"/usr/local/frc/JRE/bin/java", "-jar", "grip.jar", "Filter.grip"});
        }
        catch (IOException e)
        {
            LOGGER.error("Grip failed: " + e);
        }
        LOGGER.info("Started GRIP");
        
        contourTable = NetworkTable.getTable("GRIP/contoursReport");
        sizeTable = NetworkTable.getTable("GRIP/mySize");
        LOGGER.info("Got GRIP table: " + contourTable);
        width = sizeTable.getNumber("x", 0.0);
    }
    
    public static VisionProcessor getInstance()
    {
        if (instance == null)
        {
            instance = new VisionProcessor();
        }
        return instance;
    }
    
    public double getHorizontalCenter()
    {
        return width/2;
    }
    
    /**
     * Refreshes the contours list, call once per cycle
     */
    public void updateContours()
    {
        LOGGER.debug("Starting to get contours");
        try
        {
            LOGGER.debug("Trying to get contours");
            double[] centerXs = contourTable.getNumberArray("centerX", (double[])null);
//            LOGGER.debug("Got centerXs: " + centerXs);
//            double[] centerYs = table.getNumberArray("centerY", (double[])null);
//            LOGGER.debug("Got centerYs: " + centerYs);
//            double[] areas    = table.getNumberArray("area",    (double[])null);
//            LOGGER.debug("Got areas: " + areas);
//            double[] heights  = table.getNumberArray("height",  (double[])null);
//            LOGGER.debug("Got heights: " + heights);
            double[] widths   = contourTable.getNumberArray("width",   (double[])null);
//            LOGGER.debug("Got widths: " + widths);
            
            List<Contour> list = new ArrayList<Contour>();
//            LOGGER.debug("Made list");

            // If we get no data, return empty list.
            if (widths == null) {
                LOGGER.debug("Empty contour list");
            }
            
            LOGGER.debug("Found " + widths.length + " contours");
            for (int i = 0; i < widths.length; i++)
            {
                try
                {
//                    Contour contour = new Contour(centerXs[i], centerYs[i], areas[i], heights[i], widths[i]);
                    Contour contour = new Contour(centerXs[i], widths[i]);
//                Contour contour = new Contour(centerXs[i], centerYs[i], areas[i], 0.0,0.0);
                    list.add(contour);
                LOGGER.debug("Contour " + i + ": " + list.get(i));
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    LOGGER.warn(e.getMessage());
                }
            }

            this.list = list;
        }
        catch (Exception e)
        {
            LOGGER.error("updateContours exception: " + e);
            throw e;
        }
    }
    
    /**
     * Gets the contours list
     */
    public List<Contour> getContours()
    {
        return list;
    }
    
    public class Contour
    {
        @Override
        public String toString()
        {
            return "Contour [centerX=" + centerX +
//                   ", centerY="        + centerY +
//                   ", area="           + area    +
//                   ", height="         + height  +
                   ", width="          + width   + "]";
        }

        private final double centerX;
//        private final double centerY;
//        private final double area;
//        private final double height;
        private final double width;
        
        public Contour(double centerX, double width)
        {
            this.centerX = centerX;
//            this.centerY = centerY;
//            this.area = area;
//            this.height = height;
            this.width = width;
        }
        
        public double getCenterX()
        {
            return centerX;
        }

//        public double getCenterY()
//        {
//            return centerY;
//        }
//
//        public double getArea()
//        {
//            return area;
//        }
//
//        public double getHeight()
//        {
//            return height;
//        }

        public double getWidth()
        {
            return width;
        }
        
//        public double getTop()
//        {
//            return centerY - (height / 2.0);
//        }
//        
//        public double getLeft()
//        {
//            return centerX - (width / 2.0);
//        }
    }
    
    public static class WidthComp implements Comparator<Contour>
    {

        @Override
        public int compare(Contour c1, Contour c2)
        {
            if (c1.getWidth() < c2.getWidth()) return -1;
            if (c1.getWidth() > c2.getWidth()) return 1;
            return 0;
        }
        
    }
}
