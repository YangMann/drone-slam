package drone_slam.apps.androidslam;

/**
 * Created by JeffreyZhang on 2014/5/13.
 */
public class AndroidSlam {

    private int port_number_navdata;
    private int port_number_image_stream;
    private NavdataListener navdata_listener;
    private ImageStreamListener image_stream_listener;

    public AndroidSlam(int port_navdata, int port_image) {
        port_number_navdata = port_navdata;
        port_number_image_stream = port_image;
    }

    private void start() {
        navdata_listener = new NavdataListener(port_number_navdata);
//        image_stream_listener = new ImageStreamListener(port_number_image_stream);
        navdata_listener.start();
//        image_stream_listener.start();
    }

    private void stop() {
        navdata_listener.interrupt();
//        image_stream_listener.interrupt();
    }

    public static void main(String args[]) {
        AndroidSlam android_slam = new AndroidSlam(5554, 5555);
        android_slam.start();
    }
}
