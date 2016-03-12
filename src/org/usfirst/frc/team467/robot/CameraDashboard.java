package org.usfirst.frc.team467.robot;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.DrawMode;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ShapeMode;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.vision.AxisCamera;
import edu.wpi.first.wpilibj.vision.USBCamera;

public class CameraDashboard extends Thread
{
    private static final Logger LOGGER = Logger.getLogger(CameraDashboard.class);
    
    private static final float BLACK = color(0, 0, 0);
    private static final float RED = color(255, 0, 0);
    private static final float GREEN = color(0, 255, 0);
    @SuppressWarnings("unused")
    private static final float BLUE = color(0, 0, 255);
    private static final float WHITE = color(255, 255, 255);
    
    private final DriverStation libStation;
    private final DriverStation2016 customStation;
    private final VisionProcessor vision;
    
    private static CameraDashboard instance;
    
    private Steering flSteering;
    private Steering frSteering;
    private Steering blSteering;
    private Steering brSteering;
    final double maxTurns = Steering.getMaxTurns();

    private boolean cameraExists = false;
    
    private long lastTimeStamp = System.currentTimeMillis();
    
    Image frame;
    CameraServer cameraServer;
    private USBCamera driveCam;
    private AxisCamera shooterCam;
    private CamView view;

    private CameraDashboard()
    {
        libStation = DriverStation.getInstance();
        customStation = DriverStation2016.getInstance();
        vision = VisionProcessor.getInstance();
        frame = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);
//        initAxisCamera();
        initUSBCamera();
    }

    public static CameraDashboard getInstance()
    {
        if (instance == null)
        {
            // Default view drive
            instance = new CameraDashboard();
        }
        return instance;
    }
    
    public void setDrive(Driveable drive)
    {
        if (drive instanceof SwerveDrive)
        {
            SwerveDrive swerve = (SwerveDrive)drive;
            flSteering = swerve.steering[RobotMap.FRONT_LEFT];
            frSteering = swerve.steering[RobotMap.FRONT_RIGHT];
            blSteering = swerve.steering[RobotMap.BACK_LEFT];
            brSteering = swerve.steering[RobotMap.BACK_RIGHT];
        }
    }

    public boolean cameraExists()
    {
        return cameraExists;
    }

    private void initUSBCamera()
    {
        try
        {
            driveCam = new USBCamera("cam1");
            driveCam.setFPS(30);
            driveCam.openCamera();
            driveCam.startCapture();
            cameraServer = CameraServer.getInstance();
//            cameraServer.setQuality(50);
            
            // the camera name (ex "cam0") can be found through the roborio web interface
            cameraServer.startAutomaticCapture(driveCam);
            
            cameraExists = true;
            LOGGER.debug("Camera initialized");
        }
        catch (Exception e)
        {
            LOGGER.info("No camera detected: " + e.getMessage());
            cameraExists = false;
        }
    }
    
    private void initAxisCamera()
    {
        try
        {
            shooterCam = new AxisCamera("169.254.15.123"); // TODO Use actual IP
            cameraExists = true;
        }
        catch (Exception e)
        {
            LOGGER.info("No camera detected: " + e.getMessage());
            cameraExists = false;
        }
    }

    public void renderImage()
    {
        // TODO Need to make this not crash
//        ImageInfo info = NIVision.imaqGetImageInfo(frame);
//        int viewWidth = info.xRes; // 640
//        int viewHeight = info.yRes; // 480
        final int driveCamWidth    = 640;
        final int driveCamHeight   = 480;
        final int shooterCamWidth  = 320;
        final int shooterCamHeight = 240;
        
        view = customStation.getView();
        
        switch (view)
        {
            case SWERVE:
                driveCam.getImage(frame);
                drawCrossHairs(driveCamWidth, driveCamHeight);
                drawAngleMonitors(driveCamWidth, driveCamHeight);

                if (libStation.isEnabled())
                {
                    drawTimerBar(driveCamWidth, driveCamHeight);
                }
                break;
            case SHOOTER:
//                shooterCam.getImage(frame);
                driveCam.getImage(frame);
                drawCrossHairs(shooterCamWidth, shooterCamHeight);
                drawWidestContour(shooterCamWidth, shooterCamHeight);
                if (libStation.isEnabled())
                {
                    drawTimerBar(shooterCamWidth, shooterCamHeight);
                }
                break;
            case TANK:
                driveCam.getImage(frame);
                drawCrossHairs(driveCamWidth, driveCamHeight);
                drawWidestContour(shooterCamWidth, shooterCamHeight);
                
                if (libStation.isEnabled())
                {
                    drawTimerBar(driveCamWidth, driveCamHeight);
                }
                break;
        }
        cameraServer.setImage(frame);
    }

    /**
     * Color values are from 0 to 255<br>
     * e.g. white = (255, 255, 255)
     * 
     * @param r - The red value
     * @param g - The green value
     * @param b - The blue value
     * @return The float for newPixelValue argument in NIVision drawing functions
     */
    private static float color(int r, int g, int b)
    {
        // 24 bits, 8 bits for each color channel
        return b*256*256 + g*256 + r;
    }
    
    /**
     * Draws Timer Bar on top of Camera Feed
     * 
     * @param viewWidth
     * @param viewHeight
     */
    private void drawTimerBar(int viewWidth, int viewHeight)
    {
        final ShapeMode RECT = ShapeMode.SHAPE_RECT;
        int height = 20;
        double matchTime = 0.0;

        try
        {
            matchTime = DriverStation.getInstance().getMatchTime();
        }
        catch (Exception e)
        {
            LOGGER.error("Can't get match time: " + e.getMessage());
            matchTime = 0.0;
        }

        double totalTime = 135; // Teleop
        if (libStation.isAutonomous())
        {
            totalTime = 15;
        }
        
        double scale = viewWidth / totalTime;
        double elapsedTime = (totalTime - matchTime);
        
        if (elapsedTime < 0) {
            LOGGER.warn("elapsedTime is negative: " + elapsedTime);
            elapsedTime = 0;
        }
        
        double barWidth = elapsedTime * scale;
        
        
        NIVision.Rect timerRect = new NIVision.Rect(0, 0, height, (int)barWidth);
        
        if (matchTime < 20)
        {
            // Final 20 seconds: No throwing noodles!
            NIVision.imaqDrawShapeOnImage(frame, frame, timerRect, DrawMode.PAINT_VALUE, RECT, RED);
        }
        else
        {
            NIVision.imaqDrawShapeOnImage(frame, frame, timerRect, DrawMode.PAINT_VALUE, RECT, GREEN);
        }
    }


    private void drawCrossHairs(int viewWidth, int viewHeight)
    {
        final ShapeMode RECT = ShapeMode.SHAPE_RECT;
        
        NIVision.Rect vertBlack = new NIVision.Rect(viewHeight / 2 - 40, viewWidth / 2 - 2, 80, 5);
        NIVision.Rect vertWhite = new NIVision.Rect(viewHeight / 2 - 40, viewWidth / 2 - 1, 80, 3);
        
        NIVision.Rect horizBlack = new NIVision.Rect(viewHeight / 2 - 2, viewWidth / 2 - 40, 5, 80);
        NIVision.Rect horizWhite = new NIVision.Rect(viewHeight / 2 - 1, viewWidth / 2 - 40, 3, 80);

        NIVision.imaqDrawShapeOnImage(frame, frame, vertBlack, DrawMode.PAINT_VALUE, RECT, BLACK);
        NIVision.imaqDrawShapeOnImage(frame, frame, horizBlack, DrawMode.PAINT_VALUE, RECT, BLACK);
        
        NIVision.imaqDrawShapeOnImage(frame, frame, vertWhite, DrawMode.PAINT_VALUE, RECT, WHITE);
        NIVision.imaqDrawShapeOnImage(frame, frame, horizWhite, DrawMode.PAINT_VALUE, RECT, WHITE);
    }

    private void drawAngleMonitors(int viewWidth, int viewHeight)
    {
        final int barWidth = 15;
        final int rectHeight = 20;
        final int rectWidth = 100 + barWidth;

        final int margin = 20;
        final int leftMargin = 20;
        final int bottomMargin = viewHeight - (margin + rectHeight);
        final int topMargin = margin + 20;
        final int rightMargin = viewWidth - (leftMargin + rectWidth) - barWidth;

        final ShapeMode RECT = ShapeMode.SHAPE_RECT;
        
        NIVision.Rect flRect = new NIVision.Rect(topMargin, leftMargin, rectHeight, rectWidth);
        NIVision.Rect flRect2 = new NIVision.Rect(topMargin-1, leftMargin-1, rectHeight+2, rectWidth+2);
        
        NIVision.Rect frRect = new NIVision.Rect(topMargin, rightMargin, rectHeight, rectWidth);
        NIVision.Rect frRect2 = new NIVision.Rect(topMargin-1, rightMargin-1, rectHeight+2, rectWidth+2);
        
        NIVision.Rect blRect = new NIVision.Rect(bottomMargin, leftMargin, rectHeight, rectWidth);
        NIVision.Rect blRect2 = new NIVision.Rect(bottomMargin-1, leftMargin-1, rectHeight+2, rectWidth+2);
        
        NIVision.Rect brRect = new NIVision.Rect(bottomMargin, rightMargin, rectHeight, rectWidth);
        NIVision.Rect brRect2 = new NIVision.Rect(bottomMargin-1, rightMargin-1, rectHeight+2, rectWidth+2);
        
        NIVision.imaqDrawShapeOnImage(frame, frame, flRect, DrawMode.DRAW_VALUE, RECT, WHITE);
        NIVision.imaqDrawShapeOnImage(frame, frame, flRect2, DrawMode.DRAW_VALUE, RECT, BLACK);
        
        NIVision.imaqDrawShapeOnImage(frame, frame, frRect, DrawMode.DRAW_VALUE, RECT, WHITE);
        NIVision.imaqDrawShapeOnImage(frame, frame, frRect2, DrawMode.DRAW_VALUE, RECT, BLACK);
        
        NIVision.imaqDrawShapeOnImage(frame, frame, blRect, DrawMode.DRAW_VALUE, RECT, WHITE);
        NIVision.imaqDrawShapeOnImage(frame, frame, blRect2, DrawMode.DRAW_VALUE, RECT, BLACK);

        NIVision.imaqDrawShapeOnImage(frame, frame, brRect, DrawMode.DRAW_VALUE, RECT, WHITE);
        NIVision.imaqDrawShapeOnImage(frame, frame, brRect2, DrawMode.DRAW_VALUE, RECT, BLACK);
        
        double flSteeringAngle = flSteering.getSteeringAngle();
        double frSteeringAngle = frSteering.getSteeringAngle();
        double blSteeringAngle = blSteering.getSteeringAngle();
        double brSteeringAngle = brSteering.getSteeringAngle();

        // Full Rotation * Max turns in 2 directions
        int flSteeringPosition = (int) (flSteeringAngle * (100 / ((2 * Math.PI) * (maxTurns * 2))) + 50);
        int frSteeringPosition = (int) (frSteeringAngle * (100 / ((2 * Math.PI) * (maxTurns * 2))) + 50);
        int blSteeringPosition = (int) (blSteeringAngle * (100 / ((2 * Math.PI) * (maxTurns * 2))) + 50);
        int brSteeringPosition = (int) (brSteeringAngle * (100 / ((2 * Math.PI) * (maxTurns * 2))) + 50);

        float flColor = (Math.abs(flSteeringAngle) < (maxTurns * Math.PI * 2) - Math.PI) ? BLACK : RED;
        float frColor = (Math.abs(frSteeringAngle) < (maxTurns * Math.PI * 2) - Math.PI) ? BLACK : RED; 
        float blColor = (Math.abs(blSteeringAngle) < (maxTurns * Math.PI * 2) - Math.PI) ? BLACK : RED; 
        float brColor = (Math.abs(brSteeringAngle) < (maxTurns * Math.PI * 2) - Math.PI) ? BLACK : RED; 

        
        NIVision.Rect flBar = new NIVision.Rect(topMargin, leftMargin + flSteeringPosition, rectHeight, barWidth);
        NIVision.Rect frBar = new NIVision.Rect(topMargin, rightMargin + frSteeringPosition, rectHeight, barWidth);
        NIVision.Rect blBar = new NIVision.Rect(bottomMargin, leftMargin + blSteeringPosition, rectHeight, barWidth);
        NIVision.Rect brBar = new NIVision.Rect(bottomMargin, rightMargin + brSteeringPosition, rectHeight, barWidth);

        NIVision.imaqDrawShapeOnImage(frame, frame, flBar, DrawMode.PAINT_VALUE, RECT, flColor);
        NIVision.imaqDrawShapeOnImage(frame, frame, frBar, DrawMode.PAINT_VALUE, RECT, frColor);
        NIVision.imaqDrawShapeOnImage(frame, frame, blBar, DrawMode.PAINT_VALUE, RECT, blColor);
        NIVision.imaqDrawShapeOnImage(frame, frame, brBar, DrawMode.PAINT_VALUE, RECT, brColor);
    }
    
    private void drawWidestContour(int viewWidth, int viewHeight)
    {
        final ShapeMode RECT = ShapeMode.SHAPE_RECT;
        final List<VisionProcessor.Contour> contours = vision.getContours();
        final VisionProcessor.Contour contour = Collections.max(contours, new VisionProcessor.WidthComp());
        NIVision.Rect rect = new NIVision.Rect(contour.getTop(), contour.getLeft(), contour.getHeight(), contour.getWidth());
        NIVision.imaqDrawShapeOnImage(frame, frame, rect, DrawMode.DRAW_VALUE, RECT, color(255, 255, 255));
    }

    @Override
    public void run()
    {
        try
        {
            final double UPDATE_FREQ = 5; // Updates per second
            final long period = (long) (1000 / UPDATE_FREQ); // Update period in milliseconds
            
            while (true)
            {
                long startTime = System.currentTimeMillis();
                LOGGER.trace("delta=" + (startTime - lastTimeStamp));
                
                lastTimeStamp = startTime;

                // Do the actual work.
                if (cameraExists)
                {   
                    try
                    {
//                        renderImage();
                    }
                    catch (Exception e)
                    {
                        LOGGER.error("Couldn't render image: " + e.getMessage());
                    }
                }

                // Sleep until next scheduled render time.
                long endTime = System.currentTimeMillis();
                long deltaTime = endTime - startTime;
                try
                {
                    // only sample camera at a fixed interval
                    long sleepTime = (long)period - deltaTime;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                }
                catch (InterruptedException e)
                {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Unexpected exception in run: " + e.getMessage());
        }
    }
    
    static enum CamView
    {
        SWERVE, SHOOTER, TANK
    }
}
