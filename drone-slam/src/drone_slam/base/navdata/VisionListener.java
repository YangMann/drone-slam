package drone_slam.base.navdata;

import java.util.EventListener;


public interface VisionListener extends EventListener {
    public void tagsDetected(VisionTag[] tags);

    public void trackersSend(TrackerData trackersData);

    public void receivedPerformanceData(VisionPerformance d);

    public void receivedRawData(float[] vision_raw);

    public void receivedData(VisionData d);

    public void receivedVisionOf(float[] of_dx, float[] of_dy);

    public void typeDetected(int detection_camera_type);

}
