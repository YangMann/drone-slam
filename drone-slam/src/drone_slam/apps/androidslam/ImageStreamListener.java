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
                String received_stream_size = readResponse(received_stream);
                int expected_byte_number = Integer.parseInt(received_stream_size);
                System.out.println("Expecting\t" + expected_byte_number);

                data_buffer = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream(expected_byte_number);
                int bytes_read = 0;
                int bytes_in;
                while (bytes_read < expected_byte_number) {
                    bytes_in = received_stream.read(data_buffer);
                    bytes_read += bytes_in;
                    baos.write(data_buffer, 0, bytes_in);
                }
                System.out.println("Read\t\t" + bytes_read);
                baos.close();
                byte[] whole_data_buffer = baos.toByteArray();
                byte[] timestamp_buffer = new byte[8];
                byte[] image_buffer = new byte[expected_byte_number - 8];
                System.arraycopy(whole_data_buffer, 0, timestamp_buffer, 0, 8);
                System.arraycopy(whole_data_buffer, 8, image_buffer, 0, expected_byte_number - 8);

                /*
                 *  byte[] 转 long
                 */
                ByteBuffer timestamp_bb = ByteBuffer.wrap(timestamp_buffer);
                long image_timestamp = timestamp_bb.getLong();

                System.out.println("#### IMAGE_TIMESTAMP\t" + image_timestamp);

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

    protected String readResponse(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder(128);
        int in;
        while ((in = inputStream.read()) != '\n') {
            sb.append((char) in);
        }
        System.out.println(sb.toString());
        return sb.toString();
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
