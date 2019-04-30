import java.io.IOException;
import java.net.*;

public class Client {



    private MulticastSocket socket;


    public Client() throws IOException {
        connectToHost();
        receiveVideoFeed();
    }

    private void receiveVideoFeed() throws IOException {

        while (true) {
            displayVideo(getVideoBytes());
        }
    }


    void connectToHost() throws IOException {
        //connect to connectionIP
        socket = new MulticastSocket(Constants.PORT);
        socket.setTimeToLive(25);
        InetAddress group = InetAddress.getByName(Constants.IP_MULTICAST);
        socket.joinGroup(group);
        System.out.println("waiting for a video feed...");
    }


    byte [] getVideoBytes() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[Constants.BUFFER_SIZE], Constants.BUFFER_SIZE);
        socket.receive(packet);
        System.out.println("got some data");
        int numBytes = packet.getLength();
        byte [] videoBytes = new byte[numBytes];
        System.arraycopy(packet.getData(), 0, videoBytes, 0, numBytes);
        return videoBytes;
    }

    void displayVideo(byte [] currentImageBytes) {
        //figure out how to display video
        System.out.println("updating GUI");

    }


}
