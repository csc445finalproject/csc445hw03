import com.github.sarxos.webcam.*;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.github.sarxos.webcam.util.ImageUtils;


public class Server implements WebcamListener{

    private final float imageQuality = 0.01f;
    private Webcam webcam;
    private InetAddress group;
    private DatagramSocket socket;
    private boolean cameraOpen;

    private ImageWriter writer;
    private ImageWriteParam jpgWriteParam;

    private final int fps = 24;
    //here 1000ms
    private final int frameFireRate = 1000/fps;

    final ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> videoHandler;


    public Server(String ip) throws SocketException {
        socket = new DatagramSocket();


        if (ip.equals("testGui")) {
            try {
                dummyStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {

            if (ip.equals("multicast")) {
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



    public void webcamOpen(WebcamEvent webcamEvent) {

    }

    public void webcamClosed(WebcamEvent webcamEvent) {

    }

    public void webcamDisposed(WebcamEvent webcamEvent) {

    }

    public void webcamImageObtained(WebcamEvent webcamEvent) {

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

        //send messages at a fixed rate
        videoHandler = scheduler.scheduleAtFixedRate(new WebcamSender(), 0,frameFireRate, TimeUnit.MILLISECONDS);


        while (cameraOpen) {
            //wait for camera to close
        }

        //tell clients there is no more stream
        tellClientsClosed();

    }





    private void sendImage(byte[] image) throws IOException {
        //send the image to all the clients who are connected
        socket.send(new DatagramPacket(image, image.length, group, Constants.PORT));

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


        //uncomment this to see what image actually looks like

//        ByteArrayInputStream bis = new ByteArrayInputStream(compressedImg);
//        BufferedImage bImage2 = ImageIO.read(bis);
//        ImageIO.write(bImage2, "jpg", new File("output.jpg") );


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




    private void dummyStream() throws IOException {
        ServerSocket s = new ServerSocket(Constants.PORT);
        Socket client = s.accept();

        OutputStream out = client.getOutputStream();

        sendPictures(out);

    }


    private void sendPictures(OutputStream out) throws IOException {

        File pep1 = new File("pep.jpg");
        byte [] bytesArray = new byte[(int) pep1.length()];
        FileInputStream fis = new FileInputStream(pep1);
        fis.read(bytesArray);


        File pep2 = new File("pep2.jpg");
        byte [] bytesArray2 = new byte[(int) pep2.length()];
        FileInputStream fis2 = new FileInputStream(pep2);
        fis2.read(bytesArray2);


        //alternate sending the 2 images we have on file for testing purposess

        for (int i = 0; ; i++) {
            if ((i & 1) == 0 ) {
                out.write(bytesArray);
            } else {
                out.write(bytesArray2);
            }

            try {
                Thread.sleep(35);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


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
