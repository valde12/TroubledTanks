import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import Client.Client;

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
        add(startGame);
        setLayout(new BorderLayout());
        add(startPanel, BorderLayout.CENTER);


        JPanel txtPanel = new JPanel();
        JTextField txtField = new JTextField(40);
        txtField.setFont(txtField.getFont().deriveFont(10f));
        txtPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        add(txtPanel, BorderLayout.SOUTH);
        hostGame.addActionListener(e -> {
            Server.Server s = new Server.Server();
            try {
                s.hostGame();
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
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                dispose();
            });





            //JOptionPane.showMessageDialog(StartScreen.this ,"host pressed");
        });


        joinGame.addActionListener(e -> {
            startPanel.remove(hostGame);
            startPanel.remove(joinGame);
            startPanel.add(startGame);
            txtPanel.add(txtField);
            startPanel.revalidate();
            startPanel.repaint();
            Client c = new Client();
            try {c.client();} catch (IOException | InterruptedException ex) {throw new RuntimeException(ex);}

            //JOptionPane.showMessageDialog(StartScreen.this ,"Join presed");
        });








        setVisible(true);
    }






}