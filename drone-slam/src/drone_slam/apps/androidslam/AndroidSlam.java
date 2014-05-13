package drone_slam.apps.androidslam;

/**
 * Created by JeffreyZhang on 2014/5/13.
 */
public class AndroidSlam {

    private int port_number;
    private NavdataListener navdata_listener;

    public AndroidSlam(int port) {
        port_number = port;
    }

    private void start() {
        navdata_listener = new NavdataListener(port_number);
        navdata_listener.start();
    }

    private void stop() {
        navdata_listener.stop();
    }

    public static void main(String args[]) {
        AndroidSlam android_slam = new AndroidSlam(12345);
        android_slam.start();
    }
}
