import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Packet {
    //simple packet class to manage connections between server and client


    //a regular image will be over 20,000 bytes in length. If a byte [] is smaller than this, then we assume it to be
    //a password to get access to the video stream, and decode it accordingly
    byte [] data;


    //use this to decode incoming data
    public Packet(DatagramPacket d) {
        this.data = getData(d);
    }

    private byte[] getData(DatagramPacket d) {

        int numBytesReceived = d.getLength();
        byte [] data = new byte [numBytesReceived];
        System.arraycopy(d.getData(), 0, data ,0, numBytesReceived);
        return data;

    }


    //recover the password from the 'byte [] data'.
    public String getPassword() {

        //TODO: The encryption stuff for password needs to be done right around here

        return new String (data);

    }


    //Used for taking data we need to send, and creating a UDP datagram packet.
    public DatagramPacket createDatagramPacket(byte [] dataToBeSent, String ip, int port) throws UnknownHostException {
        DatagramPacket packet = new DatagramPacket(dataToBeSent, dataToBeSent.length, InetAddress.getByName(ip), port);
        return packet;
    }






}
