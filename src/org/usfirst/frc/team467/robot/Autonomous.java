package org.usfirst.frc.team467.robot;

import java.util.Comparator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ImageType;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Autonomous
{

    private static final Logger LOGGER = Logger.getLogger(Robot.class);

    int session;
    Image frame;

    CameraServer cameraServer = null;

    boolean useCamera = true;

    private static Autonomous autonomous = null;

    private Drive drive = null;

    private AutoType autonomousType = AutoType.DRIVE_ONLY;

    /**
     * Gets a singleton instance of the Autonomous
     * 
     * @return Autonomous object
     */

    public class ParticleReport implements Comparator<ParticleReport>, Comparable<ParticleReport>
    {
        double PercentAreaToImageArea;
        double Area;
        double BoundingRectLeft;
        double BoundingRectTop;
        double BoundingRectRight;
        double BoundingRectBottom;

        public int compareTo(ParticleReport r)
        {
            return (int) (r.Area - this.Area);
        }

        public int compare(ParticleReport r1, ParticleReport r2)
        {
            return (int) (r1.Area - r2.Area);
        }
    };

    // Structure to represent the scores for the various tests used for target
    // identification
    public class Scores
    {
        double Area;
        double Aspect;
    };

    // Images
    Image binaryFrame;
    int imaqError;

    // Constants
    NIVision.Range TOTE_HUE_RANGE = new NIVision.Range(101, 64); // Default hue
                                                                 // range for
                                                                 // yellow
                                                                 // tote
    NIVision.Range TOTE_SAT_RANGE = new NIVision.Range(88, 255); // Default
                                                                 // saturation
                                                                 // range for
                                                                 // yellow
                                                                 // tote
    NIVision.Range TOTE_VAL_RANGE = new NIVision.Range(134, 255); // Default
                                                                  // value
                                                                  // range for
                                                                  // yellow
                                                                  // tote
    double AREA_MINIMUM = 0.5; // Default Area minimum for particle as a
                               // percentage of total image area
    double LONG_RATIO = 2.22; // Tote long side = 26.9 / Tote height = 12.1 =
                              // 2.22
    double SHORT_RATIO = 1.4; // Tote short side = 16.9 / Tote height = 12.1 =
                              // 1.4
    double SCORE_MIN = 70.0; // Minimum score to be considered a tote
    double VIEW_ANGLE = 49.4; // View angle fo camera, set to Axis m1011 by
                              // default, 64 for m1013, 51.7 for 206, 52 for
                              // HD3000 square, 60 for HD3000 640x480
    NIVision.ParticleFilterCriteria2 criteria[] = new NIVision.ParticleFilterCriteria2[1];
    NIVision.ParticleFilterOptions2 filterOptions = new NIVision.ParticleFilterOptions2(0, 0, 1, 1);
    Scores scores = new Scores();

    public void cameraSetup()
    {

        // create images
        frame = NIVision.imaqCreateImage(ImageType.IMAGE_RGB, 0);
        binaryFrame = NIVision.imaqCreateImage(ImageType.IMAGE_U8, 0);
        criteria[0] = new NIVision.ParticleFilterCriteria2(NIVision.MeasurementType.MT_AREA_BY_IMAGE_AREA, AREA_MINIMUM, 100.0, 0,
                0);

        // Put default values to SmartDashboard so fields will appear
        SmartDashboard.putNumber("Tote hue min", TOTE_HUE_RANGE.minValue);
        SmartDashboard.putNumber("Tote hue max", TOTE_HUE_RANGE.maxValue);
        SmartDashboard.putNumber("Tote sat min", TOTE_SAT_RANGE.minValue);
        SmartDashboard.putNumber("Tote sat max", TOTE_SAT_RANGE.maxValue);
        SmartDashboard.putNumber("Tote val min", TOTE_VAL_RANGE.minValue);
        SmartDashboard.putNumber("Tote val max", TOTE_VAL_RANGE.maxValue);
        SmartDashboard.putNumber("Area min %", AREA_MINIMUM);
    }

    public boolean cameraPeriodic()
    {
        boolean isTote = false;
        if (useCamera)
        {
            NIVision.IMAQdxGrab(session, frame, 1);

            // Update threshold values from SmartDashboard. For performance
            // reasons it is recommended to remove this after calibration is
            // finished.
            TOTE_HUE_RANGE.minValue = (int) SmartDashboard.getNumber("Tote hue min", TOTE_HUE_RANGE.minValue);
            TOTE_HUE_RANGE.maxValue = (int) SmartDashboard.getNumber("Tote hue max", TOTE_HUE_RANGE.maxValue);
            TOTE_SAT_RANGE.minValue = (int) SmartDashboard.getNumber("Tote sat min", TOTE_SAT_RANGE.minValue);
            TOTE_SAT_RANGE.maxValue = (int) SmartDashboard.getNumber("Tote sat max", TOTE_SAT_RANGE.maxValue);
            TOTE_VAL_RANGE.minValue = (int) SmartDashboard.getNumber("Tote val min", TOTE_VAL_RANGE.minValue);
            TOTE_VAL_RANGE.maxValue = (int) SmartDashboard.getNumber("Tote val max", TOTE_VAL_RANGE.maxValue);

            // Threshold the image looking for yellow (tote color)
            NIVision.imaqColorThreshold(binaryFrame, frame, 255, NIVision.ColorMode.HSV, TOTE_HUE_RANGE, TOTE_SAT_RANGE,
                    TOTE_VAL_RANGE);

            // Send particle count to dashboard
            int numParticles = NIVision.imaqCountParticles(binaryFrame, 1);
            SmartDashboard.putNumber("Masked particles", numParticles);

            // Send masked image to dashboard to assist in tweaking mask.
            cameraServer.setImage(binaryFrame);

            // filter out small particles
            float areaMin = (float) SmartDashboard.getNumber("Area min %", AREA_MINIMUM);
            criteria[0].lower = areaMin;
            imaqError = NIVision.imaqParticleFilter4(binaryFrame, binaryFrame, criteria, filterOptions, null);

            // Send particle count after filtering to dashboard
            numParticles = NIVision.imaqCountParticles(binaryFrame, 1);
            SmartDashboard.putNumber("Filtered particles", numParticles);

            if (numParticles > 0)
            {
                // Measure particles and sort by particle size
                Vector<ParticleReport> particles = new Vector<ParticleReport>();
                for (int particleIndex = 0; particleIndex < numParticles; particleIndex++)
                {
                    ParticleReport par = new ParticleReport();
                    par.PercentAreaToImageArea = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0,
                            NIVision.MeasurementType.MT_AREA_BY_IMAGE_AREA);
                    par.Area = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0, NIVision.MeasurementType.MT_AREA);
                    par.BoundingRectTop = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0,
                            NIVision.MeasurementType.MT_BOUNDING_RECT_TOP);
                    par.BoundingRectLeft = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0,
                            NIVision.MeasurementType.MT_BOUNDING_RECT_LEFT);
                    par.BoundingRectBottom = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0,
                            NIVision.MeasurementType.MT_BOUNDING_RECT_BOTTOM);
                    par.BoundingRectRight = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0,
                            NIVision.MeasurementType.MT_BOUNDING_RECT_RIGHT);
                    particles.add(par);

                }

                // particles.sort(null); TODO

                // This example only scores the largest particle. Extending to
                // score all particles and choosing the desired one is left as
                // an exercise
                // for the reader. Note that this scores and reports information
                // about a single particle (single L shaped target). To get
                // accurate information
                // about the location of the tote (not just the distance) you
                // will need to correlate two adjacent targets in order to find
                // the true center of the tote.

                particleLoop: for (ParticleReport pr : particles)
                {
                    scores = new Scores();
                    scores.Aspect = singleLAspectScore(pr);
                    SmartDashboard.putNumber("Aspect", scores.Aspect);
                    scores.Area = singleLAreaScore(pr);
                    SmartDashboard.putNumber("Area", scores.Area);
                    LOGGER.debug("Single L Aspect: " + scores.Aspect + " Area: " + scores.Area);
                    if (scores.Aspect > SCORE_MIN && scores.Area > SCORE_MIN)
                    {
                        isTote = true;
                        break particleLoop;
                    }

                    scores = new Scores();
                    scores.Aspect = doubleLAspectScore(pr);
                    SmartDashboard.putNumber("Aspect", scores.Aspect);
                    scores.Area = doubleLAreaScore(pr);
                    SmartDashboard.putNumber("Area", scores.Area);
                    LOGGER.debug("Double L Aspect: " + scores.Aspect + " Area: " + scores.Area);
                    if (scores.Aspect > SCORE_MIN && scores.Area > SCORE_MIN)
                    {
                        isTote = true;
                        break particleLoop;
                    }
                }

                // Send distance and tote status to dashboard. The bounding
                // rect, particularly the horizontal center (left - right) may
                // be useful for rotating/driving towards a tote
                SmartDashboard.putBoolean("IsTote", isTote);
                SmartDashboard.putNumber("Distance", computeDistance(binaryFrame, particles.elementAt(0)));

            }
            else
            {
                SmartDashboard.putBoolean("IsTote", false);
            }
        }
        return isTote;
    }

    // Comparator function for sorting particles. Returns true if particle 1 is
    // larger
    static boolean CompareParticleSizes(ParticleReport particle1, ParticleReport particle2)
    {
        // we want descending sort order
        return particle1.PercentAreaToImageArea > particle2.PercentAreaToImageArea;
    }

    /**
     * Converts a ratio with ideal value of 1 to a score. The resulting function
     * is piecewise linear going from (0,0) to (1,100) to (2,0) and is 0 for all
     * inputs outside the range 0-2
     */
    double singleLRatioToScore(double ratio)
    {
        return (Math.max(0, Math.min(100 * (1 - Math.abs(1 - ratio)), 100)));
    }

    /**
     * Converts a ratio with ideal value of 2:1 to a score. The resulting
     * function is piecewise linear going from (0,0) to (2,100) to (4,0) and is
     * 0 for all inputs outside the range 0-4
     */
    double doubleLRatioToScore(double ratio)
    {
        // divides ratio of width/height which is ideally 2:1
        // by 2 to fit 1:1 ratio for the math
        ratio /= 2;
        return (Math.max(0, Math.min(100 * (1 - Math.abs(1 - ratio)), 100)));
    }

    double singleLAreaScore(ParticleReport report)
    {
        double boundingArea = (report.BoundingRectBottom - report.BoundingRectTop)
                * (report.BoundingRectRight - report.BoundingRectLeft);
        // Tape is 7" edge so 49" bounding rect. With 2" wide tape it covers 24"
        // of the rect.
        return singleLRatioToScore((49 / 24) * report.Area / boundingArea);
    }

    double doubleLAreaScore(ParticleReport report)
    {
        double boundingArea = (report.BoundingRectBottom - report.BoundingRectTop)
                * (report.BoundingRectRight - report.BoundingRectLeft);
        // total area = 119
        // reflect area = 48
        // ratio = 48/119
        // therefore, multiply by 119/48 to get area between 0 and 1
        LOGGER.debug("Report Area: " + report.Area + " Bounding Area: " + boundingArea);
        return singleLRatioToScore((119 / 71) * report.Area / boundingArea);
    }

    /**
     * Method to score if the aspect ratio of the particle appears to match the
     * retro-reflective target. Target is 7"x7" so aspect should be 1
     */
    double singleLAspectScore(ParticleReport report)
    {
        // width vs height ratio
        return singleLRatioToScore(((report.BoundingRectRight - report.BoundingRectLeft) / (report.BoundingRectBottom - report.BoundingRectTop)));
    }

    double doubleLAspectScore(ParticleReport report)
    {
        // width vs height ratio
        return doubleLRatioToScore(((report.BoundingRectRight - report.BoundingRectLeft) / (report.BoundingRectBottom - report.BoundingRectTop)));
    }

    /**
     * Computes the estimated distance to a target using the width of the
     * particle in the image. For more information and graphics showing the math
     * behind this approach see the Vision Processing section of the
     * ScreenStepsLive documentation.
     *
     * @param image
     *            The image to use for measuring the particle estimated
     *            rectangle
     * @param report
     *            The Particle Analysis Report for the particle
     * @param isLong
     *            Boolean indicating if the target is believed to be the long
     *            side of a tote
     * @return The estimated distance to the target in feet.
     */
    double computeDistance(Image image, ParticleReport report)
    {
        double normalizedWidth, targetWidth;
        NIVision.GetImageSizeResult size;

        size = NIVision.imaqGetImageSize(image);
        normalizedWidth = 2 * (report.BoundingRectRight - report.BoundingRectLeft) / size.width;
        targetWidth = 7;

        return targetWidth / (normalizedWidth * 12 * Math.tan(VIEW_ANGLE * Math.PI / (180 * 2)));

    }

    public static Autonomous getInstance()
    {
        if (autonomous == null)
            autonomous = new Autonomous();
        return autonomous;
    }

    /**
     * Private constructor to setup the Autonomous
     */

    private Autonomous()
    {
        drive = Drive.getInstance();
    }

    /**
     * Sets what autonomous routine to run. Please do not call this after
     * updateAutonomousPeriodic() has been run.
     * 
     * @param autoType
     */
    public void setAutoType(AutoType autoType)
    {
        this.autonomousType = autoType;
    }

    /**
     * Updates the Autonomous routine.
     */
    public void updateAutonomousPeriodic()
    {

        //
        // Possible Auto Types:
        //
        // Drive to AUTO zone,
        // Grab box and drive to AUTO zone
        // Grab container and drive to AUTO zone
        // Grab box and grab container and drive to AUTO zone
        switch (this.autonomousType)
        {
            case DRIVE_ONLY:

                // Drive to auto zone. Starts on the very edge and
                // just creeps into the zone
                break;
            case GRAB_ITEM:
                boolean isTote = cameraPeriodic();

                if (isTote)
                {
                    // Start behind an item (container or tote), and pick it up
                    // and carry it to the auto zone
                }

                break;
            case PUSH_TOTE:
                // Pushes tote into the AUTO zone as we drive ourselves
                // into the auto zone
                break;
            case GRAB_CONTAINER_PUSH_TOTE:
                // Pick up container, strafe over to tote,
                // plow tote into AUTO zone while holding container
                break;
            case GRAB_BOTH:
                // Pick up the container and stack it on the tote.
                // Then, pick up the tote and drive the robot and stack into
                // the AUTO zone.
                break;

        }
    }
}

/**
 * Type of Autonomous drive to use.
 * 
 * @author kyle
 *
 */
enum AutoType
{
    DRIVE_ONLY, GRAB_ITEM, PUSH_TOTE, GRAB_CONTAINER_PUSH_TOTE, GRAB_BOTH;
}
