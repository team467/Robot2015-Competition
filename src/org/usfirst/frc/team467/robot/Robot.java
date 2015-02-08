/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */

/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.usfirst.frc.team467.robot;

import java.util.Comparator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.DrawMode;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ImageType;
import com.ni.vision.NIVision.ParticleReport;
import com.ni.vision.NIVision.ShapeMode;

import edu.wpi.first.wpilibj.CameraServer;
// import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	private static final Logger LOGGER = Logger.getLogger(Robot.class);

	public static final boolean SINGLE_STICK_DRIVE = false;

	// Robot objects
	private DriverStation467 driverstation;

	private Drive drive;

	int session;
	Image frame;

	CameraServer cameraServer = null;

	boolean useCamera = true;

	/**
	 * Time in milliseconds
	 */
	double time;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */

	public class ParticleReport implements Comparator<ParticleReport>,
			Comparable<ParticleReport> {
		double PercentAreaToImageArea;
		double Area;
		double BoundingRectLeft;
		double BoundingRectTop;
		double BoundingRectRight;
		double BoundingRectBottom;

		public int compareTo(ParticleReport r) {
			return (int) (r.Area - this.Area);
		}

		public int compare(ParticleReport r1, ParticleReport r2) {
			return (int) (r1.Area - r2.Area);
		}
	};

	// Structure to represent the scores for the various tests used for target
	// identification
	public class Scores {
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
	NIVision.ParticleFilterOptions2 filterOptions = new NIVision.ParticleFilterOptions2(
			0, 0, 1, 1);
	Scores scores = new Scores();

	public void robotInit() {
		// Initialize logging framework.
		Logging.init();

		// Make robot objects
		driverstation = DriverStation467.getInstance();

		drive = Drive.getInstance();
		try {
			cameraServer = CameraServer.getInstance();
		} catch (Exception ex) {
			useCamera = false;
		}
		// cameraServer.setQuality(50);
		// cameraServer.startAutomaticCapture("cam0");
		//
		// frame = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);
		//
		// // the camera name (ex "cam0") can be found through the roborio web
		// interface
		// session = NIVision.IMAQdxOpenCamera("cam0",
		// NIVision.IMAQdxCameraControlMode.CameraControlModeController);
		// NIVision.IMAQdxConfigureGrab(session);

		time = System.currentTimeMillis();

		Calibration.init();
	}

	public void disabledInit() {
		LOGGER.info("Robot disabled");
		LOGGER.debug("Robot disabled");

		frame = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);

		// the camera name (ex "cam0") can be found through the roborio web
		// interface
		session = NIVision.IMAQdxOpenCamera("cam0",
				NIVision.IMAQdxCameraControlMode.CameraControlModeController);
		NIVision.IMAQdxConfigureGrab(session);

		// create images
		frame = NIVision.imaqCreateImage(ImageType.IMAGE_RGB, 0);
		binaryFrame = NIVision.imaqCreateImage(ImageType.IMAGE_U8, 0);
		criteria[0] = new NIVision.ParticleFilterCriteria2(
				NIVision.MeasurementType.MT_AREA_BY_IMAGE_AREA, AREA_MINIMUM,
				100.0, 0, 0);

		// Put default values to SmartDashboard so fields will appear
		SmartDashboard.putNumber("Tote hue min", TOTE_HUE_RANGE.minValue);
		SmartDashboard.putNumber("Tote hue max", TOTE_HUE_RANGE.maxValue);
		SmartDashboard.putNumber("Tote sat min", TOTE_SAT_RANGE.minValue);
		SmartDashboard.putNumber("Tote sat max", TOTE_SAT_RANGE.maxValue);
		SmartDashboard.putNumber("Tote val min", TOTE_VAL_RANGE.minValue);
		SmartDashboard.putNumber("Tote val max", TOTE_VAL_RANGE.maxValue);
		SmartDashboard.putNumber("Area min %", AREA_MINIMUM);
	}

	public void disabledPeriodic() {

		if (useCamera) {
			NIVision.IMAQdxGrab(session, frame, 1);

			// Update threshold values from SmartDashboard. For performance
			// reasons it is recommended to remove this after calibration is
			// finished.
			TOTE_HUE_RANGE.minValue = (int) SmartDashboard.getNumber(
					"Tote hue min", TOTE_HUE_RANGE.minValue);
			TOTE_HUE_RANGE.maxValue = (int) SmartDashboard.getNumber(
					"Tote hue max", TOTE_HUE_RANGE.maxValue);
			TOTE_SAT_RANGE.minValue = (int) SmartDashboard.getNumber(
					"Tote sat min", TOTE_SAT_RANGE.minValue);
			TOTE_SAT_RANGE.maxValue = (int) SmartDashboard.getNumber(
					"Tote sat max", TOTE_SAT_RANGE.maxValue);
			TOTE_VAL_RANGE.minValue = (int) SmartDashboard.getNumber(
					"Tote val min", TOTE_VAL_RANGE.minValue);
			TOTE_VAL_RANGE.maxValue = (int) SmartDashboard.getNumber(
					"Tote val max", TOTE_VAL_RANGE.maxValue);

			// Threshold the image looking for yellow (tote color)
			NIVision.imaqColorThreshold(binaryFrame, frame, 255,
					NIVision.ColorMode.HSV, TOTE_HUE_RANGE, TOTE_SAT_RANGE,
					TOTE_VAL_RANGE);

			// Send particle count to dashboard
			int numParticles = NIVision.imaqCountParticles(binaryFrame, 1);
			SmartDashboard.putNumber("Masked particles", numParticles);

			// Send masked image to dashboard to assist in tweaking mask.
			cameraServer.setImage(binaryFrame);

			// filter out small particles
			float areaMin = (float) SmartDashboard.getNumber("Area min %",
					AREA_MINIMUM);
			criteria[0].lower = areaMin;
			imaqError = NIVision.imaqParticleFilter4(binaryFrame, binaryFrame,
					criteria, filterOptions, null);

			// Send particle count after filtering to dashboard
			numParticles = NIVision.imaqCountParticles(binaryFrame, 1);
			SmartDashboard.putNumber("Filtered particles", numParticles);

			if (numParticles > 0) {
				// Measure particles and sort by particle size
				Vector<ParticleReport> particles = new Vector<ParticleReport>();
				for (int particleIndex = 0; particleIndex < numParticles; particleIndex++) {
					ParticleReport par = new ParticleReport();
					par.PercentAreaToImageArea = NIVision.imaqMeasureParticle(
							binaryFrame, particleIndex, 0,
							NIVision.MeasurementType.MT_AREA_BY_IMAGE_AREA);
					par.Area = NIVision.imaqMeasureParticle(binaryFrame,
							particleIndex, 0, NIVision.MeasurementType.MT_AREA);
					par.BoundingRectTop = NIVision.imaqMeasureParticle(
							binaryFrame, particleIndex, 0,
							NIVision.MeasurementType.MT_BOUNDING_RECT_TOP);
					par.BoundingRectLeft = NIVision.imaqMeasureParticle(
							binaryFrame, particleIndex, 0,
							NIVision.MeasurementType.MT_BOUNDING_RECT_LEFT);
					par.BoundingRectBottom = NIVision.imaqMeasureParticle(
							binaryFrame, particleIndex, 0,
							NIVision.MeasurementType.MT_BOUNDING_RECT_BOTTOM);
					par.BoundingRectRight = NIVision.imaqMeasureParticle(
							binaryFrame, particleIndex, 0,
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

				boolean isTote = false;
				particleLoop: for (ParticleReport pr : particles) {
					scores = new Scores();
					scores.Aspect = singleLAspectScore(pr);
					SmartDashboard.putNumber("Aspect", scores.Aspect);
					scores.Area = singleLAreaScore(pr);
					SmartDashboard.putNumber("Area", scores.Area);
					LOGGER.debug("Single L Aspect: " + scores.Aspect
							+ " Area: " + scores.Area);
					if (scores.Aspect > SCORE_MIN && scores.Area > SCORE_MIN) {
						isTote = true;
						break particleLoop;
					}

					scores = new Scores();
					scores.Aspect = doubleLAspectScore(pr);
					SmartDashboard.putNumber("Aspect", scores.Aspect);
					scores.Area = doubleLAreaScore(pr);
					SmartDashboard.putNumber("Area", scores.Area);
					LOGGER.debug("Double L Aspect: " + scores.Aspect
							+ " Area: " + scores.Area);
					if (scores.Aspect > SCORE_MIN && scores.Area > SCORE_MIN) {
						isTote = true;
						break particleLoop;
					}
				}

				// Send distance and tote status to dashboard. The bounding
				// rect, particularly the horizontal center (left - right) may
				// be useful for rotating/driving towards a tote
				SmartDashboard.putBoolean("IsTote", isTote);
				SmartDashboard.putNumber("Distance",
						computeDistance(binaryFrame, particles.elementAt(0)));

			} else {
				SmartDashboard.putBoolean("IsTote", false);
			}
		}

	}

	// Comparator function for sorting particles. Returns true if particle 1 is
	// larger
	static boolean CompareParticleSizes(ParticleReport particle1,
			ParticleReport particle2) {
		// we want descending sort order
		return particle1.PercentAreaToImageArea > particle2.PercentAreaToImageArea;
	}

	/**
	 * Converts a ratio with ideal value of 1 to a score. The resulting function
	 * is piecewise linear going from (0,0) to (1,100) to (2,0) and is 0 for all
	 * inputs outside the range 0-2
	 */
	double singleLRatioToScore(double ratio) {
		return (Math.max(0, Math.min(100 * (1 - Math.abs(1 - ratio)), 100)));
	}

	/**
	 * Converts a ratio with ideal value of 2:1 to a score. The resulting
	 * function is piecewise linear going from (0,0) to (2,100) to (4,0) and is
	 * 0 for all inputs outside the range 0-4
	 */
	double doubleLRatioToScore(double ratio) {
		// divides ratio of width/height which is ideally 2:1
		// by 2 to fit 1:1 ratio for the math
		ratio /= 2;
		return (Math.max(0, Math.min(100 * (1 - Math.abs(1 - ratio)), 100)));
	}

	double singleLAreaScore(ParticleReport report) {
		double boundingArea = (report.BoundingRectBottom - report.BoundingRectTop)
				* (report.BoundingRectRight - report.BoundingRectLeft);
		// Tape is 7" edge so 49" bounding rect. With 2" wide tape it covers 24"
		// of the rect.
		return singleLRatioToScore((49 / 24) * report.Area / boundingArea);
	}

	double doubleLAreaScore(ParticleReport report) {
		double boundingArea = (report.BoundingRectBottom - report.BoundingRectTop)
				* (report.BoundingRectRight - report.BoundingRectLeft);
		// total area = 119
		// reflect area = 48
		// ratio = 48/119
		// therefore, multiply by 119/48 to get area between 0 and 1
		LOGGER.debug("Report Area: " + report.Area + " Bounding Area: "
				+ boundingArea);
		return singleLRatioToScore((119 / 71) * report.Area / boundingArea);
	}

	/**
	 * Method to score if the aspect ratio of the particle appears to match the
	 * retro-reflective target. Target is 7"x7" so aspect should be 1
	 */
	double singleLAspectScore(ParticleReport report) {
		// width vs height ratio
		return singleLRatioToScore(((report.BoundingRectRight - report.BoundingRectLeft) / (report.BoundingRectBottom - report.BoundingRectTop)));
	}

	double doubleLAspectScore(ParticleReport report) {
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
	double computeDistance(Image image, ParticleReport report) {
		double normalizedWidth, targetWidth;
		NIVision.GetImageSizeResult size;

		size = NIVision.imaqGetImageSize(image);
		normalizedWidth = 2
				* (report.BoundingRectRight - report.BoundingRectLeft)
				/ size.width;
		targetWidth = 7;

		return targetWidth
				/ (normalizedWidth * 12 * Math.tan(VIEW_ANGLE * Math.PI
						/ (180 * 2)));

	}

	/**
	 * This function is run when autonomous control mode is first enabled
	 */
	public void autonomousInit() {

	}

	/**
	 * This function is run when operator control mode is first enabled
	 */
	public void teleopInit() {
		// NIVision.IMAQdxStartAcquisition(session);
	}

	/**
	 * This function is run when test mode is first enabled
	 */
	public void testInit() {
	}

	/**
	 * This function is called periodically test mode
	 */
	public void testPeriodic() {
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic() {
		LOGGER.debug("Autonomous");
	}

	// read file in from disk. For this example to run you need to copy
	// image.jpg from the SampleImages folder to the
	// directory shown below using FTP or SFTP:
	// http://wpilib.screenstepslive.com/s/4485/m/24166/l/282299-roborio-ftp

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic() {
		// Read driverstation inputs
		driverstation.readInputs();

		if (driverstation.getCalibrate()) {
			// Calibrate Mode
			Calibration.updateCalibrate();
		} else {
			// Drive Mode
			updateDrive();
		}

		// renderImage();

	}

	private void renderImage() {
		/**
		 * Rectangle to be rendered in
		 */
		NIVision.Rect rect = new NIVision.Rect(100, 100, 500, 500);
		ShapeMode shape = ShapeMode.SHAPE_OVAL;
		NIVision.IMAQdxGrab(session, frame, 1);
		NIVision.imaqDrawShapeOnImage(frame, frame, rect, DrawMode.DRAW_VALUE,
				shape, 0.0f);

		cameraServer.setImage(frame);

	}

	/**
	 * called once per iteration to perform any necessary updates to the drive
	 * system.
	 */
	private void updateDrive() {
		switch (driverstation.getDriveMode()) {
		case REWIND:
			drive.rewindDrive();
			break;

		case REVOLVE: {
			Direction direction = Direction.LEFT;
			if (driverstation.getDriveJoystick().buttonDown(6)) {
				direction = Direction.RIGHT;
			}
			drive.revolveDrive(direction);
		}
			break;

		case STRAFE: {
			Direction direction = Direction.LEFT;
			if (driverstation.getDriveJoystick().getPOV() < 180) {
				direction = Direction.RIGHT;
			}
			drive.strafeDrive(direction);
		}
			break;

		case TURN:
			drive.turnDrive(-driverstation.getDriveJoystick().getTwist());
			break;

		case CRAB_FA:
			drive.crabDrive(driverstation.getDriveJoystick().getStickAngle(),
					driverstation.getDriveJoystick().getStickDistance(), true /*
																			 * field
																			 * aligned
																			 */);
			break;

		case CRAB_NO_FA:
			drive.crabDrive(driverstation.getDriveJoystick().getStickAngle(),
					driverstation.getDriveJoystick().getStickDistance(), false /*
																				 * not
																				 * field
																				 * aligned
																				 */);
			break;

		default: // should never enter here
			System.err.println("Button State not calculated correctly");
			drive.crabDrive(driverstation.getDriveJoystick().getStickAngle(),
					driverstation.getDriveJoystick().getStickDistance(), false /*
																				 * not
																				 * field
																				 * aligned
																				 */);
			break;
		}
	}
}
