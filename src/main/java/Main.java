import Misc.Constants;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;


public class Main extends JFrame {


    public static void main(String[] args) {

        JFrame mainFrame = new JFrame("CSC445hw03");

        JPanel clientOptions = new JPanel();
        JPanel serverOptions = new JPanel();
        Border blackLine = BorderFactory.createLineBorder(Color.BLACK);
        serverOptions.setBorder(blackLine);

        JButton clientButton = new JButton("Launch Client");
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
        unicastPanel.add(unicastButton);
        unicastPanel.add(unicastDest);

        String[] multicastAddr = {Constants.IP_MULTICAST};
        JComboBox address = new JComboBox(multicastAddr);
        address.setSelectedIndex(0);
        JRadioButton multicastButton = new JRadioButton("Multicast", true);

        multicastPanel.add(multicastButton);
        multicastPanel.add(address);

        serverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

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
                            new Server(addr);
                        } catch (SocketException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                serverThread.start();
            }
        });


        ipType.add(multicastButton);
        ipType.add(unicastButton);

        serverOptions.add(unicastPanel);
        serverOptions.add(multicastPanel);
        serverOptions.add(serverButton, BorderLayout.SOUTH);

        mainFrame.add(clientOptions, BorderLayout.SOUTH);
        mainFrame.add(serverOptions, BorderLayout.NORTH);

        mainFrame.setVisible(true);
        mainFrame.pack();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }


    public static String printExternalIP() throws UnknownHostException {
        String systemipaddress;
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            systemipaddress = sc.readLine().trim();
        } catch (Exception e) {
            systemipaddress = "Cannot Execute Properly";
        }
        System.out.println("Public IP Address: " + systemipaddress);
        return systemipaddress;
    }


}



