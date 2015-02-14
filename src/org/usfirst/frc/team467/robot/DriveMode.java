package org.usfirst.frc.team467.robot;

public enum DriveMode
{
    CRAB_FA,    // Crab Drive - Field Aligned
    CRAB_NO_FA, // Crab Drive - No Field Alignment
    TURN, 		// Turn in Place
    STRAFE, 	// Strafe Drive
    REVOLVE,	// Revolve Drive (rotate around a center outside robot)
    UNWIND,		// Unwind the wheel pods
}
