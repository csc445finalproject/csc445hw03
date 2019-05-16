import Misc.Constants;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;


public class Main extends JFrame {


    public static void main(String[] args) {

        JFrame mainFrame = new JFrame("CSC445hw03");

        JPanel clientOptions = new JPanel();
        JPanel serverOptions = new JPanel();
        Border blackLine = BorderFactory.createLineBorder(Color.BLACK);
        serverOptions.setBorder(blackLine);

        JButton clientButton = new JButton("Launch Client");

        /**
         * Launch client gui on button press
         */
        clientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                mainFrame.setVisible(false);
                mainFrame.dispose();

                Thread clientThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new Client();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                clientThread.start();
            }
        });

        clientOptions.add(clientButton);

        JButton serverButton = new JButton("Launch Server");

        JPanel unicastPanel = new JPanel();
        JPanel multicastPanel = new JPanel();

        ButtonGroup ipType = new ButtonGroup();

        JRadioButton unicastButton = new JRadioButton("Forward to client");
        JTextField unicastDest = new JTextField(20);
        unicastDest.setToolTipText("Enter the client to which you would like to stream directly to. This client will be receiving the video stream, and will be forwarding all data to the multicast socket it specifies");
        unicastPanel.add(unicastButton);
        unicastPanel.add(unicastDest);

        String[] multicastAddr = {Constants.IP_MULTICAST};
        JComboBox address = new JComboBox(multicastAddr);
        address.setSelectedIndex(0);
        JRadioButton multicastButton = new JRadioButton("Multicast", true);

        multicastPanel.add(multicastButton);
        multicastPanel.add(address);


        JTextField passwordField = new JTextField("Enter room password");
        passwordField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                passwordField.setText("");
            }
        });
        passwordField.setToolTipText("Enter room password");


        ipType.add(multicastButton);
        ipType.add(unicastButton);

        serverOptions.add(unicastPanel);
        serverOptions.add(multicastPanel);

        JPanel miscServPanel = new JPanel();
        miscServPanel.add(passwordField);
        miscServPanel.add(serverButton);


        serverOptions.add(miscServPanel);

        mainFrame.add(clientOptions, BorderLayout.SOUTH);
        mainFrame.add(serverOptions, BorderLayout.NORTH);

        mainFrame.setVisible(true);
        mainFrame.pack();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        /**
         * launch server on button press
         */
        serverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {



                if (!passwordField.getText().equals("Enter room password") && !passwordField.getText().equals("")) {
                    //valid password was entered

                    //get rid of this window in favour of the regular server window
                    mainFrame.setVisible(false);
                    mainFrame.dispose();

                    String addr;

                    if (unicastButton.isSelected()) {
                        addr = unicastDest.getText();
                    } else {
                        //get the section from the combo box
                        addr = address.getSelectedItem().toString();
                    }

                    System.out.println("sending packets to: " + addr);
                    Thread serverThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new Server(addr, passwordField.getText());
                            } catch (SocketException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });

                    serverThread.start();

                } else {

                    passwordField.setForeground(Color.RED);
                    passwordField.setText("Enter room password");


                }

            }
        });

    }


    /**
     * Print external ip to
     * @return  the device running this programs external IP address
     */

    public static String printExternalIP() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("ifconfig");
        builder.redirectErrorStream(true);
        Process process = builder.start();
        InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            String [] content = line.split(" ");

            if (content[0].contains("inet") && !content[0].contains("6") && !line.contains("127.0.0.1")) {

                if (content[1].contains("addr")) {
                    String ip = content[1].split(":")[1];
                    System.out.println(ip);
                    return ip;
                } else {
                    String ip = line.split(" ")[1];
                    System.out.println(ip);
                    return ip;
                }

            }
        }

        return "IP N/A";
    }


}



