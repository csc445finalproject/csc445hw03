package Misc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastPublisher {
    private static DatagramSocket mcSocket, inputSocket = null;
    private static final byte [] buf = new byte[Constants.BUFFER_SIZE];

    // make changes so that it receives an incoming packet from another host fist
    // might have to build a cache on here from receiving packets from other host if the jumbo packets don't work.
    public static void main(String [] args) throws IOException{
        mcSocket = new DatagramSocket();
        inputSocket = new DatagramSocket(Constants.PORT);
        InetAddress group = InetAddress.getByName(Constants.IP_MULTICAST);

        DatagramPacket incPacket = new DatagramPacket(buf, buf.length);
        byte [] messageBytes;
        for (;;) {
            inputSocket.receive(incPacket);
            System.out.println("Packet Received, sending now");
            // For now the publisher only sends one message at a time, client listens as long as he wants
            // probably want gui to handle things like leaving the group and invoking a method for this
            messageBytes = incPacket.getData();
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, group, Constants.PORT);
            mcSocket.send(packet);
        }
    }

    public static void multiCast(String message){
        // should handle what main does right now
    }
}