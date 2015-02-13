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
		
	}

	
	
	
	
	
	public void disabledPeriodic() {
	}
		

	/**
	 * This function is run when autonomous control mode is first enabled
	 */
	public void autonomousInit() {
		int ticks = 0;
		autonomousPeriodic(ticks);
		
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
	public void autonomousPeriodic(int ticks) {
		LOGGER.debug("Autonomous");
		ticks = ticks + 1;
		
		
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
