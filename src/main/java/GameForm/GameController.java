package GameForm;

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
    private SequentialSpace playerMovement;
    private Object[] receivedMovement;

    private String playerIP;
    private float playerX;
    private float playerY;
    private HashSet<Integer> playerKeyState;
    private Player targetPlayer;

    private static final String MOVEMENT_PORT = "9002";
    private static final String TCP_PREFIX = "tcp://";

    public GameController(List<String> ips, String targetIp, boolean isHost, String id, List<String> ids) throws IOException {
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
            playerMovement = new SequentialSpace();
            spaceRepo.add("playerMovement", playerMovement);
            spaceRepo.addGate(TCP_PREFIX + targetIp + ":" + MOVEMENT_PORT + "/?keep");
        } else {
            String hostIp = null;
             for(Player player : players){
                 hostIp = player.getIp();
             }
            cPlayermovement = new RemoteSpace(TCP_PREFIX + hostIp + ":" + MOVEMENT_PORT + "/playerMovement?keep");
        }

        spawnAllPlayers();

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

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                board.update();
                targetPlayer.getTank().keystateCheck(keyStates);
                processKeyStates(isHost ? playerMovement : cPlayermovement, id, targetIp, ids);
                for(Player player : players){
                    receivedMovement = retrieveMovement(isHost ? playerMovement : cPlayermovement, id);

                    if( receivedMovement != null && receivedMovement[0].equals(id) && !receivedMovement[1].equals(targetIp)){
                        System.out.println("Received movement: " + receivedMovement[0] + " Player X = " + receivedMovement[2] + "Player Y = " + receivedMovement[3]);
                        playerIP = (String) receivedMovement[1];
                        playerX = (float) receivedMovement[2];
                        playerY = (float) receivedMovement[3];
                        /*List<?> receivedList = (List<?>) receivedMovement[2];
                        playerKeyState = new HashSet<>();
                        for (Object key : receivedList) {
                            if (key instanceof Number) {
                                playerKeyState.add(((Number) key).intValue());
                            }
                        }*/

                    }
                    applyPlayerMovements(player, playerX, playerY);
                }
                CheckDeaths();
                gameForm.repaint();  // Redraw the frame
            }
        }, 0, 16);// Approx. 60 FPS

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


    // TODO: Send playerX and playerY
    private void processKeyStates(Space playerMovementSpace, String id, String targetIp, List<String> ids) {
        try {
            if (!keyStates.isEmpty()) {
                float playerX = targetPlayer.getTank().getPlayerX();
                float playerY = targetPlayer.getTank().getPlayerY();
                for (String i : ids) {
                    if (!i.equals(id)) {
                        //List<Integer> keys = new ArrayList<>(keyStates);
                        playerMovementSpace.put(i, targetIp, playerX, playerY);
                        System.out.println("Sending movement: " + targetPlayer.getIp());
                    }
                }
            }
        } catch (InterruptedException e) {
            handleException(e);
        }
    }


    // TODO: Receive playerX and playerY
    private Object[] retrieveMovement(Space playerMovementSpace, String id) {
        try {
            return playerMovementSpace.getp(new ActualField(id), new FormalField(String.class), new FormalField(Float.class), new FormalField(Float.class) );
        } catch (InterruptedException e) {
            handleException(e);
            return null;
        }
    }


    // TODO: set to playerX and playerY
    private void applyPlayerMovements(Player player, float playerX, float playerY) {
        if (player.getIp().equals(playerIP)) {
            /*player.getTank().keystateCheck(playerKeyState);
            playerKeyState.clear();*/
            player.getTank().setPlayerX(playerX);
            player.getTank().setPlayerY(playerY);
        }
    }

    private void CheckDeaths() {
        for(Player player : players) {
            for (Projectile projectile : allProjectiles()) {
                if (player.getTank().isHit(projectile.getX(), projectile.getY())) {
                    alivePlayers.remove(player);
                    if (alivePlayers.size() == 1) {
                        alivePlayers.get(0).winRound();
                        spawnAllPlayers();
                    }
                }
            }
        }
    }

    private void handleException(Exception e) {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();  // Log the stack trace
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
