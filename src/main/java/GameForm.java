import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GameForm extends JFrame {
    private Timer timer;
    private Tank player;
    private Board board;

    public GameForm() {
        super("Tank Trouble");  // Set the window title

        player = new Tank(TankColor.Red);
        board = new Board(Maps.MAP1);

        setSize(800, 600);  // Set initial size for the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // Timer setup for game loop
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                player.keystateCheck(board.getMapData(), board.getTileSize());
                player.updateProjectiles();
                repaint();  // Redraw the frame
            }
        }, 0, 16);  // Approx. 60 FPS

        // Add key and paint listeners
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                player.keyDown(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.keyUp(e);
            }
        });

        // Custom painting for the tank
        JPanel gamePanel = new JPanel() {
            {
                setDoubleBuffered(true);  // Enable double buffering for smoother rendering
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                board.paintMap(g, getWidth(), getHeight());
                player.paintTank(g, board.getTileSize());
                player.drawProjectiles(g, board.getTileSize());
            }
        };
        add(gamePanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameForm::new);  // Launch the form on the Event Dispatch Thread
    }
}
