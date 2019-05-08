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
    private String roomPassword;
    private AES aes;

    private ImageWriter writer;
    private ImageWriteParam jpgWriteParam;

    private final int fps = 18;
    //here 1000ms
    private final int frameFireRate = 1000/fps;

    final ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> videoHandler;


    int numImagesTaken = 0;


    /**
     * Initializes server
     *
     * @param ip    The IP address we wish to stream to. Can be a multicast address or an individual computers IP addres.
     * @param roomPassword  The password to encrypt/decrypt the data. All outgoing Data from the server will be encrypted using this password
     * @throws SocketException  Throws if socket port is being used
     */
    public Server(String ip, String roomPassword) throws SocketException {

        this.roomPassword = roomPassword;
        aes = new AES();

        socket = new DatagramSocket(Constants.UNICAST_PORT);

        try {
            group = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        setCompressionLevel();
        initializeWebcam();
        startStream();
    }


    /**
     * This class is used to send Images. When called in the startStream method, this function will be triggered every 'fireRate' ms.
     */
    class WebcamSender implements Runnable {

        public void run() {
            try {
                sendImage(captureImage());
            } catch (IOException e) {
                System.out.println("Program closed");
            }
        }

    }


    /**
     * Sets the compression level for the images being sent over the stream. By default 45% of original Jpeg quality is being used (as specified in imageQuality field)
     */
    private void setCompressionLevel() {
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(0.5f);
        writer = ImageIO.getImageWritersByFormatName("jpg").next();
        jpgWriteParam = writer.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(imageQuality);
    }


    /**
     * Initializes a webcam instance, and allows the server to view whatever the webcam sees
     */
    private void initializeWebcam() {
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(320,240));

        cameraOpen = true;

        //this code here essentially just creates a window that displays current webcam view
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);

        JFrame window = new JFrame("Stream view");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }


    /**
     * Starts streaming to the appropriate destination. The videoHandler creates a new WebcamSender instance every 'fireRate' ms, in order to get a smooth
     * stream of images. Keeps doing so until the server quits the application
     */
    private void startStream() {

        //send messages at a fixed rate
        videoHandler = scheduler.scheduleAtFixedRate(new WebcamSender(), 0,frameFireRate, TimeUnit.MILLISECONDS);


        while (cameraOpen) {
            //wait for camera to close
        }

    }


    /**
     * Sends an image to a client
     *
     * @param image A byte representation of the image to be sent. Note that the image will be divided into several chunks to be streamed
     * @throws IOException If error sending the packet through the socket
     */

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

            //ENCRYPT DATA RIGHT HERE
            byte [] dataTosend = aes.encrypt(chunk.getBytes(), roomPassword.getBytes());

            DatagramPacket packet = new DatagramPacket(dataTosend, dataTosend.length, group, Constants.UNICAST_PORT);

            socket.send(packet);
        }


    }


    /**
     * Used to get a byte [] representation of the current webcam view. The image will be compressed at the level specified by the imageQuality field
     * @return byte [] representation of the webcam view
     * @throws IOException if image output stream fails
     */

    //return compressed byte [] representation of the webcam view
    private byte [] captureImage() throws IOException {

        BufferedImage image = webcam.getImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(baos);
        writer.setOutput(outputStream);
        writer.write(null, new IIOImage(image, null,null), jpgWriteParam);
        byte [] compressedImg = baos.toByteArray();

        numImagesTaken++;
        return compressedImg;
    }



}
