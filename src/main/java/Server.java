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
import java.nio.ByteBuffer;
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
    private static long currentEstimateRTT = 64;
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

//    void sendFileWindowed(final int numPackets, final byte [] fileBytes) throws IOException{
//        int windowSize = calculateWindowSize(numPackets); // calculates initial window size with a max of 32
//
//        DatagramPacket dataPacket;
//        byte[] dataBytes = new byte[Constants.BUFFER_SIZE];
//        ByteBuffer buffer = ByteBuffer.wrap(fileBytes); // put file bytes in bytebuffer
//        ByteBuffer packetBuffer;
//        short seqNum = 1, startingSeqNum = 1; // starting seqnum is the initial window position
//        // seqnum is current seqnum to be sent
//        byte opCode = 3; // opcode for datapacket
//        byte finalWindowByte; // final windowbyte is a hack on the first byte of the datapacket
//        // = 1 if its the last packet in the window, so the server knows to send an ack
//        // = 0 if its not the last packet in the window, so the server shouldn't send anything
//        int remaining, remainingPackets; // remaining is for bytes in file, remainingPackets is for total packets left to send
//        long start, end; // start, end for calculating reasonable timeouts.
//        remainingPackets = numPackets;
//
//        /*
//         Iterate over the number of packets, and through the current window size until there are none left
//         send packets = to the current window size, and then receive an ack
//         if ack.seqnum < seqNum, move buffer and everything else to ack.seqnum, then start the new window at that point
//         */
//
//        // TODO: make packet objects so that OPCODE and SEQ_NUM actually have references, and figure out what to do with acks
//        while (remainingPackets != 0) {
//            start = System.currentTimeMillis();
//            for (int i = 0; i < windowSize; i++) {
//                if (i == windowSize - 1){
//                    finalWindowByte = 1;
//                } else {
//                    finalWindowByte = 0;
//                }
//                remaining = buffer.remaining();
//                if (seqNum == numPackets) {
//                    // final packet
//                    byte[] remain = new byte[remaining];
//                    buffer.get(remain);
//                    packetBuffer = ByteBuffer.allocate(remaining + OPCODE + SEQ_NUM);
//                    packetBuffer.put(finalWindowByte);
//                    packetBuffer.put(opCode);
//                    packetBuffer.putShort(seqNum);
//                    packetBuffer.put(remain);
//                } else {
//                    packetBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE + OPCODE + SEQ_NUM);
//                    buffer.get(dataBytes);
//                    packetBuffer.put(finalWindowByte);
//                    packetBuffer.put(opCode);
//                    packetBuffer.putShort(seqNum);
//                    packetBuffer.put(dataBytes);
//                }
//                byte[] packetBytes = packetBuffer.array();
//
//                packetBuffer.clear();
//
//                dataPacket = new DatagramPacket(packetBytes, packetBytes.length, group, Constants.PORT);
//                socket.send(dataPacket);
//
//                seqNum++;
//
//            }
//            try {
//                // try to receive ack packet, if it fails we timeout
//                // if it succeeds, increment our window size
//                socket.setSoTimeout((int) (2 * currentEstimateRTT));
//                socket.receive(ackPacket);
//                end = System.currentTimeMillis();
//                long sample = end - start;
//                currentEstimateRTT = estimateRTT(sample, currentEstimateRTT);
//                socket.setSoTimeout((int) (2 * currentEstimateRTT));
//                windowSize++;
//            } catch (SocketTimeoutException e){
//                // Hit timeout
//                // decrease windowsize
//                // increase timeout
//                // send entire window again
//                windowSize /= 2;
//                currentEstimateRTT *= 4;
//                socket.setSoTimeout((int) (2 * currentEstimateRTT));
//                seqNum = startingSeqNum; // revert seqnum to initial window position
//                buffer.position(Constants.BUFFER_SIZE * (seqNum - 1));
//                for (int i = 0; i < windowSize; i++) {
//                    if (i == windowSize - 1){
//                        finalWindowByte = 1;
//                    } else {
//                        finalWindowByte = 0;
//                    }
//                    remaining = buffer.remaining();
//                    if (seqNum == numPackets) {
//                        byte[] remain = new byte[remaining];
//                        buffer.get(remain);
//                        packetBuffer = ByteBuffer.allocate(remaining + OPCODE + SEQ_NUM);
//                        packetBuffer.put(finalWindowByte);
//                        packetBuffer.put(opCode);
//                        packetBuffer.putShort(seqNum);
//                        packetBuffer.put(remain);
//                    } else {
//                        packetBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE + OPCODE + SEQ_NUM);
//                        buffer.get(dataBytes);
//                        packetBuffer.put(finalWindowByte);
//                        packetBuffer.put(opCode);
//                        packetBuffer.putShort(seqNum);
//                        packetBuffer.put(dataBytes);
//                    }
//                    byte[] packetBytes = packetBuffer.array();
//
//                    packetBuffer.clear();
//
//                    dataPacket = new DatagramPacket(packetBytes, packetBytes.length, group, Constants.PORT);
//
//                    socket.send(dataPacket);
//                    seqNum++;
//                }
//                socket.receive(ackPacket);
//                end = System.currentTimeMillis();
//                long sample = end - start;
//                currentEstimateRTT = estimateRTT(sample, currentEstimateRTT);
//                socket.setSoTimeout((int) (2 * currentEstimateRTT));
//                // don't increment window size here
//            }
//
//            ByteBuffer seqBuffer = ByteBuffer.wrap(ackPacket.getData()); // process ack packet
//            seqBuffer.getShort();
//            short currentSeq = seqBuffer.getShort();
//            if(seqNum != currentSeq){
//                seqNum = currentSeq;
//                buffer.position(Constants.BUFFER_SIZE * (seqNum - 1));
//                // move buffer to ideal position, revert seqnum to whatever ack we receive
//                socket.setSoTimeout((int) (2 * currentEstimateRTT));
//            }
//            startingSeqNum = seqNum; // changee our initial window position
//
//            remainingPackets = numPackets - (seqNum - 1); // remaining packets is used to keep track of final window size
//            if(remainingPackets < windowSize){
//                // less packets than our current window, change size
//                windowSize = remainingPackets;
//            }
//
//
//        }
//
//
//        // Clean up resources
//        socket.close();
//    }

    int calculateWindowSize(final int numPackets){
        int maxInitSize = 32;

        int size = numPackets / 4;

        if(size > maxInitSize) return maxInitSize;
        else return 32 - Integer.numberOfLeadingZeros(size - 1) + 1;
    }

    int calculateNumPackets(){
        // calculate the total number of packets we need
        return 0;
    }

    long estimateRTT(final long sample, final long estimate) {
        // Used a sample algorithm to determine a reasonable socket timeout time, limiting the
        // shortest amount of time to 2 ms
        double beta = 1/8d;
        double alpha = 1 - beta;
        double alphaestimate =  alpha * estimate;
        double samplebeta =  beta * sample;
        long res = (long)(alphaestimate + samplebeta);
        if (res <= 2) res = 2; // upper bound for rtts is 2
        return res;
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

        byte[] pep1 = new byte[(int) (new File("pep.jpg").length())];
        FileInputStream fs = new FileInputStream("pep.jpg");
        fs.read(pep1);
        fs.close();


        byte[] pep2 = new byte[(int) (new File("pep2.jpg").length())];
        FileInputStream fs2 = new FileInputStream("pep2.jpg");
        fs2.read(pep2);
        fs2.close();


        //alternate sending the 2 images we have on file for testing purposess

        for (int i = 0; ; i++) {
            if ((i & 1) == 0 ) {
                out.write(pep1);
                System.out.println("Sent " + pep1.length + " bytes");
            } else {
                out.write(pep2);
                System.out.println("Sent " + pep2.length + " bytes");
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
