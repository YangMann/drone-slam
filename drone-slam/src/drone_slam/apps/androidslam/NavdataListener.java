package drone_slam.apps.androidslam;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by JeffreyZhang on 2014/5/13.
 */
public class NavdataListener {

    private int port_number;
    private byte[] received_data = new byte[1024];

    public static boolean is_running;

    public NavdataListener(int port) {
        port_number = port;
    }

    public void start() {
        try {
            DatagramSocket udp_socket = new DatagramSocket(port_number);
            is_running = true;
            while (is_running) {
                DatagramPacket received_packet = new DatagramPacket(received_data, received_data.length);
                udp_socket.receive(received_packet);
                String data_sentence = new String(received_packet.getData());
                System.out.println(data_sentence);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        is_running = false;
    }
}
