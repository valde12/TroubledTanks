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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private float playerDeltaX;
    private float playerDeltaY;
    private boolean hasShot;
    private HashSet<Integer> playerKeyState;
    private Player targetPlayer;
    private float playerAngle;
    private ExecutorService networkThreadPool;


    private static final String MOVEMENT_PORT = "9002";
    private static final String TCP_PREFIX = "tcp://";

    public GameController(List<String> ips, String targetIp, boolean isHost, String id, List<String> ids) throws IOException {
        players = new ArrayList<>();
        TankColor[] colors = TankColor.values();
        alivePlayers = new ArrayList<>();
        networkThreadPool = Executors.newFixedThreadPool(2);


        int i =0;

        // initialize players
        for(String ip : ips){
            TankColor color = colors[i];
            Player player = new Player(color, ip, (i +1)*100f, (i+1)*100f);
            players.add(player);
            i++;
        }
        // find target player
        for(Player player : players){
            if(player.getIp().equals(targetIp)){
                targetPlayer = player;
            }
        }
        // Set up networking and space repository if host
        if(isHost){
            spaceRepo = new SpaceRepository();
            playerMovement = new PileSpace();
            spaceRepo.add("playerMovement", playerMovement);
            spaceRepo.addGate(TCP_PREFIX + targetIp + ":" + MOVEMENT_PORT + "/?keep");
        } else {
            String hostIp = null;
            for(Player player : players){
                hostIp = player.getIp();
            }
            cPlayermovement = new RemoteSpace(TCP_PREFIX + hostIp + ":" + MOVEMENT_PORT + "/playerMovement?keep");
        }

        // Spawn all players on map and initialize tank
        spawnAllPlayers();
        List<Tank> Tanks = new ArrayList<>();
        for (Player player : players) {
            Tanks.add(player.getTank());
        }

        // initialize board, gameform and set map data for all tanks
        board = new Board(Maps.MAP1, Tanks);
        gameForm = new GameForm(board, players);
        for (Player player : players) {
            player.getTank().setMapData(board.getMapData());
        }

        // Timer setup for game loop
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                board.update();
                targetPlayer.getTank().keystateCheck(keyStates);
                gameForm.repaint();
            }
        }, 0, 16);// Approx. 60 FPS

        Space movementSpace = isHost ? playerMovement : cPlayermovement;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                processKeyStatesAsync(movementSpace, id, targetIp, ids);
                for(Player player : players){
                    retrieveMovementAsync(movementSpace, id, player, targetIp);
                }
                CheckDeaths();
            }
        }, 0, 32);// Approx. 60 FPS

        // Add key listener for handling game inputs
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



    private void processKeyStatesAsync(Space playerMovementSpace, String id, String targetIp, List<String> ids) {
        networkThreadPool.submit(() -> {
            try {
                float playerX = targetPlayer.getTank().getPlayerX();
                float playerY = targetPlayer.getTank().getPlayerY();
                float playerDeltaX = targetPlayer.getTank().getPlayerDeltaX();
                float playerDeltaY = targetPlayer.getTank().getPlayerDeltaY();
                float playerAngle = targetPlayer.getTank().getPlayerAngle();
                boolean hasShot = targetPlayer.getTank().getHasShot();
                targetPlayer.getTank().setHasShot(false);

                for (String i : ids) {
                    if (!i.equals(id)) {
                        playerMovementSpace.put(i, targetIp, playerX, playerY, playerDeltaX, playerDeltaY, playerAngle, hasShot);
                    }
                }
            } catch (InterruptedException e) {
                handleException(e);
            }
        });
    }



    private void retrieveMovementAsync(Space playerMovementSpace, String id, Player player, String targetIp) {
        networkThreadPool.submit(() -> {
            try {
                Object[] receivedMovement = playerMovementSpace.queryp(new ActualField(id), new FormalField(String.class),
                        new FormalField(Float.class), new FormalField(Float.class), new FormalField(Float.class),
                        new FormalField(Float.class), new FormalField(Float.class), new FormalField(Boolean.class));
                // Apply player movement if received
                if( receivedMovement != null && receivedMovement[0].equals(id) && !receivedMovement[1].equals(targetIp)){
                    playerIP = (String) receivedMovement[1];
                    playerX = (float) receivedMovement[2];
                    playerY = (float) receivedMovement[3];
                    playerDeltaX = (float) receivedMovement[4];
                    playerDeltaY = (float) receivedMovement[5];
                    playerAngle = (float) receivedMovement[6];
                    hasShot = (boolean) receivedMovement[7];

                }
                applyPlayerMovements(player, playerX, playerY, playerDeltaX, playerDeltaY, playerAngle, hasShot);
            } catch (InterruptedException e) {
                handleException(e);
            }
        });

    }


    private void applyPlayerMovements(Player player, float playerX, float playerY, float playerDeltaX, float playerDeltaY, float playerAngle, boolean hasShot) {
        if (player.getIp().equals(playerIP)) {
            Tank playerTank = player.getTank();

            playerTank.setPlayerX(playerX);
            playerTank.setPlayerY(playerY);
            playerTank.setPlayerDeltaX(playerDeltaX);
            playerTank.setPlayerDeltaY(playerDeltaY);
            playerTank.setPlayerAngle(playerAngle);
            if (hasShot) {
                playerTank.shoot();
            }
        }
    }

    private void CheckDeaths() {
        for(Player player : players) {
            for (Projectile projectile : allProjectiles()) {
                if (player.getTank().isHit(projectile.getX(), projectile.getY())) {
                    alivePlayers.remove(player);
                    if (alivePlayers.size() == 1) {
                        Player winner = alivePlayers.get(0);
                        winner.winRound();

                        gameForm.updateScore(winner.getIp(), winner.getScore());

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
