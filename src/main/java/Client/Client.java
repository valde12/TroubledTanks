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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client {
    public boolean start = false;
    private List<String> playerIps;
    private String ip;
    private RemoteSpace playerMovement;
    public void client() throws IOException, InterruptedException {

        /*
            This part queries all the ip's and ports from the remote space gameRooms and lists them out
         */
        System.out.println("Enter your ip");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        ip = in.readLine();
        RemoteSpace gameRooms = new RemoteSpace("tcp://192.168.50.178:9001/gameRooms?keep");
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
        //TODO: implement logic for joining
        RemoteSpace chat = new RemoteSpace("tcp://"+ selectedRoom + "/chat?keep");
        chat.put("Hello From", ip);
        boolean isHost = false;
        while(!start){
            Object[] t = chat.get(new ActualField("Start"));
            if(Objects.equals(t[0].toString(), "Start")){
                start = true;
                System.out.println("Start");
            }
        }

        List<Object[]> c = chat.queryAll(new ActualField("Hello From"), new FormalField(String.class));
        playerIps = new ArrayList<>();
        for (Object[] players : c) {
            playerIps.add((String) players[1]);
        }
        chat.close();
        playerMovement = new RemoteSpace("tcp://"+selectedRoom+":playerMovement/?keep");
        new GameController(playerIps,ip,isHost);
    }

    public void putplayerMovement(float playerX, float playerY) throws InterruptedException {
        playerMovement.put(playerX, playerY);
    }

    public Object[] getPlayerMovement() throws InterruptedException {
        return playerMovement.queryp(new FormalField(String.class), new FormalField(float.class), new FormalField(float.class));

    }
}
