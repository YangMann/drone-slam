package drone_slam.base.command;

public class LandCommand extends RefCommand {
    public LandCommand() {
        super(false, false);
        // 9th bit set to 0
    }

}
