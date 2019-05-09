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

    public static boolean passwordCorrect = true;

    InetAddress group;

    String passcode;
    String myIp = Main.printExternalIP();

    AES aes;



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


    /**
     * CONSTRUCTOR
     *
     * Initializes an AES object for decryption and encryption at an extremely fast pace, also
     * generates a Gui that the user can interact with to either forawrd a video stream, or recieve
     * a video stream
     * @throws IOException
     */
    public Client() throws IOException {
        aes = new AES();
        initializeGUI();

    }

    /**
     * This is the specific method that generates and initializes the GUI
     * @throws IOException
     */
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
                passwordField.setForeground(Color.BLACK);
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


    /**
     * This establishes a multicast connection to the specific multicat address
     * @param mcastAddr - the multicast address that the IGMP agreed to use
     * @throws IOException
     */
    void connectToMcastSocket(String mcastAddr) throws IOException {
        //connect to connectionIP
        group = InetAddress.getByName(mcastAddr);
        socket = new MulticastSocket(Constants.MULTICAST_PORT);
        socket.setTimeToLive(25);
        socket.joinGroup(group);
        isMultiHost = false;
    }

    /**
     * Forwards the video stream to the multicast group, this was done because the
     * computer with a webcam was separated by multiple switches and unable to directly
     * connect to the multicast group
     * @param mcastAddr - the multicast address that the IGMP agreed to use
     * @throws IOException
     */
    void forwardFromHostToMcast(String mcastAddr) throws IOException {

        group = InetAddress.getByName(mcastAddr);
        UNICAST_SOCKET = new DatagramSocket(Constants.UNICAST_PORT);
        mcForward = new DatagramSocket(Constants.MULTICAST_PORT);
        isMultiHost = true;
    }


   /**
    * purpose of this function is to update the display whenever the receive video function
    * has received enough frames
    */
    void updateDisplay() {


        while (true) {

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

                if (streamOver) {
                    try {
                        image = ImageIO.read(new File("WaitingForStream.jpg"));
                    } catch (IOException e1) {
                        System.out.println("Image not found");
                    }

                    imageIcon = new ImageIcon(image);
                    imageLabel.setIcon(imageIcon);
                    break;
                } else if (!passwordCorrect) {

                    try {
                        image = ImageIO.read(new File("WaitingForStream.jpg"));
                    } catch (IOException e1) {
                        System.out.println("Image not found");
                    }

                    imageIcon = new ImageIcon(image);
                    imageLabel.setIcon(imageIcon);

                    passwordField.setForeground(Color.RED);
                    passwordField.setText("Password Incorrect");
                    passwordField.setEnabled(true);
                    break;
                }
            }
        }
    }


    /**
     * purpose of this function is to continuously listen for datagram packets, and whenever we receive a packet,
     * we add it to the queue if it is a new image, or we update the image by adding the appropriate bytes.
     *
     * when we receive a new image, we tell the updateDisplay() method to update the image being displayed
     * @throws IOException
    */
    void receiveVideo() throws IOException {

         /*

         4 bytes(int)  2 bytes(short)  2 bytes(short)     byte[]
            -------------------------------------------------------
           | imageNum |  order         |   numChunks |    data    |
            -------------------------------------------------------

         */

        DatagramPacket incomingFrame = new DatagramPacket(new byte[Constants.ENCRYPTED_CHUNKSIZE], Constants.ENCRYPTED_CHUNKSIZE);
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
                passwordField.setEnabled(true);
                connectButton.setText("Connect");
                break;
            }



            byte[] data = incomingFrame.getData();

            byte [] decryptedData = aes.decrypt(data, passcode.getBytes());

            if (!passwordCorrect) {

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


                passwordField.setEnabled(true);
                passwordField.setForeground(Color.RED);
                passwordField.setText("Password Incorrect");

                connectButton.setEnabled(true);
                connectButton.setText("Connect");
                break;
            }

            ByteBuffer buffer = ByteBuffer.wrap(decryptedData);



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


        }


    }


    /**
     * Triggered when the user hits the connect button
     * @param e - how the interrupt from the client is detected
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        //when the connect button is pressed

        passwordCorrect = true;

        receiveVideo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //join the appropriate socket
                    if (isMcastClient.isSelected()) {

                        //is a client
                        connectToMcastSocket(multicastPort.getSelectedItem().toString());
                    } else {

                        //is forwarding packets to mcast address
                        forwardFromHostToMcast(multicastPort.getSelectedItem().toString());
                    }

                    //get the passcode from the user
                    passcode = passwordField.getText();

                    passwordField.setEnabled(false);
                    passwordField.setForeground(Color.BLACK);

                    //listen continuously for video packets being sent in
                    receiveVideo();

                } catch (IOException e1) {
                    passwordCorrect = false;
                }
            }
        });

        updateVideo = new Thread(new Runnable() {
            @Override
            public void run() {
                    //start displaying whatever is available in the queue
                    updateDisplay();
            }
        });


        connectButton.setEnabled(false);
        connectButton.setText("Connection in progress...");
        receiveVideo.start();
        updateVideo.start();


    }



}
