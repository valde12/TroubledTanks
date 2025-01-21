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
    private Tank tank;
    private float playerX;
    private float playerY;
    private float playerDeltaX;
    private float playerDeltaY;
    private boolean hasShot;
    private HashSet<Integer> playerKeyState;
    private Player targetPlayer;
    private float playerAngle;

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
        // Optimalt ville man nok lave et space til hver spiller's movement og så have threads til kun at kigge på en spiller's movement
        // For at undgå alt for stort delay
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

        spawnAllPlayers();

        timer = new Timer();

        List<Tank> Tanks = new ArrayList<>();
        for (Player player : players) {
            Tanks.add(player.getTank());
        }

        board = new Board(Maps.MAP1, Tanks);  // Load the map and create the player tank
        gameForm = new GameForm(board, players);


        for (Player player : players) {
            player.getTank().setMapData(board.getMapData());
        }
        // Timer setup for game loop

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                board.update();
                targetPlayer.getTank().keystateCheck(keyStates);
                /*targetPlayer.getTank().keystateCheck(keyStates);
                processKeyStates(isHost ? playerMovement : cPlayermovement, id, targetIp, ids);
                for(Player player : players){
                    receivedMovement = retrieveMovement(isHost ? playerMovement : cPlayermovement, id);

                    if( receivedMovement != null && receivedMovement[0].equals(id) && !receivedMovement[1].equals(targetIp)){
                        //System.out.println("Received movement: " + receivedMovement[0] + " Player X = " + receivedMovement[2] + "Player Y = " + receivedMovement[3]);
                        playerIP = (String) receivedMovement[1];
                        playerX = (float) receivedMovement[2];
                        playerY = (float) receivedMovement[3];
                        playerDeltaX = (float) receivedMovement[4];
                        playerDeltaY = (float) receivedMovement[5];
                        playerAngle = (float) receivedMovement[6];
                        hasShot = (boolean) receivedMovement[7];
                        /*List<?> receivedList = (List<?>) receivedMovement[2];
                        playerKeyState = new HashSet<>();
                        for (Object key : receivedList) {
                            if (key instanceof Number) {
                                playerKeyState.add(((Number) key).intValue());
                            }
                        }

                    }
                    applyPlayerMovements(player, playerX, playerY, playerDeltaX, playerDeltaY, playerAngle, hasShot);
                }*/
                CheckDeaths();
                gameForm.repaint();  // Redraw the frame
            }
        }, 0, 16);// Approx. 60 FPS

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for(Player player : players){
                    receivedMovement = retrieveMovement(isHost ? playerMovement : cPlayermovement, id);

                    if( receivedMovement != null && receivedMovement[0].equals(id) && !receivedMovement[1].equals(targetIp)){
                        //System.out.println("Received movement: " + receivedMovement[0] + " Player X = " + receivedMovement[2] + "Player Y = " + receivedMovement[3]);
                        playerIP = (String) receivedMovement[1];
                        playerX = (float) receivedMovement[2];
                        playerY = (float) receivedMovement[3];
                        playerDeltaX = (float) receivedMovement[4];
                        playerDeltaY = (float) receivedMovement[5];
                        playerAngle = (float) receivedMovement[6];
                        hasShot = (boolean) receivedMovement[7];
                        tank =(Tank) receivedMovement[8];
                        /*List<?> receivedList = (List<?>) receivedMovement[2];
                        playerKeyState = new HashSet<>();
                        for (Object key : receivedList) {
                            if (key instanceof Number) {
                                playerKeyState.add(((Number) key).intValue());
                            }
                        }*/

                    }
                    applyPlayerMovements(player, playerX, playerY, playerDeltaX, playerDeltaY, playerAngle, hasShot, tank);
                }
            }

        }, 0, 32);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                processKeyStates(isHost ? playerMovement : cPlayermovement, id, targetIp, ids);
            }

        }, 0, 64);
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
            float playerX = targetPlayer.getTank().getPlayerX();
            float playerY = targetPlayer.getTank().getPlayerY();
            float playerDeltaX = targetPlayer.getTank().getPlayerDeltaX();
            float playerDeltaY = targetPlayer.getTank().getPlayerDeltaY();
            float playerAngle = targetPlayer.getTank().getPlayerAngle();
            boolean hasShot = targetPlayer.getTank().getHasShot();
            targetPlayer.getTank().setHasShot(false);
            Tank tank = targetPlayer.getTank();

            for (String i : ids) {
                if (!i.equals(id)) {
                    //List<Integer> keys = new ArrayList<>(keyStates);
                    playerMovementSpace.put(i, targetIp, playerX, playerY, playerDeltaX, playerDeltaY, playerAngle, hasShot, tank);
                    //System.out.println("Sending movement: " + targetPlayer.getIp() + "X = " + playerX + " Y = " + playerY + "ANGLE" + "Delta X = " + playerDeltaX + "Delta Y = " + playerDeltaY);
                }
            }
        } catch (InterruptedException e) {
            handleException(e);
        }
    }


    // TODO: Receive playerX and playerY
    private Object[] retrieveMovement(Space playerMovementSpace, String id) {
        try {
            return playerMovementSpace.queryp(new ActualField(id), new FormalField(String.class), new FormalField(Float.class), new FormalField(Float.class), new FormalField(Float.class), new FormalField(Float.class),new FormalField(Float.class), new FormalField(Boolean.class), new FormalField(Tank.class));
        } catch (InterruptedException e) {
            handleException(e);
            return null;
        }
    }


    // TODO: set to playerX and playerY
    private void applyPlayerMovements(Player player, float playerX, float playerY, float playerDeltaX, float playerDeltaY, float playerAngle, boolean hasShot, Tank tank) {
        if (player.getIp().equals(playerIP)) {
            /*player.getTank().keystateCheck(playerKeyState);
            playerKeyState.clear();*/
            Tank playerTank = player.getTank();
            playerTank = tank;
            /*playerTank.setPlayerX(playerX);
            playerTank.setPlayerY(playerY);
            playerTank.setPlayerDeltaX(playerDeltaX);
            playerTank.setPlayerDeltaY(playerDeltaY);
            playerTank.setPlayerAngle(playerAngle);
            if (hasShot) {
                playerTank.shoot();
            }*/
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
