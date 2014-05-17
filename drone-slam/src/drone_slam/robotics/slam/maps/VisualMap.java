package drone_slam.robotics.slam.maps;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.Map;

/**
 * Created by JeffreyZhang on 2014/5/16.
 */
public class VisualMap {

    public Mat canvas;
    public FeatureMap feature_map;

    public float resolution;
    public float resolution_inv;

    public int w, h, origin_x, origin_y;
    public boolean map_updated;
    public int[] frame_roi = new int[4];
    public int[] sync_roi = {-1, -1, -1, -1};

    private Size canvas_size;
    private Mat undo_translate;
    private TermCriteria term_criteria;

    private Map<Integer, Short> cell_best_keypoints;

    public VisualMap() {
        canvas = new Mat(4096, 4096, CvType.CV_8UC4);
        undo_translate = new Mat(3, 3, CvType.CV_64F);
        term_criteria = new TermCriteria(1 | 2, 20, 0.03);
        map_updated = false;
        Core.setIdentity(undo_translate);
        resolution = 0.2048f;
        resolution_inv = 4.8828125f;

        w = 2 * 2048;
        h = 2 * 2048;
        canvas_size.width = w;
        canvas_size.height = h;

        origin_x = 2048;
        origin_y = 2048;
    }

    public void update(Mat frame, MatOfPoint2f lc, MatOfPoint3f wc) {
        Point[] src = new Point[4];
        Point[] dst = new Point[4];
        Point[] lc_a = lc.toArray();
        Point3[] wc_a = wc.toArray();

        for (int i = 0; i < 4; i++) {
            frame_roi[i] = -1;
        }
        for (int i = 0; i < 4; i++) {
            src[i] = new Point(lc_a[i].x, lc_a[i].y);
            worldpos_to_canvaspos(wc_a[i], dst[i]);
            update_roi(dst[i], frame_roi);
        }
        MatOfPoint src_m = new MatOfPoint();
        MatOfPoint dst_m = new MatOfPoint();
        src_m.fromArray(src);
        dst_m.fromArray(dst);
        Mat T = Imgproc.getPerspectiveTransform(src_m, dst_m);

        int w = frame_roi[1] - frame_roi[0];
        int h = frame_roi[3] - frame_roi[2];

        // TODO: Dirty....
        if (!((frame_roi[0] < 0) || (frame_roi[2] < 0) || (frame_roi[0] + w >= 4096) || (frame_roi[2] + h >= 4096))) {
            Mat sub_canvas = new Mat(canvas, new Rect(frame_roi[0], frame_roi[2], w, h));
            undo_translate.put(0, 2, -frame_roi[0]);
            undo_translate.put(1, 2, -frame_roi[2]);
            Core.gemm(undo_translate, T, 1, new Mat(), 0, T, 0);

            canvas_size.width = w;
            canvas_size.height = h;
            Imgproc.warpPerspective(frame, sub_canvas, T, canvas_size, Imgproc.INTER_LINEAR, Imgproc.BORDER_TRANSPARENT, new Scalar(0));

            update_roi(frame_roi, sync_roi);
            map_updated = true;
        }
    }

    public void frame_to_canvas(Mat frame, Mat frameT, MatOfPoint2f lc, MatOfPoint3f wc) {
        Point[] src = new Point[4];
        Point[] dst = new Point[4];
        Point[] lc_a = lc.toArray();
        Point3[] wc_a = wc.toArray();

        for (int i = 0; i < 4; i++) {
            frame_roi[i] = -1;
        }
        for (int i = 0; i < 4; i++) {
            src[i] = new Point(lc_a[i].x, lc_a[i].y);
            worldpos_to_canvaspos(wc_a[i], dst[i]);
            update_roi(dst[i], frame_roi);
        }
        MatOfPoint src_m = new MatOfPoint();
        MatOfPoint dst_m = new MatOfPoint();
        src_m.fromArray(src);
        dst_m.fromArray(dst);
        Mat T = Imgproc.getPerspectiveTransform(src_m, dst_m);

        int w = frame_roi[1] - frame_roi[0];
        int h = frame_roi[3] - frame_roi[2];

        undo_translate.put(0, 2, -frame_roi[0]);
        undo_translate.put(1, 2, -frame_roi[2]);
        Core.gemm(undo_translate, T, 1, new Mat(), 0, T, 0);

        canvas_size.width = w;
        canvas_size.height = h;
        frameT.create(w, h, frame.type());
        Imgproc.warpPerspective(frame, frameT, T, canvas_size, Imgproc.INTER_LINEAR, Imgproc.BORDER_TRANSPARENT, new Scalar(0));
    }

    public boolean is_updated(int[] roi, boolean reset_roi) {
        if (!map_updated) {
            return false;
        }
        System.arraycopy(sync_roi, 0, roi, 0, 4);
        if (reset_roi) {
            for (int i = 0; i < 4; i++) {
                sync_roi[i] = -1;
            }
            map_updated = false;
        }

        return true;
    }

    public byte[] get_array() {
        byte[] buff = new byte[(int) (canvas.total() * canvas.channels())];
        canvas.get(0, 0, buff);
        return buff;
    }

    public void save_canvas() {
        Highgui.imwrite("data/visual_map_canvas.png", canvas);
        System.out.println("Saved visual map canvas");
    }

    private void worldpos_to_canvaspos(Point3 src, Point dst) {
        dst.x = Math.floor(src.x * resolution + 0.5f) + origin_x;
        dst.y = Math.floor(src.y * resolution + 0.5f) + origin_y;
    }

    private void canvaspos_to_worldpos(Point src, Point dst) {
        dst.x = (src.x + frame_roi[0] - origin_x) * resolution_inv;
        dst.y = (src.y + frame_roi[1] - origin_y) * resolution_inv;
    }

    private void update_roi(Point p, int[] roi) {
        if (roi[0] == -1 || p.x < roi[0])
            roi[0] = (int) p.x;

        if (roi[1] == -1 || p.x > roi[1])
            roi[1] = (int) p.x;

        if (roi[2] == -1 || p.y < roi[2])
            roi[2] = (int) p.y;

        if (roi[3] == -1 || p.y > roi[3])
            roi[3] = (int) p.y;
    }

    private void update_roi(int[] src, int[] dst) {
        if (dst[0] == -1 || src[0] < dst[0])
            dst[0] = src[0];

        if (dst[1] == -1 || src[1] > dst[1])
            dst[1] = src[1];

        if (dst[2] == -1 || src[2] < dst[2])
            dst[2] = src[2];

        if (dst[3] == -1 || src[3] > dst[3])
            dst[3] = src[3];
    }

}
