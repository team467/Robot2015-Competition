package org.usfirst.frc.team467.robot;

import org.apache.log4j.Logger;
import org.usfirst.frc.team467.robot.Robot.Scores;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.DrawMode;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ParticleFilterCriteria2;
import com.ni.vision.NIVision.ParticleFilterOptions2;
import com.ni.vision.NIVision.Point;
import com.ni.vision.NIVision.Range;
import com.ni.vision.NIVision.ShapeMode;

import edu.wpi.first.wpilibj.CameraServer;

public class Dashboard
{
    private static final Logger LOGGER = Logger.getLogger(Dashboard.class);
    static Dashboard instance;
    Steering flSteering;
    Steering frSteering;
    Steering blSteering;
    Steering brSteering;
    
    private boolean cameraExists = true;

    Image frame;
    CameraServer cameraServer;
    int session;

    private Dashboard()
    {
        Drive drive = Drive.getInstance();
        flSteering = drive.steering[RobotMap.FRONT_LEFT];
        frSteering = drive.steering[RobotMap.FRONT_RIGHT];
        blSteering = drive.steering[RobotMap.BACK_LEFT];
        brSteering = drive.steering[RobotMap.BACK_RIGHT];
        
        initCamera();
    }
    
    public static Dashboard getInstance()
    {
        if (instance == null)
        {
            instance = new Dashboard();
        }
        return instance;
    }
    
    public boolean cameraExists()
    {
        return cameraExists;
    }

    private void initCamera()
    {
        try
        {
            cameraExists = true;
            cameraServer = CameraServer.getInstance();
            cameraServer.setQuality(50);

            frame = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);

            // the camera name (ex "cam0") can be found through the roborio web interface
            session = NIVision.IMAQdxOpenCamera("cam1", NIVision.IMAQdxCameraControlMode.CameraControlModeController);
            NIVision.IMAQdxConfigureGrab(session);
        }
        catch (Exception e)
        {
            LOGGER.info("No Camera Detected: " + e.getMessage());
            cameraExists = false;
        }
    }
    
    public void renderImage()
    {
        if (cameraExists)
        {
            int viewWidth = 640;
            int viewHeight = 480;
            
            NIVision.IMAQdxGrab(session, frame, 1);
            
            drawCrossHairs(viewWidth, viewHeight);
            drawAngleMonitors(viewWidth, viewHeight);
        
            cameraServer.setImage(frame);
        }
    }
    private void drawCrossHairs(int viewWidth, int viewHeight)
    {
        NIVision.Point vertStart = new Point(viewWidth / 2, viewHeight / 2 - 5);
        NIVision.Point vertEnd = new Point(viewWidth / 2, viewHeight / 2 + 5);
    
        NIVision.Point horizStart = new Point(viewWidth / 2 - 5, viewHeight / 2);
        NIVision.Point horizEnd = new Point(viewWidth / 2 + 5, viewHeight / 2);
    
        NIVision.imaqDrawLineOnImage(frame, frame, DrawMode.DRAW_VALUE, vertStart, vertEnd, 0.0f);
        NIVision.imaqDrawLineOnImage(frame, frame, DrawMode.DRAW_VALUE, horizStart, horizEnd, 0.0f);
    }

    private void drawAngleMonitors(int viewWidth, int viewHeight)
    {
        final int barWidth = 15;
        final int rectHeight = 20;
        final int rectWidth = 100 + barWidth;

        final int topMargin = 20;
        final int leftMargin = 20;
        final int bottomMargin = viewHeight - (topMargin + rectHeight);
        final int rightMargin = viewWidth - (leftMargin + rectWidth) - barWidth;
        
        ShapeMode shape = ShapeMode.SHAPE_RECT;
        NIVision.Rect flRect = new NIVision.Rect(topMargin, leftMargin, rectHeight, rectWidth);
        NIVision.Rect frRect = new NIVision.Rect(topMargin, rightMargin, rectHeight, rectWidth);
        NIVision.Rect blRect = new NIVision.Rect(bottomMargin, leftMargin, rectHeight, rectWidth);
        NIVision.Rect brRect = new NIVision.Rect(bottomMargin, rightMargin, rectHeight, rectWidth);
    
        NIVision.imaqDrawShapeOnImage(frame, frame, flRect, DrawMode.DRAW_VALUE, shape, 0.0f);
        NIVision.imaqDrawShapeOnImage(frame, frame, frRect, DrawMode.DRAW_VALUE, shape, 0.0f);
        NIVision.imaqDrawShapeOnImage(frame, frame, blRect, DrawMode.DRAW_VALUE, shape, 0.0f);
        NIVision.imaqDrawShapeOnImage(frame, frame, brRect, DrawMode.DRAW_VALUE, shape, 0.0f);

        double flSteeringAngle = flSteering.getSteeringAngle();
        double frSteeringAngle = frSteering.getSteeringAngle();
        double blSteeringAngle = blSteering.getSteeringAngle();
        double brSteeringAngle = brSteering.getSteeringAngle();
                                                        // 2PI = Full Rotation * Max turns in 2 directions 
        int flSteeringPosition = (int)(flSteeringAngle * (100 / ((2 * Math.PI) * (Steering.MAX_TURNS * 2))) + 50);
        int frSteeringPosition = (int)(frSteeringAngle * (100 / ((2 * Math.PI) * (Steering.MAX_TURNS * 2))) + 50);
        int blSteeringPosition = (int)(blSteeringAngle * (100 / ((2 * Math.PI) * (Steering.MAX_TURNS * 2))) + 50);
        int brSteeringPosition = (int)(brSteeringAngle * (100 / ((2 * Math.PI) * (Steering.MAX_TURNS * 2))) + 50);

        NIVision.Rect flBar = new NIVision.Rect(topMargin, leftMargin + flSteeringPosition, rectHeight, barWidth);
        NIVision.Rect frBar = new NIVision.Rect(topMargin, rightMargin + frSteeringPosition, rectHeight, barWidth);
        NIVision.Rect blBar = new NIVision.Rect(bottomMargin, leftMargin + blSteeringPosition, rectHeight, barWidth);
        NIVision.Rect brBar = new NIVision.Rect(bottomMargin, rightMargin + brSteeringPosition, rectHeight, barWidth);

        NIVision.imaqDrawShapeOnImage(frame, frame, flBar, DrawMode.PAINT_VALUE, shape, 0.0f);
        NIVision.imaqDrawShapeOnImage(frame, frame, frBar, DrawMode.PAINT_VALUE, shape, 0.0f);
        NIVision.imaqDrawShapeOnImage(frame, frame, blBar, DrawMode.PAINT_VALUE, shape, 0.0f);
        NIVision.imaqDrawShapeOnImage(frame, frame, brBar, DrawMode.PAINT_VALUE, shape, 0.0f);
    }
}