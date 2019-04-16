public class Packet {
    //simple packet class to manage connections between server and client
    //client can send over a 1 to server to signal wanting to start listening to the service
    //client can send over a 0 to server to signal wanting to disconnect from the service

    final static int CONNECT = 1;
    final static int DISCONNECT = 0;

    public Packet(int status) {



    }




}
