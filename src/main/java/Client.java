import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public class Client extends JPanel implements ActionListener {

    JFrame frame;
    JPanel panels;
    JPanel imagePanel;
    JPanel textPanel;

    JLabel imageLabel;
    BufferedImage image;
    private ImageIcon imageIcon;

    JTextField ipTextField;

    JTextField passwordField;

    JButton connectButton;

    private MulticastSocket socket;
    private byte[] imageBytes;
    private InputStream in;

    String ip;


    public Client() throws IOException {

//        System.out.println("Enter Ip Address: " );
//        ip = (new Scanner(System.in)).nextLine();


        initializeGUI();
        //dummyStream(ip);

//        connectToHost();
//        receiveVideoFeed();
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
        imagePanel.setMinimumSize(new Dimension(300, 300));


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

    private void receiveVideoFeed() throws IOException, InvocationTargetException, InterruptedException {

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


    byte[] getVideoBytes() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[Constants.BUFFER_SIZE], Constants.BUFFER_SIZE);
        socket.receive(packet);
        System.out.println("got some data");
        int numBytes = packet.getLength();
        byte[] videoBytes = new byte[numBytes];
        System.arraycopy(packet.getData(), 0, videoBytes, 0, numBytes);
        return videoBytes;
    }

    void updateDisplay(byte[] currentImageBytes) throws InvocationTargetException, InterruptedException {
        //figure out how to display video
        System.out.println("Updating display");
        imageIcon = new ImageIcon(currentImageBytes);
        imageLabel.setIcon(imageIcon);
    }


    void dummyUpdateGUI() throws IOException {
        for (int i = 0; ; i++) {
            //update GUI indefinitely
            try {
                Thread.sleep(35);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //if even display a, if odd display b
            if ((i & 1) == 0) {
                imageIcon = new ImageIcon("a.png");
            } else {
                imageIcon = new ImageIcon("c.jpg");
            }

            imageLabel.setIcon(imageIcon);
        }
    }


    byte[] getDummyBytes() throws IOException {
        //this will use tcp just because its easier (as we dont have sliding windows setup yet)
        //so basically just receive a packet, and have dummy Update gui change the GUI
        imageBytes = new byte[Constants.BIG_TEST_BUFFER];
        int count = in.read(imageBytes);
        System.arraycopy(imageBytes, 0, imageBytes, 0, count);
        System.out.println("Received " + count + " bytes");
        return imageBytes;
    }


    public void dummyStream(String ip) throws IOException, InvocationTargetException, InterruptedException {

        Socket s = new Socket(ip, Constants.PORT);
        in = s.getInputStream();
        System.out.println("Connected to: " + ip);

        while (true) {
            updateDisplay(getDummyBytes());
        }


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //when the connect button is pressed
        if (!ipTextField.getText().equals("") || !ipTextField.getText().equals("Enter host IP address")) {

            System.out.println("Connecting to " + ipTextField.getText());

            Thread stream = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        dummyStream(ipTextField.getText());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            stream.start();


        } else {
            System.out.println("invalid ip address");
        }


    }

}
