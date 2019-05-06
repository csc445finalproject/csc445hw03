import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;



public class Main {


    public static void main(String[] args) {

        int role;
        try {
            role = Integer.parseInt(getInput("Type 0 for server, 1 for client: "));
        } catch (NumberFormatException e) {
            //server by default
            role = 0;
        }


        //we are server
        if (role == 0) {

            String name = getInput("Type server IP address: ");

            try {
                Server server = new Server(name);
            } catch (SocketException e) {
                System.out.println("Socket exception for server");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                //yes I know this is garbage...
                System.out.println("Webcam closed");
            }



        //we are client
        } else if (role == 1) {

            try {
                Client client = new Client();

            }catch (IOException e) {
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



