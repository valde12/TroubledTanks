package GameForm;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.util.*;
import java.awt.event.KeyEvent;
import java.util.Timer;

public class GameController {

    private Timer timer;
    private GameForm gameForm;
    private Board board;
    private List<Player> players;
    private HashSet<Integer> keyStates = new HashSet<>();

    public GameController() {
        Player player1 = new Player(TankColor.Red);

        players = new ArrayList<Player>();
        players.add(player1);


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


}
