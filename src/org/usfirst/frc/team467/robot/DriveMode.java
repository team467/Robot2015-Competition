package org.usfirst.frc.team467.robot;

public enum DriveMode
{
    ARCADE_FA,                    // Crab Drive - Field Aligned
    ARCADE_NO_FA,                 // Crab Drive - No Field Alignment
    TURN, 		                // Turn in Place
    STRAFE_FRONT,                  // Drive Forwards Slowly
    STRAFE_LEFT, 	            // Drive left Slowly
    STRAFE_RIGHT,               // Drive Right Slowly
    STRAFE_BACK,                // Drive Backwards Slowly
    ALIGN,
    REVOLVE_LARGE_LEFT,	        // Revolve Drive (rotate left around a center outside robot)
    REVOLVE_LARGE_RIGHT,        // Revolve Drive (rotate right around a center outside robot)
    REVOLVE_SMALL_LEFT,         // Revolve Drive (rotate left around a center near robot)
    REVOLVE_SMALL_RIGHT,        // Revolve Drive (rotate right around a center near robot)
    UNWIND,	                    // Unwind the wheel pods
}
