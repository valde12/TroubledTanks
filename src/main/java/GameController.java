import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

public class GameController {

    private Timer timer;
    private GameForm gameForm;
    private Board board;
    private List<Player> players;
    private List<Player> alivePlayers;
    private HashSet<Integer> keyStates = new HashSet<>();


    public GameController() {
        Player player1 = new Player(TankColor.Red, 100f, 100f);
        Player player2 = new Player(TankColor.Green, 200f, 200f);
        Player player3 = new Player(TankColor.Blue, 300f, 300f);
        players = new ArrayList<Player>();
        alivePlayers = new ArrayList<Player>();
        players.add(player1);
        players.add(player2);
        players.add(player3);
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
                for (Player player : players) {
                    player.getTank().keystateCheck(keyStates);
                    checkProjectileCollision(player);
                }

                gameForm.repaint();  // Redraw the frame
            }

            
        }, 0, 16);  // Approx. 60 FPS

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameController::new);  // Launch the form on the Event Dispatch Thread
    }
    
    private void checkProjectileCollision(Player player) {
        for (Projectile projectile : allProjectiles()) {
            if(player.getTank().isHit(projectile.getX(), projectile.getY())) {
                alivePlayers.remove(player);
                System.out.println(alivePlayers.size());
                if (alivePlayers.size() <= 1) {
                    alivePlayers.get(0).winRound();
                    spawnAllPlayers();
                }
            }
        }
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
            alivePlayers.add(player);
        }
    }

}
