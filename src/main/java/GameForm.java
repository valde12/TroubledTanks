import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GameForm extends JFrame {
    public GameForm(Board board) {
        super("Tank Trouble");  // Set the window title

        setSize(800, 600);  // Set initial size for the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        JPanel gamePanel = new JPanel() {
            {
                setDoubleBuffered(true);  // Enable double buffering for smoother rendering
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                board.draw(g, getWidth(), getHeight());
            }
        };
        add(gamePanel);
    }

}
