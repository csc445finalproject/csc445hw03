import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {


    static int port = 2689;


    public static void main(String[] args) {
        int role;
        try {
            role = Integer.parseInt(getInput("Type 0 for client, 1 for server: "));
        } catch (NumberFormatException e) {
            //server by default
            role = 0;
        }


        //we are server
        if (role == 0) {

            try {
                System.out.println(printExternalIP());
            } catch (UnknownHostException e) {
                System.out.println("Cannot get external IP...");
                e.printStackTrace();
            }

            int maxClients = Integer.parseInt(getInput("Enter max number of clients: "));

            try {
                Server server = new Server(port, maxClients);
            } catch (SocketException e) {
                System.out.println("Socket exception for server");
                e.printStackTrace();
            }


        //we are client
        } else if (role == 1) {

            try {
                Client client = new Client(port);
                String serverIp = getInput("Enter the server IP address");
                client.connectToHost(serverIp);
            } catch (SocketException e) {
                e.printStackTrace();
            }

        }


        System.out.println("Closing program...");

    }






    //return input for a given message
    public static String getInput(String message) {
        System.out.print(message);
        return (new Scanner(System.in)).nextLine();
    }

    public static String printExternalIP() throws UnknownHostException {
        String systemipaddress;
        try
        {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            systemipaddress = sc.readLine().trim();
        } catch (Exception e) {
            systemipaddress = "Cannot Execute Properly";
        }
        System.out.println("Public IP Address: " + systemipaddress);
        return systemipaddress;
    }


}



