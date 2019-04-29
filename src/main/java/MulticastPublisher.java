import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastPublisher {
    private static DatagramSocket socket = null;

    public static void main(String [] args) throws IOException{
        socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(Constants.IP_MULTICAST);
        // For now the publisher only sends one message at a time, client listens as long as he wants
        // probably want gui to handle things like leaving the group and invoking a method for this
        byte [] messageBytes = args[0].getBytes();
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, group, Constants.PORT);
        socket.send(packet);
        socket.close();

    }

    public static void multiCast(String message){
        // should handle what main does right now
    }
}
