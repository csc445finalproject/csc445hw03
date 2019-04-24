import com.github.sarxos.webcam.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {

    private Webcam webcam;
    private ArrayList<Client> clients;
    private DatagramSocket socket;
    private float imageQuality = 0.99f;
    boolean cameraOpen;


    public Server(int port, int maxClients) throws SocketException {
        socket = new DatagramSocket(port);

        initializeWebcam();
        startStream();
    }




    private void initializeWebcam() {
        webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        cameraOpen = true;

        //this code here essentially just creates a window that displays current webcam view
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);

        JFrame window = new JFrame("Test webcam panel");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }


    private void startStream() {

        //start a thread here to receive connections
        //and also to listen for clients who wish to terminate their connection when they want to stop watching feed
        //listenForConnections();

        while (cameraOpen) {
            byte [] image = captureImage();

            //send image to all clients
            sendImage(image);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //tell clients there is no more stream


    }



    private void listenForConnections() {
        //listen for connections, and add them to the list of clients
        //also listen for clients who want to disconnect
        //we could design packets such that 1 is wanting to connect, and 0 is wanting to disconnect
        //only allow maxClients connections



    }



    private void sendImage(byte[] image) {
        //send the image to all the clients who are connected


    }

    private void tellClientsClosed() {


    }


    //return compressed byte [] representation of the webcam view
    private byte [] captureImage(){

            long start = System.nanoTime();
            byte [] test = WebcamUtils.getImageBytes(webcam, "jpg");
            long total = System.nanoTime() - start;

            System.out.println("Test Total time in ns: "+  total + " in ms : " + (total/1000000));

            return test;
    }







    /*

    TODO: Implement these functions if we decide to do anything with motion

    public void motionDetected(WebcamMotionEvent webcamMotionEvent) {

    }

    void DetectMotion() {
        WebcamMotionDetector detector = new WebcamMotionDetector(Webcam.getDefault());
        detector.setInterval(100); //
        detector.addMotionListener(this);
        detector.start();
    }
    */

}
