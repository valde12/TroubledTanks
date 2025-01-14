import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class StartScreen extends  JFrame{

    public StartScreen(){

        super("GameForm.Tank Trouble");


        setSize(800, 600);  // Set initial size for the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel startPanel = new JPanel();
        JButton hostGame = new JButton("Host Game");
        JButton joinGame = new JButton("Join Game");
        JButton startGame = new JButton("Start Game");
        startPanel.add(hostGame);
        startPanel.add(joinGame);
        startPanel.add(startGame);
        setLayout(new BorderLayout());
        add(startPanel, BorderLayout.CENTER);


        JPanel txtPanel = new JPanel();
        JTextField txtField = new JTextField(40);
        txtField.setFont(txtField.getFont().deriveFont(10f));
        txtPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        add(txtPanel, BorderLayout.SOUTH);


        hostGame.addActionListener(e -> {

        });


        joinGame.addActionListener(e -> {

        });

        startGame.addActionListener(e -> {
            javax.swing.SwingUtilities.invokeLater(GameController::new);
            setVisible(false);
        });

        setVisible(true);
    }
}