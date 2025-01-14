package GameForm;

import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
