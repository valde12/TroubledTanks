package GameForm;

import Client.Client;
import Server.Server;
import org.jspace.*;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameController {

    private Timer timer;
    private GameForm gameForm;
    private Board board;
    private List<Player> players;
    private List<Player> alivePlayers;
    private HashSet<Integer> keyStates = new HashSet<>();
    private RemoteSpace cPlayermovement;
    private SpaceRepository spaceRepo;
    private SequentialSpace playerMovment;
    private Object[] receivedMovement;

    private String playerIP;
    private float playerX;
    private float playerY;
    private HashSet<Integer> playerKeyState;
    private Player targetPlayer;

    public GameController(List<String> ips, String targetIp, boolean isHost, String id, List<String> ids) throws IOException {
        /*Player player1 = new Player(TankColor.Red, 100f, 100f);
        Player player2 = new Player(TankColor.Green, 200f, 200f);
        Player player3 = new Player(TankColor.Blue, 300f, 300f);
        players.add(player1);
        players.add(player2);
        players.add(player3);*/
        players = new ArrayList<>();
        TankColor[] colors = TankColor.values();
        alivePlayers = new ArrayList<>();

        int i =0;

        for(String ip : ips){
            TankColor color = colors[i];
            Player player = new Player(color, ip, (i +1)*100f, (i+1)*100f);
            players.add(player);
            i++;
        }
        for(Player player : players){
            if(player.getIp().equals(targetIp)){
                targetPlayer = player;
            }
        }
        if(isHost){
            spaceRepo = new SpaceRepository();
            playerMovment = new SequentialSpace();
            spaceRepo.add("playerMovement",playerMovment);

            spaceRepo.addGate("tcp://"+ targetIp + ":9002/?keep");
        }
        else{
            String hostIp = null;
         for(Player player : players){
             hostIp = player.getIp();
         }
            cPlayermovement = new RemoteSpace("tcp://"+ hostIp + ":9002/playerMovement?keep");
        }


        spawnAllPlayers();

        Server s = new Server();
        Client c = new Client();
        timer = new Timer();

        List<Tank> Tanks = new ArrayList<>();
        for (Player player : players) {
            Tanks.add(player.getTank());
        }

        board = new Board(Maps.MAP1, Tanks);  // Load the map and create the player tank
        gameForm = new GameForm(board);


        for (Player player : players) {
            player.getTank().setMapData(board.getMapData());
        }
        // Timer setup for game loop
        if(isHost){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                board.update();
                targetPlayer.getTank().keystateCheck(keyStates);
                try {
                    if(!keyStates.isEmpty()) {
                        for (String i : ids) {
                            if(!i.equals(id)) {
                                playerMovment.put(i, targetIp, new ArrayList<>(keyStates));
                                System.out.println("Sending movement: " + targetPlayer.getIp() + keyStates);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for(Player player : players){
                    receivedMovement = playerMovment.getp(new ActualField(id),new FormalField(String.class), new FormalField(List.class));
                    if( receivedMovement != null && receivedMovement[0].equals(id) && !receivedMovement[1].equals(targetIp)){
                        System.out.println("Received movement: " + receivedMovement[0] + " keyState " + receivedMovement[2]);
                        playerIP = (String) receivedMovement[1];
                        List<?> receivedList = (List<?>) receivedMovement[2];
                        playerKeyState = new HashSet<>();
                        for (Object key : receivedList) {
                            if (key instanceof Number) {
                                playerKeyState.add(((Number) key).intValue());
                            }
                        }

                    }
                    if(player.getIp().equals(playerIP)) {
                        player.getTank().keystateCheck(playerKeyState);
                        playerKeyState.clear();
                    }
                }
                for(Player player : players) {
                    for (Projectile projectile : allProjectiles()) {
                        if (player.getTank().isHit(projectile.getX(), projectile.getY())) {
                            if (alivePlayers.contains(player)) {
                                alivePlayers.remove(player);
                            }
                            if (alivePlayers.size() == 1) {
                                alivePlayers.get(0).winRound();
                                spawnAllPlayers();
                            }
                        }
                    }
                }

                gameForm.repaint();  // Redraw the frame
            }
        }, 0, 16);}// Approx. 60 FPS
        if(!isHost){ timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                board.update();
                targetPlayer.getTank().keystateCheck(keyStates);
                try {
                    if(!keyStates.isEmpty()) {
                        for(String a : ids) {
                            if(!a.equals(id)) {
                                cPlayermovement.put(a ,targetIp, new ArrayList<>(keyStates));
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                for(Player player : players){
                    try {
                        receivedMovement = cPlayermovement.getp(new ActualField(id),new FormalField(String.class), new FormalField(List.class) );
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if( receivedMovement != null && receivedMovement[0].equals(id) && !receivedMovement[1].equals(targetIp)){
                        System.out.println("Received movement: " + receivedMovement[0] + " keyState " + receivedMovement[1]);
                        playerIP = (String) receivedMovement[0];
                        List<?> receivedList = (List<?>) receivedMovement[2];
                        playerKeyState = new HashSet<>();
                        for (Object key : receivedList) {
                            if (key instanceof Number) {
                                playerKeyState.add(((Number) key).intValue());
                            }
                        }

                    }
                    if(player.getIp().equals(playerIP)) {
                        player.getTank().keystateCheck(playerKeyState);
                        playerKeyState.clear();
                    }
                }

                for(Player player : players) {
                    for (Projectile projectile : allProjectiles()) {
                        if (player.getTank().isHit(projectile.getX(), projectile.getY())) {
                            if (alivePlayers.contains(player)) {
                                alivePlayers.remove(player);
                            }
                            if (alivePlayers.size() == 1) {
                                alivePlayers.get(0).winRound();
                                spawnAllPlayers();
                            }
                        }
                    }
                }

                gameForm.repaint();  // Redraw the frame
            }
        }, 0, 16);}  // Approx. 60 FPS

        // Add key listener
        gameForm.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keyDown(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keyUp(e);
            }
        });

    }

    public void keyDown(KeyEvent e) {
        keyStates.add(e.getKeyCode());
    }

    public void keyUp(KeyEvent e) {
        keyStates.remove(e.getKeyCode());
    }



    public List<Projectile> allProjectiles() {
        List<Projectile> projectiles = new ArrayList<>();
        for (Player player : players) {
            projectiles.addAll(player.getTank().getProjectiles());
        }
        return projectiles;
    }

    public void spawnAllPlayers() {
        for (Player player : players) {
            player.getTank().respawn();
            if (!alivePlayers.contains(player)) {
                alivePlayers.add(player);
            }
        }
    }

}
