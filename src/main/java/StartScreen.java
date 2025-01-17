import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import Client.Client;
import Server.Server;

public class StartScreen extends JFrame {
    public StartScreen() {
        super("GameForm.Tank Trouble");

        setSize(800, 600);  // Set initial size for the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel startPanel = new JPanel();
        JButton hostGame = new JButton("Host Game");
        JButton joinGame = new JButton("Join Game");
        JButton startGame = new JButton("Start Game");
        JTextField nameField = new JTextField(20);  // Field for entering the player name
        startPanel.add(new JLabel("Enter your name:"));
        startPanel.add(nameField);
        startPanel.add(hostGame);
        startPanel.add(joinGame);
        setLayout(new BorderLayout());
        add(startPanel, BorderLayout.CENTER);

        JPanel txtPanel = new JPanel();
        JTextField txtField = new JTextField(40);
        txtField.setFont(txtField.getFont().deriveFont(10f));
        txtPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        add(txtPanel, BorderLayout.SOUTH);

        hostGame.addActionListener(e -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a name to host the game.");
                return;
            }
            Server s = new Server();
            try {
                s.hostGame(playerName);  // Pass the name to hostGame
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            startPanel.remove(hostGame);
            startPanel.remove(joinGame);
            startPanel.add(startGame);
            txtPanel.add(txtField);
            startPanel.revalidate();
            startPanel.repaint();
            startGame.addActionListener(e1 -> {
                try {
                    s.startGame();
                } catch (InterruptedException | IOException ex) {
                    throw new RuntimeException(ex);
                }
                dispose();
            });
        });

        joinGame.addActionListener(e -> {
            startPanel.remove(hostGame);
            startPanel.remove(joinGame);
            startPanel.add(startGame);
            txtPanel.add(txtField);
            startPanel.revalidate();
            startPanel.repaint();
            Client c = new Client();
            try {
                c.client();
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        setVisible(true);
    }
}