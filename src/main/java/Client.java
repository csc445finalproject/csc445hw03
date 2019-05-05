import Misc.Constants;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Client extends JPanel implements ActionListener {


    final int imageQueueSize = 100;

    Thread receiveVideo;
    Thread updateVideo;

    ConcurrentHashMap<Integer, ImagePacket> images = new ConcurrentHashMap<Integer, ImagePacket>();
    BlockingQueue<ImagePacket> imageQueue = new ArrayBlockingQueue<ImagePacket>(imageQueueSize);

    JFrame frame;
    JPanel panels;
    JPanel imagePanel;
    JPanel textPanel;

    JLabel imageLabel;
    BufferedImage image;
    ImageIcon imageIcon;

    JTextField ipTextField;

    JTextField passwordField;

    JButton connectButton;

    MulticastSocket socket;


    public Client() throws IOException {

        initializeGUI();

    }

    public void initializeGUI() throws IOException {
        frame = new JFrame("csc445hw03");
        frame.setSize(new Dimension(400, 600));
        imagePanel = new JPanel();
        textPanel = new JPanel();

        imageLabel = new JLabel();
        image = ImageIO.read(new File("pep.jpg"));
        imageIcon = new ImageIcon(image);
        imageLabel.setIcon(imageIcon);
        imageLabel.setMinimumSize(new Dimension(500, 500));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);


        ipTextField = new JTextField(50);
        ipTextField.setText("Enter host IP address");
        ipTextField.setToolTipText("Enter host IP address");

        passwordField = new JTextField(50);
        passwordField.setText("Enter room password");
        passwordField.setToolTipText("Enter room password");

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

        textPanel.add(ipTextField, BorderLayout.NORTH);
        textPanel.add(passwordField, BorderLayout.CENTER);
        textPanel.add(connectButton, BorderLayout.SOUTH);

        panels.add(imagePanel, BorderLayout.NORTH);
        panels.add(textPanel, BorderLayout.SOUTH);
        frame.add(panels);
        frame.setVisible(true);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }


    void connectToHost() throws IOException {
        //connect to connectionIP
        socket = new MulticastSocket(Constants.PORT);
        socket.setTimeToLive(25);
        InetAddress group = InetAddress.getByName(Constants.IP_MULTICAST);
        socket.joinGroup(group);
        System.out.println("waiting for a video feed...");
    }

    void connectToHost(String ip) throws IOException {
        socket = new MulticastSocket(Constants.PORT);
        socket.setTimeToLive(25);
        InetAddress group = InetAddress.getByName(ip);
        socket.joinGroup(group);
        System.out.println("waiting for a video feed...");
    }



    //purpose of this function is to update the display whenever the receive video function has received enough frames

    void updateDisplay() throws InvocationTargetException {

        // TODO : continuously update video by taking whatever is at the front of the queue at given intervals

        while (true) {
            System.out.println("Updating display");


            //this if statement will make sure we have buffered a few images before we begin updating the images
            if (imageQueue.remainingCapacity() < 60) {

                //remove image from both hashtable and queue
                ImagePacket currentImage = imageQueue.poll();
                images.remove(currentImage.imageNum);

                //display the image if its a good image
                if (currentImage != null) {
                    imageIcon = new ImageIcon(currentImage.getImageData());
                    imageLabel.setIcon(imageIcon);
                }
            }


            try {
                //TODO: sleep for a second and have the receiving thread interrupt this thread to tell it that
                //TODO: more data is ready
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Time to update the display!");
                e.printStackTrace();
            }

        }

    }

    /*

    purpose of this function is to continuously listen for datagram packets, and whenever we receive a packet,
    we add it to the queue if it is a new image, or we update the image by adding the appropriate bytes.

    when we receive a new image, we tell the updateDisplay() method to update the image being displayed
     */


    void receiveVideo() {

        DatagramPacket incomingFrame = new DatagramPacket(new byte[Constants.BUFFER_SIZE], Constants.BUFFER_SIZE);

        while (true) {
            try {
                socket.receive(incomingFrame);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] data = new byte[incomingFrame.getLength()];
            System.arraycopy(incomingFrame.getData(), 0, data, 0, data.length);

            ImagePacket.ImageChunk imageChunk = new ImagePacket.ImageChunk(data);

            if (images.contains(imageChunk.imageNum)) {
                ImagePacket image = new ImagePacket(imageChunk.imageNum);
                images.put(imageChunk.imageNum, image);
                imageQueue.add(image);
            } else {
                //we already have received chunks of this image, so we update everything
                images.get(imageChunk.imageNum).addChunk(data);
            }

            //tell the update video function that we just processed something, and display something new
            updateVideo.interrupt();
        }


    }



    //Triggered when the user hits the connect button
    @Override
    public void actionPerformed(ActionEvent e) {
        //when the connect button is pressed

        System.out.println("Connecting to " + ipTextField.getText());

        receiveVideo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //join the appropriate socket
                    if (ipTextField.getText().equals("") || ipTextField.getText() == null) {
                        System.out.println("Connecting to multicast socket: " + Constants.IP_MULTICAST);
                        connectToHost();
                    } else {
                        System.out.println("Connecting to: " + ipTextField.getText());
                        connectToHost(ipTextField.getText());
                    }

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
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
            }
        });



        receiveVideo.start();
        updateVideo.start();


    }


}
