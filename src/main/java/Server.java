import com.github.sarxos.webcam.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Misc.Constants;


public class Server {

    private final float imageQuality = 0.45f;
    private Webcam webcam;
    private InetAddress group;
    private DatagramSocket socket;
    private boolean cameraOpen;

    private ImageWriter writer;
    private ImageWriteParam jpgWriteParam;

    private final int fps = 18;
    //here 1000ms
    private final int frameFireRate = 1000/fps;

    final ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> videoHandler;


    int numImagesTaken = 0;


    public Server(String ip) throws SocketException {


        socket = new DatagramSocket(Constants.UNICAST_PORT);

        try {

            if (ip.equals("m")) {
                //if we want multicast, send right to the multicast socket
                System.out.println("Directly multicasting");
                group = InetAddress.getByName(Constants.IP_MULTICAST);
            } else {
                // if an IP address is entered (in our case, pi or rho or whatever server we choose)
                // then we forward everything there, and let them handle the multicast
                System.out.println("Sending to " + ip + " and letting them handle the multicast");
                group = InetAddress.getByName(ip);
                // I think in this case, we pass to a new class
            }

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
                System.out.println("took image");
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
        webcam.setViewSize(new Dimension(320,240));

        cameraOpen = true;

        //this code here essentially just creates a window that displays current webcam view
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);

        JFrame window = new JFrame("Test webcam panels");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }


    private void startStream() {

        //send messages at a fixed rate
        videoHandler = scheduler.scheduleAtFixedRate(new WebcamSender(), 0,frameFireRate, TimeUnit.MILLISECONDS);


        while (cameraOpen) {
            //wait for camera to close
        }

        //tell clients there is no more stream
        //tellClientsClosed();

        // rather than doing ^^ , we simply stop streaming, and the clients will timeout and will figure out there is no more stream

    }



    //Used to send an entire image over.
    //Note that the param image is the byte [] representing the ENTIRE IMAGE
    //and must be divided and sectionned off into multiple imageChunks
    private void sendImage(byte[] image) throws IOException {

        ByteBuffer buffer = ByteBuffer.wrap(image);
        int numChunks = (image.length / Constants.BUFFER_SIZE) + 1;
        short shortChunks = (short)numChunks;
        for (short i = 0; i< shortChunks; i++) {

            byte [] imageChunkData;

            //lastPacket
            if (i == numChunks-1) {
                imageChunkData = new byte[buffer.remaining()];

            } else {
                //not the last packet
                imageChunkData = new byte [Constants.BUFFER_SIZE];
            }

            buffer.get(imageChunkData);

            ImagePacket.ImageChunk chunk = new ImagePacket.ImageChunk(imageChunkData, numImagesTaken, i, shortChunks);
            byte [] dataTosend = chunk.getBytes();

            DatagramPacket packet = new DatagramPacket(dataTosend, dataTosend.length, group, Constants.UNICAST_PORT);

            socket.send(packet);
        }


    }



    private void tellClientsClosed() {

        //TODO: send some sort of escape sequence to multicast socket signaling that the broadcast is over
        webcam.close();
        System.out.println("Closing feed");
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

        numImagesTaken++;
        return compressedImg;
    }



}
