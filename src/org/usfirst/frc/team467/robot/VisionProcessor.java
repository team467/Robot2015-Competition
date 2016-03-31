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
    private List<Contour> list = new ArrayList<Contour>();
//    private double height;
    private double width = 0.0;
    private boolean isEnabled = false;
    
    private VisionProcessor()
    {   
        setupTables();
        LOGGER.debug("Got GRIP table: " + contourTable);
        width = sizeTable.getNumber("x", 0.0);
    }

    private void setupTables()
    {
        if (contourTable == null) {
            contourTable = NetworkTable.getTable("GRIP/contoursReport");
        }
        if (sizeTable == null)
        {
            sizeTable = NetworkTable.getTable("GRIP/mySize");
        }
        if (sizeTable != null)
        {
            width = sizeTable.getNumber("x", 0.0);
        }
        isEnabled = (sizeTable != null && contourTable != null);
    }
    
    public boolean isEnabled()
    {
        return isEnabled;
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
        if (width == 0.0)
        {
            throw new IllegalStateException("width is zero");
        }
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
            setupTables();
            double[] centerXs = contourTable.getNumberArray("centerX", (double[])null);
            LOGGER.debug("Got centerXs: " + centerXs);
            double[] centerYs = contourTable.getNumberArray("centerY", (double[])null);
            LOGGER.debug("Got centerYs: " + centerYs);
            double[] areas    = contourTable.getNumberArray("area",    (double[])null);
//            LOGGER.debug("Got areas: " + areas);
            double[] heights  = contourTable.getNumberArray("height",  (double[])null);
            LOGGER.debug("Got heights: " + heights);
            double[] widths   = contourTable.getNumberArray("width",   (double[])null);
//            LOGGER.debug("Got widths: " + widths);
            
            List<Contour> list = new ArrayList<Contour>();
//            LOGGER.debug("Made list");

            // If we get no data, return empty list.
            if (widths == null || centerXs == null) {
                LOGGER.debug("Empty contour list");
                return;
            }
            
            else
            {
                for (int i = 0; i < widths.length; i++)
                {
                    try
                    {
//                        Contour contour = new Contour(centerXs[i], centerYs[i], areas[i], heights[i], widths[i]);
//                        Contour contour = new Contour(centerXs[i], widths[i]);
                        Contour contour = new Contour(centerXs[i], widths[i], centerYs[i], heights[i]);

//                        Contour contour = new Contour(centerXs[i], centerYs[i], areas[i], 0.0,0.0);
                        list.add(contour);
                    LOGGER.debug("Contour " + i + ": " + list.get(i));
                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        LOGGER.warn(e.getMessage());
                    }
                }
    
                this.list = list;
                LOGGER.debug("Found " + list.size() + " contours");
            }
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
                   ", centerY="        + centerY +
//                   ", area="           + area    +
                   ", height="         + height  +
                   ", width="          + width   + "]";
        }

        private final double centerX;
        private final double centerY;
        private final double top;
        private final double left;
//        private final double area;
        private final double height;
        private final double width;
        
        public Contour(double centerX, double width, double centerY, double height)
        {
            this.centerX = centerX;
            this.centerY = centerY;
            this.top = centerY - height/2;
            this.left = centerX - width/2;
//            this.area = area;
            this.height = height;
            this.width = width;
        }
        
        public int getCenterX()
        {
            return (int)centerX;
        }

        public int getCenterY()
        {
            return (int)centerY;
        }
//
//        public double getArea()
//        {
//            return area;
//        }
//
        public int getHeight()
        {
            return (int)height;
        }

        public int getWidth()
        {
            return (int)width;
        }
        
        public int getTop()
        {
            return (int)top;
        }
        
        public int getLeft()
        {
            return (int)left;
        }
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
