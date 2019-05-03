import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Client extends JPanel{

    JFrame frame;
    JPanel panel;

    JLabel imageLabel;
    BufferedImage image;
    ImageIcon imageIcon;


    JLabel ipLabel;
    JTextField ipTextField;

    JLabel passwordLabel;
    JTextField passwordField;

    JButton connectButton;

    private MulticastSocket socket;
    private byte [] imageBytes;
    private InputStream in;


    public Client() throws IOException {

        initializeGUI();



//        connectToHost();
//        receiveVideoFeed();
    }

    public void initializeGUI() throws IOException {
        frame = new JFrame("csc445hw03");
        panel = new JPanel();
        imageLabel = new JLabel();
        image = ImageIO.read(new File("pep.jpg"));
        imageIcon = new ImageIcon(image);
        imageLabel.setIcon(imageIcon);
        ipLabel = new JLabel("Host IP Address: ");
        ipTextField = new JTextField();
        ipLabel.setLabelFor(ipTextField);

        passwordLabel = new JLabel("Room password: ");
        passwordField = new JTextField();
        passwordLabel.setLabelFor(passwordField);



        connectButton = new JButton("Connect");




        //make all componants stack vertically
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        //add the image
        panel.add(imageLabel);
        //add label and fields for ip and passwords
        panel.add(ipLabel);
        panel.add(ipTextField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(connectButton);

        frame.add(panel);
        frame.setVisible(true);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Connecting button pressed");
                System.out.println("Ip: " + ipTextField.getText() + " Password: " + passwordField.getText());
                try {
                    dummyStream(ipTextField.getText());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

    }

    private void receiveVideoFeed() throws IOException {

        while (true) {
            updateDisplay(getVideoBytes());
        }
    }


    void connectToHost() throws IOException {
        //connect to connectionIP
        socket = new MulticastSocket(Constants.PORT);
        socket.setTimeToLive(25);
        InetAddress group = InetAddress.getByName(Constants.IP_MULTICAST);
        socket.joinGroup(group);
        System.out.println("waiting for a video feed...");
    }


    byte [] getVideoBytes() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[Constants.BUFFER_SIZE], Constants.BUFFER_SIZE);
        socket.receive(packet);
        System.out.println("got some data");
        int numBytes = packet.getLength();
        byte [] videoBytes = new byte[numBytes];
        System.arraycopy(packet.getData(), 0, videoBytes, 0, numBytes);
        return videoBytes;
    }

    void updateDisplay(byte [] currentImageBytes) {
        //figure out how to display video
        imageIcon = new ImageIcon(currentImageBytes);
        imageLabel.setIcon(imageIcon);
    }








    void dummyUpdateGUI() throws IOException {
        for (int i =0; ; i++){
            //update GUI indefinitely
            try {
                Thread.sleep(35);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //if even display a, if odd display b
            if ((i & 1) == 0 ) {
                imageIcon = new ImageIcon("a.png");
            } else {
                imageIcon = new ImageIcon("c.jpg");
            }

            imageLabel.setIcon(imageIcon);
        }
    }


    byte [] getDummyBytes() throws IOException {
        //this will use tcp just because its easier (as we dont have sliding windows setup yet)
        //so basically just receive a packet, and have dummy Update gui change the GUI
        imageBytes = new byte [Constants.BIG_TEST_BUFFER];
        int count = in.read(imageBytes);
        System.arraycopy(imageBytes,0,imageBytes,0,count);
        System.out.println("Received " + count + " bytes");

        return imageBytes;
    }


    public void dummyStream(String ip) throws IOException {

        Socket s = new Socket(ip, Constants.PORT);
        in = s.getInputStream();
        System.out.println("Connected to: " + ip);

        while (true) {

            updateDisplay(getDummyBytes());

        }


    }

}
