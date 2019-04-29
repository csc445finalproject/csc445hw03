import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastClient {
    private static MulticastSocket socket = null;
    private static byte [] messageBuffer = new byte[256];

    public static void main(String[] args) throws IOException{
        socket = new MulticastSocket(Constants.PORT);
        InetAddress group = InetAddress.getByName(Constants.IP_MULTICAST);
        socket.joinGroup(group);

        for(;;){
            // keep client listening, until either the client wants to terminate or host dies
            DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length);
            socket.receive(packet);
            // for now im doing testing with Strings
            String message = new String(packet.getData(), 0, packet.getLength()); // removes extra bytes from the buffer for string messages

            if(message.equals("lolyouwouldathoughtidiot")){
                break;
            }

        }

        socket.leaveGroup(group);
        socket.close();
    }
}
