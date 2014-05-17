package drone_slam.robotics.slam;

/**
 * Created by JeffreyZhang on 2014/5/17.
 */
public class Global {
    /* SLAM */
    public static final boolean SLAM_USE_QUEUE = true; // use a queue to store the controldata and sensor data
    public static final float SLAM_SURF_HESSIANTHRESHOLD = 70.0f; // 70.0f
    public static final float SLAM_SURF_MIN_RESPONSE = 100.0f; // 1000.0f
    public static final int SLAM_ELEVATION_MAP_DEFAULT_SIZE = 200; // 10m * 10m in each direction + 1 for center
    public static final int SLAM_DESCRIPTOR_SIZE = 64 * Float.SIZE / 8;
    // #define SLAM_WC_SIZE 3 * sizeof(float)
    //#define SLAM_LOC_WRITE_STATE_DIRECTLY true // remove line if false
    //#define SLAM_VISUALMOTION_WRITE_STATE_DIRECTLY true // remove line if false
    public static final boolean SLAM_LOC_UPDATE_YAW = false;

    public static final int SLAM_MODE_VISUALMOTION = 0x01;
    public static final int SLAM_MODE_ACCEL = 0x02;
    public static final int SLAM_MODE_VEL = 0x04;
    public static final int SLAM_MODE_VISUALLOC = 0x08;
    public static final int SLAM_MODE_MAP = 0x10;
    public static final int SLAM_MODE_ELEVMAP = 0x20;
    public static final int SLAM_MODE_VEL_OR = 0x40;
}
