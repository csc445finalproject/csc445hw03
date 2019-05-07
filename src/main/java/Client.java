import Misc.Constants;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Client extends JFrame implements ActionListener {

    /*
    BACK END COMPONANTS
     */


    final int imageQueueSize = 100;

    Thread receiveVideo;
    Thread updateVideo;

    ConcurrentHashMap<Integer, ImagePacket> images = new ConcurrentHashMap<Integer, ImagePacket>();
    BlockingQueue<ImagePacket> imageQueue = new ArrayBlockingQueue<ImagePacket>(imageQueueSize);

    MulticastSocket socket;
    DatagramSocket UNICAST_SOCKET, mcForward;

    boolean streamOver, isMultiHost;

    InetAddress group;

    String passcode;
    String myIp = Main.printExternalIP();



    /*

    GUI COMPONANTS
     */

    JFrame frame;
    JPanel panels;
    JPanel imagePanel;
    JPanel forwardingPanel;
    JPanel textPanel;

    JLabel imageLabel;
    BufferedImage image;
    ImageIcon imageIcon;

    ButtonGroup forwardingOptions;
    JRadioButton isForwarder;
    JRadioButton isMcastClient;
    JComboBox multicastPort;

    JTextField passwordField;

    JButton connectButton;




    public Client() throws IOException {

        initializeGUI();

    }

    public void initializeGUI() throws IOException {
        frame = new JFrame("csc445hw03 Client");
        frame.setSize(new Dimension(400, 600));
        imagePanel = new JPanel();
        textPanel = new JPanel();

        //configure all of the image displaying stuff
        imageLabel = new JLabel();
        image = ImageIO.read(new File("WaitingForStream.jpg"));
        imageIcon = new ImageIcon(image);
        imageLabel.setIcon(imageIcon);
        imageLabel.setMinimumSize(new Dimension(500, 500));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);


        forwardingPanel = new JPanel();
        forwardingOptions = new ButtonGroup();

        isForwarder = new JRadioButton("Forwarder", true);
        isMcastClient = new JRadioButton("Regular client");
        forwardingOptions.add(isForwarder);
        forwardingOptions.add(isMcastClient);


        String [] multicastPorts = {Constants.IP_MULTICAST};
        multicastPort = new JComboBox(multicastPorts);
        multicastPort.setSelectedIndex(0);

        forwardingPanel.add(new JLabel("My IP: " + myIp + "     "), BorderLayout.NORTH);
        forwardingPanel.add(isForwarder, BorderLayout.LINE_START);
        forwardingPanel.add(isMcastClient, BorderLayout.CENTER);
        forwardingPanel.add(multicastPort, BorderLayout.LINE_END);



        passwordField = new JTextField(50);
        passwordField.setText("Enter room password");
        passwordField.setToolTipText("Enter room password");
        //remove the default text when pressed
        passwordField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                passwordField.setText("");
            }
        });

        connectButton = new JButton("Connect");
        connectButton.addActionListener(this);

        panels = new JPanel();


        //make all componants stack vertically
        panels.setLayout(new BorderLayout());
        textPanel.setLayout(new BorderLayout());
        imagePanel.setMinimumSize(new Dimension(500, 300));
        imagePanel.setPreferredSize(new Dimension(500, 300));


        //add the image
        imagePanel.add(imageLabel);



        textPanel.add(forwardingPanel, BorderLayout.NORTH);
        textPanel.add(passwordField, BorderLayout.CENTER);
        textPanel.add(connectButton, BorderLayout.SOUTH);

        panels.add(imagePanel, BorderLayout.NORTH);
        panels.add(textPanel, BorderLayout.SOUTH);
        frame.add(panels);
        frame.setVisible(true);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }


    void forwardFromHostToMcast(String mcastAddr) throws IOException {
        //connect to connectionIP
        group = InetAddress.getByName(mcastAddr);
        socket = new MulticastSocket(Constants.MULTICAST_PORT);
        socket.setTimeToLive(25);
        socket.joinGroup(group);
        isMultiHost = false;
        System.out.println("waiting for a video feed...");
    }

    void connectToMcastSocket(String mcastAddr) throws IOException {

        group = InetAddress.getByName(mcastAddr);
        UNICAST_SOCKET = new DatagramSocket(Constants.UNICAST_PORT);
        mcForward = new DatagramSocket(Constants.MULTICAST_PORT);
        isMultiHost = true;
        System.out.println("waiting for a video feed...");
    }



    //purpose of this function is to update the display whenever the receive video function has received enough frames

    void updateDisplay() throws IOException {


        while (true) {
            System.out.println("Updating display");

            //this if statement will make sure we have buffered a few images before we begin updating the images
            if ((imageQueueSize - imageQueue.remainingCapacity()) > 5) {

                //remove image from both hashtable and queue
                ImagePacket currentImage = imageQueue.poll();
                images.remove(currentImage.imageNum);
                byte [] imageData = currentImage.getImageData();
                //display the image if its a good image
                if (imageData != null) {
                    imageIcon = new ImageIcon(imageData);
                    imageLabel.setIcon(imageIcon);
                }
            }

            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                System.out.println("Time to update the display!");

                if (streamOver) {
                    try {
                        image = ImageIO.read(new File("WaitingForStream.jpg"));
                    } catch (IOException e1) {
                        System.out.println("Image not found");
                    }

                    imageIcon = new ImageIcon(image);
                    imageLabel.setIcon(imageIcon);
                    System.out.println("Stream ended");
                    break;
                }
            }
        }
    }

    /*

    purpose of this function is to continuously listen for datagram packets, and whenever we receive a packet,
    we add it to the queue if it is a new image, or we update the image by adding the appropriate bytes.

    when we receive a new image, we tell the updateDisplay() method to update the image being displayed
     */


    void receiveVideo() throws IOException {

         /*

         4 bytes(int)  2 bytes(short)  2 bytes(short)     byte[]
            -------------------------------------------------------
           | imageNum |  order         |   numChunks |    data    |
            -------------------------------------------------------

         */

        DatagramPacket incomingFrame = new DatagramPacket(new byte[Constants.IMAGE_CHUNK_SIZE], Constants.IMAGE_CHUNK_SIZE);
        if (isMultiHost){

            UNICAST_SOCKET.setSoTimeout(3000);
        }
        else {
            socket.setSoTimeout(3000);
        }

        while (true) {
            try {
                //socket.receive(incomingFrame);
                if(isMultiHost) {
                    UNICAST_SOCKET.receive(incomingFrame);
                    System.out.println("Packet forwarded");
                    byte [] incData = incomingFrame.getData();
                    DatagramPacket forwardPacket = new DatagramPacket(incData, incData.length, group, Constants.MULTICAST_PORT);
                    mcForward.send(forwardPacket);
                }
                else socket.receive(incomingFrame);
                streamOver = false;
            } catch (SocketTimeoutException e) {
                //The host has ended their stream (probably)
                //e.printStackTrace();
                streamOver = true;
                updateVideo.interrupt();

                if(isMultiHost) {

                    //close both unicast socket and forwarding socket
                    UNICAST_SOCKET.close();
                    mcForward.close();
                }

                else {
                    //only close multicastSocket
                    socket.close();
                }

                connectButton.setEnabled(true);
                connectButton.setText("Connect");
                break;
            }

            byte[] data = incomingFrame.getData();
            ByteBuffer buffer = ByteBuffer.wrap(data);

            int imageNum = buffer.getInt();
            short order = buffer.getShort();
            short numChunks = buffer.getShort();

            byte [] chunkBytes = new byte[buffer.remaining()];
            buffer.get(chunkBytes);
            ImagePacket.ImageChunk imageChunk = new ImagePacket.ImageChunk(chunkBytes, imageNum, order, numChunks);

            if (!images.containsKey(imageChunk.imageNum)) {
                ImagePacket image = new ImagePacket(imageChunk.imageNum);
                image.addChunk(chunkBytes, imageNum, order, numChunks);
                images.put(imageChunk.imageNum, image);
                imageQueue.add(image);

                updateVideo.interrupt();
            } else {
                //we already have received chunks of this image, so we update everything
                images.get(imageChunk.imageNum).addChunk(chunkBytes, imageNum, order, numChunks);
            }

            //tell the update video function that we just processed something, and display something new
        }


    }



    //Triggered when the user hits the connect button
    @Override
    public void actionPerformed(ActionEvent e) {
        //when the connect button is pressed

        receiveVideo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //join the appropriate socket
                    if (isMcastClient.isSelected()) {

                        //is a client
                        System.out.println("Connecting to multicast socket: " + multicastPort.getSelectedItem().toString());
                        connectToMcastSocket(multicastPort.getSelectedItem().toString());
                    } else {

                        //is forwarding packets to mcast address
                        System.out.println("Ready to forward packets to: " + multicastPort.getSelectedItem().toString());
                        forwardFromHostToMcast(multicastPort.getSelectedItem().toString());
                    }

                    //get the passcode from the user
                    passcode = passwordField.getText();
                    System.out.println("Passcode: " + passcode);

                    //listen continuously for video packets being sent in
                    receiveVideo();

                } catch (IOException e1) {
                    e1.printStackTrace();

                }
            }
        });

        updateVideo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //start displaying whatever is available in the queue
                    updateDisplay();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        connectButton.setEnabled(false);
        connectButton.setText("Connection in progress...");
        receiveVideo.start();
        updateVideo.start();


    }



}
