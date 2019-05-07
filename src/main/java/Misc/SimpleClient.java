package Misc;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SimpleClient {

    private static DatagramSocket socket = null;

    public static void main(String[] args){
        try {
            // just try pi for now
            InetAddress address = InetAddress.getByName("129.3.20.26");
            byte [] garbage = new byte[512];

            socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(garbage, garbage.length, address, Constants.UNICAST_PORT);
            for (int i = 0; i < 1000000; i++) {
                System.out.println("Sending packet #: " + i);
                socket.send(packet);
            }

        } catch (IOException e){
            e.printStackTrace();
        }

        socket.close();
    }
}
