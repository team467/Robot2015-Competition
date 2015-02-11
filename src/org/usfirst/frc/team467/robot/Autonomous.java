package org.usfirst.frc.team467.robot;

public class Autonomous
{

    private static Autonomous autonomous = null;

    private Drive drive = null;

    private AutoType autonomousType = AutoType.DRIVE_ONLY;

    /**
     * Gets a singleton instance of the Autonomous
     * 
     * @return Autonomous object
     */
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
                // Drive to auto zone. Starts on the very edge and just creeps into the zone
                break;
            case GRAB_ITEM:
                // Start behind an item (container or tote), and pick it up and carry it to the auto zone
                break;
            case PUSH_TOTE:
                // Pushes tote into the AUTO zone as we drive ourselves into the auto zone
                break;
            case GRAB_CONTAINER_PUSH_TOTE:
                // Pick up container, strafe over to tote, plow tote into AUTO zone while holding container
                break;
            case GRAB_BOTH:
                // Pick up the container and stack it on the tote.
                // Then, pick up the tote and drive the robot and stack into the AUTO zone.
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
    DRIVE_ONLY, GRAB_ITEM, PUSH_TOTE, GRAB_CONTAINER_PUSH_TOTE, GRAB_BOTH
}
