package GameForm;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;



public class GameForm extends JFrame {
    JPanel scorePanel;
    Map<String, JLabel> playerScores;

    public GameForm(Board board, List<Player> players) {
        super("Tank Trouble");  // Set the window title

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        scorePanel = new JPanel();
        scorePanel.setLayout(new GridLayout(1, players.size()));

        playerScores = new HashMap<>();
        for (Player player : players) {
            JLabel scoreLabel = new JLabel(player.getIp() + ": 0");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
            scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
            playerScores.put(player.getIp(), scoreLabel);
            scorePanel.add(scoreLabel);
        }

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
        setLayout(new BorderLayout());
        add(scorePanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
    }

    public void updateScore(String playerIp, int score) {
        JLabel scoreLabel = playerScores.get(playerIp);
        if (scoreLabel != null) {
            scoreLabel.setText(playerIp + ": " + score);
        }
    }
}


