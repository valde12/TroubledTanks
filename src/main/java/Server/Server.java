package Server;

import GameForm.GameForm;
import org.jspace.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;


public class Server{

    private SpaceRepository chatRepository;
    private SequentialSpace chat;

    private String ip;


    public Server() {
        // Initialize the chatRepository and chat space
        chatRepository = new SpaceRepository();
        chat = new SequentialSpace();
    }


    // Run on Caspers Mac or another pc that is purely a server (i.e. no game running on that)
    public void Server () {

        SpaceRepository repository = new SpaceRepository();

        SequentialSpace gameRooms = new SequentialSpace();

        repository.add("gameRooms",gameRooms);

        repository.addGate("tcp://192.168.50.231:9001/?keep");



    }

    public void hostGame() throws IOException, InterruptedException {
        /*
            This part adds the ip that the user wants to host on to the remote space gameRooms
         */


        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        RemoteSpace gameRooms = new RemoteSpace("tcp://192.168.50.178:9001/gameRooms?keep");
        System.out.println("Enter your name");
        String name = in.readLine();
        name = name+" is hosting on ";
        System.out.println("Enter your IP");
        ip = in.readLine();
        gameRooms.put(name, ip, "9001");
        System.out.println("Hosting on " + ip + ":9001");


        chatRepository.add("chat",chat);
        chatRepository.addGate("tcp://"+ip+":9001/?keep");

        //TODO: Implement logic for hosting game


    }


    public void startGame() throws InterruptedException {
        chatRepository.closeGates();
        List<Object[]> c = chat.queryAll(new ActualField("Hello From"), new FormalField(String.class));
        int i = c.size();
        new GameForm(3);
    }



}
