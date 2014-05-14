package drone_slam.apps.androidslam;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by JeffreyZhang on 2014/5/14.
 */
public class ImageStreamListener extends Thread {

    private int port_number;
    private Socket connection_socket;
    private byte[] data_buffer;
    private int data_length;
    private JFrame frame;

    public static boolean is_running;

    public ImageStreamListener(int port) {
        port_number = port;
        frame = new JFrame("Received image");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(480, 320);
        frame.setVisible(true);
    }

    public void run() {
        try {
            ServerSocket tcp_socket = new ServerSocket(port_number);
            is_running = true;
            System.out.println("#### TCP RUNNING ####");
            while (is_running) {
                connection_socket = tcp_socket.accept();
                InputStream received_stream = connection_socket.getInputStream();

                data_buffer = new byte[1024];
                data_length = received_stream.read(data_buffer);
                byte[] timestamp_buffer = new byte[64];
                byte[] image_buffer = new byte[data_length - 64];
                System.arraycopy(data_buffer, 0, timestamp_buffer, 0, 64);
                System.arraycopy(data_buffer, 64, image_buffer, 0, data_length - 64);

                /*
                 *  测试byte[] 转 long
                 */
                ByteBuffer timestamp_bb = ByteBuffer.wrap(timestamp_buffer);
                long image_timestamp = timestamp_bb.getLong();

                System.out.println("#### IMAGE_TIMESTAMP\t" + image_timestamp);
                System.out.println(System.currentTimeMillis());

                ByteArrayOutputStream s = new ByteArrayOutputStream();
                long temp = System.currentTimeMillis();
                s.write((int) temp);
                s.write((int) (temp << 32));
                ByteArrayInputStream i = new ByteArrayInputStream(s.toByteArray());
                byte[] t = new byte[64];
                i.read(t);
                timestamp_bb = ByteBuffer.wrap(t);
                System.out.println(timestamp_bb.getLong());

                /*
                 *  测试结束
                 */

                ByteArrayInputStream received_image_input_stream = new ByteArrayInputStream(image_buffer);
                BufferedImage received_image = ImageIO.read(received_image_input_stream);
                if (received_image == null) {
                    System.out.println("received_image == null");
                }
                ImageIcon received_image_icon = new ImageIcon(received_image);
                setBackground(frame, received_image_icon, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setBackground(JFrame frame, ImageIcon icon, boolean is_auto_size) {
        if (frame == null || icon == null) {
            System.out.println("frame == null || icon == null");
        } else {
            Container pane = frame.getContentPane();
            ((JPanel) pane).setOpaque(false);
            JLayeredPane layered_pane = frame.getLayeredPane();
            Component[] components = layered_pane.getComponentsInLayer(Integer.MIN_VALUE);
            if (components.length > 0) {
                for (Component component : components) {
                    layered_pane.remove(component);
                }
            }
            JLabel label = new JLabel(icon);
            if (is_auto_size) {
                icon.setImage(icon.getImage().getScaledInstance(frame.getSize().width, frame.getSize().height, Image.SCALE_SMOOTH));
            }
            label.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
            layered_pane.add(label, Integer.MIN_VALUE);
        }
    }

}
