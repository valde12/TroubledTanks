package Server;

import GameForm.GameController;
import org.jspace.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class Server{

    private SpaceRepository chatRepository;
    private SequentialSpace chat;

    private String ip;
    private String id;
    private PileSpace playerMovement;
    private List<String> playerIps;
    private List<String> ids;

    public Server() {
        // Initialize the chatRepository and chat space
        chatRepository = new SpaceRepository();
        chat = new SequentialSpace();
        playerIps = new ArrayList<>();
        playerMovement = new PileSpace();
        ids = new ArrayList<>();
    }


    // Run on Caspers Mac or another pc that is purely a server (i.e. no game running on that)
    public void Server () {

        SpaceRepository repository = new SpaceRepository();

        SequentialSpace gameRooms = new SequentialSpace();

        repository.add("gameRooms",gameRooms);

        repository.addGate("tcp://0.0.0.0:9001/?keep");

    }

    public void hostGame(String name) throws IOException, InterruptedException {
        /*
            This part adds the ip that the user wants to host on to the remote space gameRooms
         */
        RemoteSpace gameRooms = new RemoteSpace("tcp://192.168.1.47:9001/gameRooms?keep");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        name = name+" is hosting on ";
        try {
            InetAddress address = InetAddress.getLocalHost();
            System.out.println("IP address: " + address.getHostAddress());
            ip = address.getHostAddress();
        } catch (UnknownHostException ex) {
            System.out.println("Could not find IP address for this host");
            System.out.println("Enter your ip");
            ip = in.readLine();
        }
        gameRooms.put(name, ip, "9001");
        System.out.println("Hosting on " + ip + ":9001");


        chatRepository.add("chat",chat);
        chatRepository.addGate("tcp://"+ip+":9001/?keep");
    }


    public void startGame() throws InterruptedException, IOException {
        List<Object[]> c = chat.queryAll(new ActualField("Hello From"), new FormalField(String.class) , new FormalField(String.class));
        id = String.valueOf(c.size());
        chat.put("Hello From", ip, id);
        chat.put("Start");
        //chatRepository.closeGate("tcp://"+ip+":9001/?keep");'
        List<Object[]> a = chat.queryAll(new ActualField("Hello From"), new FormalField(String.class) , new FormalField(String.class));
        for (Object[] players : a) {
            playerIps.add((String) players[1]);
            ids.add((String) players[2]);
        }
        boolean isHost = true;
       /// chatRepository.add("playerMovement", playerMovement);
       /// chatRepository.addGate("tcp://"+ip+":9002/?keep");
       /// chat.put("SS");
        new GameController(playerIps, ip, isHost, id, ids);


    }

    public void playerMovement(float playerX, float playerY) throws InterruptedException {
        playerMovement.put(ip, playerX,playerY);

    }




}
