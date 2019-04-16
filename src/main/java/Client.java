import java.net.DatagramSocket;
import java.net.SocketException;

public class Client {


    private int port;
    private DatagramSocket socket;

    public Client(int port) throws SocketException {
        this.port = port;
        socket = new DatagramSocket(port);

    }


    void connectToHost(String connectionIP) {
        //connect to connectionIP




    }

    void disconnectFromHost() {
        //disconnect from host, perhaps on a button press, or on closing window?

    }

    void displayVideo() {
        //figure out how to display video


    }


}
