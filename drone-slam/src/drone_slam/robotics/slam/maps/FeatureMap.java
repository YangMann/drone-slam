package drone_slam.robotics.slam.maps;

import drone_slam.robotics.slam.Global;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.TimeUnit;

/**
 * Created by JeffreyZhang on 2014/5/16.
 */
public class FeatureMap {

    public Mat descriptors;
    public Mat descriptors_grid;         // grid with index to descriptors matrix row (index u16, response float 32, time int 32) = 80
    public Mat keypoints_world_coords;   // local world coordinates
    public Mat keypoints_t;              // time
    public short descriptors_count;

    private double[] cell_value;   // Vec3i
    private short g_idx;
    private float g_response;
    long g_time, time, time_threshold;

    public FeatureMap() {
        descriptors = new Mat(1000, 64, CvType.CV_32F);
        keypoints_world_coords = new Mat(1000, 1, CvType.CV_32FC3);
        descriptors_grid = new Mat(200, 200, CvType.CV_32SC3);
        keypoints_t = new Mat(1000, 1, CvType.CV_32S);

        descriptors_count = 0;

        cell_value = new double[3];
        g_idx = (short) cell_value[0];
        g_response = (float) cell_value[1];
        g_time = (long) cell_value[2];

    }

    public void update(MatOfKeyPoint keypoints, Mat m_descriptors, MatOfPoint3f world_coords) {
        time = System.currentTimeMillis();
        time_threshold = System.currentTimeMillis() - 5 * TimeUnit.SECONDS.toMillis(1);

        int size = keypoints.rows();
        int idx;

        // double the size of the matrix
        if (descriptors_count + size > descriptors.rows()) {
            int s = descriptors.rows() * 2;
            Imgproc.resize(descriptors, descriptors, new Size(s, 64));
            Imgproc.resize(keypoints_world_coords, keypoints_world_coords, new Size(s, 1));
            Imgproc.resize(keypoints_t, keypoints_t, new Size(s, 1));
        }

        short[] x = new short[size];
        short[] y = new short[size];

        // convert all at once
        worldpos_to_dgridpos(world_coords, x, y);

        for (int i = 0; i < m_descriptors.rows(); i++) {
            if (!cell_inside_descriptors_grid(x[i], y[i])) {
                continue;
            }
            cell_value = descriptors_grid.get(y[i], x[i]);
            g_idx = (short) cell_value[0];
            g_response = (float) cell_value[1];
            g_time = (long) cell_value[2];
            // descriptor found for cell: overwrite?
            if (g_idx > 0) {
                if (keypoints.toArray()[i].response <= g_response || g_time < time_threshold) {
                    continue;
                }
                idx = g_idx;
            } else {    // not in buffer -> add
                idx = descriptors_count++;
            }

            m_descriptors.copyTo(descriptors);

            world_coords.row(i).copyTo(keypoints_world_coords.row(idx));
            keypoints_t.put(idx, 0, time);

            g_idx = (short) idx;
            g_response = keypoints.toArray()[i].response;
            g_time = time;
            cell_value[0] = g_idx;
            cell_value[1] = g_response;
            cell_value[2] = g_time;

            descriptors_grid.put(y[i], x[i], cell_value);
        }

    }

    public void get_local_descriptors(Mat map_descriptors, Mat map_keypoints, Mat map_keypoints_t, Point3 world_coord) {
        float radius = 0.0f;
        get_local_descriptors(map_descriptors, map_keypoints, map_keypoints_t, world_coord, radius);
    }

    public void get_local_descriptors(Mat map_descriptors, Mat map_keypoints, Mat map_keypoints_t, Point3 world_coord, float radius) {
        if (radius <= 0.0f) {
            System.out.println("Get descriptors of whole map: DEPRICATED");

            // return all keypoints and descriptors
            Range rows = new Range(1, descriptors_count);
            map_descriptors = new Mat(descriptors, rows);
            map_keypoints = new Mat(keypoints_world_coords, rows);
        } else {
            int i = 0;
            short[] indices;
            short x, y;
            double[] cell_value;
            short g_idx;
            long g_time;
            long time_threshold = System.currentTimeMillis() - 5 * TimeUnit.SECONDS.toMillis(1);
            short[] t = worldpos_to_dgridpos(world_coord);
            x = t[0];
            y = t[1];
            int r = (int) Math.floor(radius * 0.01f);

            int x2, y2, w, h;
            x2 = Math.max(0, x - r);
            y2 = Math.max(0, y - r);
            w = Math.min(descriptors_grid.cols() - x2, r * 2);
            h = Math.min(descriptors_grid.rows() - y2, r * 2);
            if (!((x2 >= descriptors_grid.cols()) || (y2 >= descriptors_grid.rows())) || ((w <= 0) || (h <= 0))) {
                indices = new short[w * h];
                Mat grid = new Mat(descriptors_grid, new Rect(x2, y2, w, h));
                for (x2 = 0; x2 < grid.cols(); x2++) {
                    for (y2 = 0; y2 < grid.rows(); y2++) {
                        cell_value = grid.get(y2, x2);
                        g_idx = (short) cell_value[0];
                        g_time = (long) cell_value[2];
                        if ((g_idx > 0) && (g_time <= time_threshold)) {
                            indices[i++] = g_idx;
                        }
                    }
                }
                map_descriptors = new Mat(i, descriptors.cols(), descriptors.type());
                map_keypoints = new Mat(i, keypoints_world_coords.cols(), keypoints_world_coords.type());
                map_keypoints_t = new Mat(i, keypoints_t.cols(), keypoints_t.type());

                int descriptors_rowsize = Global.SLAM_DESCRIPTOR_SIZE;
                int keypoint_rowsize = 3 * Float.SIZE / 8;
                int keypoint_t_rowsize = Integer.SIZE / 8;

                // TODO: unfinished

            }
        }

    }

    public boolean inside(Mat m, Rect r) {
        Size size = m.size();
        return !((r.x + r.width >= size.width) || (r.y + r.height >= size.height) || (r.x - r.width < 0) || (r.y - r.height < 0));
    }

    public boolean cell_inside_descriptors_grid(short x, short y) {
        return ((x >= 0) && (y >= 0) && (x < 200) && (y < 200));
    }

    private void worldpos_to_dgridpos(MatOfPoint3f src, short[] x, short[] y) {
        for (int i = 0; i < src.rows(); i++) {
            x[i] = (short) (Math.floor(src.toArray()[i].x * 0.01f + 0.5f) + 100);
            y[i] = (short) (Math.floor(src.toArray()[i].y * 0.01f + 0.5f) + 100);
        }

    }

    private short[] worldpos_to_dgridpos(Point3 src) {
        short[] t = new short[2];
        t[0] = (short) (Math.floor(src.x * 0.01f + 0.5f) + 100);
        t[1] = (short) (Math.floor(src.y * 0.01f + 0.5f) + 100);
        return t;
    }
}
