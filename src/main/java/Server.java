import com.github.sarxos.webcam.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.sarxos.webcam.util.ImageUtils;


public class Server {

    private final String multicastAddress = "230.12.69.12";
    private final float imageQuality = 0.5f;
    private Webcam webcam;
    private InetAddress group;
    private int port;
    private DatagramSocket socket;
    private boolean cameraOpen;

    private ImageWriter writer;
    private ImageWriteParam jpgWriteParam;

    private final int fps = 24;
    //here 1000ms
    private final int frameFireRate = 1000/fps;

    final ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> videoHandler;


    public Server(int port) throws SocketException {
        this.port = port;
        socket = new DatagramSocket();

        try {
            group = InetAddress.getByName(multicastAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        setCompressionLevel();
        initializeWebcam();
        startStream();
    }

    class WebcamSender implements Runnable {

        public void run() {
            try {
                sendImage(captureImage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void setCompressionLevel() {
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(0.5f);
        writer = ImageIO.getImageWritersByFormatName("jpg").next();
        jpgWriteParam = writer.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(imageQuality);
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

        //while (cameraOpen) {

            videoHandler = scheduler.scheduleAtFixedRate(new WebcamSender(), 0,frameFireRate, TimeUnit.MILLISECONDS);


//            //keep camera open a
            try {
                System.out.println("Getting ready to take pictures");
                Thread.sleep(1000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        //}

        //tell clients there is no more stream
        tellClientsClosed();

    }



    private void listenForConnections() {
        //listen for connections, and add them to the list of clients
        //also listen for clients who want to disconnect
        //we could design packets such that 1 is wanting to connect, and 0 is wanting to disconnect




    }



    private void sendImage(byte[] image) throws IOException {
        //send the image to all the clients who are connected
        socket.send(new DatagramPacket(image, image.length, group, port));

    }

    private void tellClientsClosed() {

        //TODO: send some sort of escape sequence to multicast socket signaling that the broadcast is over
        webcam.close();

    }


    //return compressed byte [] representation of the webcam view
    private byte [] captureImage() throws IOException {

        BufferedImage image = webcam.getImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(baos);
        writer.setOutput(outputStream);
        writer.write(null, new IIOImage(image, null,null), jpgWriteParam);
        byte [] compressedImg = baos.toByteArray();
        System.out.println("Took image");



        return compressedImg;


        /*

        alternatively, this makes images of higher quality, but far bigger.
        Not sure which is faster


        BufferedImage image = webcam.getImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "JPG", baos);
        byte[] data = baos.toByteArray();


        or even

        byte [] bytes = webcam.getImageBytes();

        but again, images are big, so speed could be issue here

         */
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
