package Client;

import GameForm.*;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import javax.swing.*;
import java.awt.*;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client {
    public boolean start = false;
    private List<String> playerIps;
    private String ip;
    private String id;
    private List<String> ids;
    private RemoteSpace playerMovement;

    private static final String HOST_PORT = "9001";
    private static final String TCP_PREFIX = "tcp://";
    private static final String SERVER_IP = "10.209.145.174";

    public void client() throws IOException, InterruptedException {

        /*
            This part queries all the ip's and ports from the remote space gameRooms and lists them out
         */
        try {
            InetAddress address = InetAddress.getLocalHost();
            System.out.println("IP address: " + address.getHostAddress());
            ip = address.getHostAddress();
        } catch (UnknownHostException ex) {
            System.out.println("Could not find IP address for this host");
            System.out.println("Enter your ip");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            ip = in.readLine();
        }
        RemoteSpace gameRooms = new RemoteSpace(TCP_PREFIX + SERVER_IP + ":" + HOST_PORT + "/gameRooms?keep");
        List<Object[]> gRooms = gameRooms.queryAll(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));

        for (Object[] room : gRooms) {
            System.out.println(room[0] + " "  + room[1] + ":" + room[2]);
        }

        JFrame frame = new JFrame("Choose a Game Room");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Object[] room : gRooms) {
            listModel.addElement(room[1] + ":" + room[2]);
        }

        JList<String> roomList = new JList<>(listModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        frame.add(new JScrollPane(roomList), BorderLayout.CENTER);

        JButton joinButton = new JButton("Join");
        joinButton.addActionListener(e -> {
            String selectedRoom = roomList.getSelectedValue();
            if (selectedRoom != null) {
                System.out.println("Selected room: " + selectedRoom);
                // Pass selectedRoom to the joinGame method or handle it
                try {
                    joinGame(selectedRoom);
                } catch (IOException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a room.");
            }
        });
        frame.add(joinButton, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public void joinGame(String selectedRoom) throws IOException, InterruptedException {
        RemoteSpace chat = new RemoteSpace(TCP_PREFIX + selectedRoom + "/chat?keep");
        List<Object[]> c = chat.queryAll(new ActualField("Hello From"), new FormalField(String.class), new FormalField(String.class));
        id = String.valueOf(c.size());
        chat.put("Hello From", ip, id);
        boolean isHost = false;
        while(!start){
            Object[] t = chat.queryp(new ActualField("Start"));
            if(t != null  && Objects.equals(t[0].toString(), "Start")){
                start = true;
                System.out.println("Start");
            }
        }
        List<Object[]> a = chat.queryAll(new ActualField("Hello From"), new FormalField(String.class), new FormalField(String.class));
        playerIps = new ArrayList<>();
        ids = new ArrayList<>();
        for (Object[] players : a) {
            playerIps.add((String) players[1]);
            ids.add((String) players[2]);
        }
        chat.close();
       // playerMovement = new RemoteSpace("tcp://"+selectedRoom+":playerMovement/?keep");
        new GameController(playerIps,ip,isHost, id, ids);
    }


}
