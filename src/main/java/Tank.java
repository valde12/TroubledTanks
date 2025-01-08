import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.HashSet;

public class Tank {
    private Color bodyColor;
    private Color barrelColor;
    private float playerX = 100f;
    private float playerY = 100f;
    private float playerAngle = 0f;
    private float playerDeltaX = 1f;
    private float playerDeltaY = 0f;
    private int playerSpeed = 15;

    private HashSet<Integer> keyStates = new HashSet<>();  // Track key presses

    public Tank(TankColor color) {
        switch (color) {
            case Red: // Red
                bodyColor = new Color(139, 0, 0);  // Dark Red
                barrelColor = Color.RED;
                break;
            case Green: // Green
                bodyColor = new Color(0, 100, 0);  // Dark Green
                barrelColor = Color.GREEN;
                break;
            case Blue: // Blue
                bodyColor = new Color(0, 0, 139);  // Dark Blue
                barrelColor = Color.BLUE;
                break;
            case Yellow: // Yellow
                bodyColor = new Color(184, 134, 11);  // Dark Goldenrod
                barrelColor = new Color(218, 165, 32);  // Goldenrod
                break;
            case Black: // Black
                bodyColor = Color.BLACK;
                barrelColor = new Color(169, 169, 169);  // Dark Gray
                break;
            default:
                bodyColor = Color.GRAY;
                barrelColor = Color.DARK_GRAY;
                break;
        }
    }

    public void paintTank(Graphics g, int panelWidth, int panelHeight) {
        // Get the smallest of the two dimensions (width and height)
        int minDimension = Math.min(panelWidth, panelHeight);

        // Scale all sizes based on the minDimension
        int bodyWidth = minDimension / 45;
        int bodyHeight = minDimension / 35;
        int barrelWidth = minDimension / 100;
        int barrelLength = minDimension / 55;
        playerSpeed = minDimension / 170;

        Point[] bodyPoints = {
                new Point((int) (playerX + playerDeltaY * (bodyWidth / 2) - playerDeltaX * bodyHeight / 2),
                        (int) (playerY - playerDeltaX * (bodyWidth / 2) - playerDeltaY * bodyHeight / 2)),
                new Point((int) (playerX - playerDeltaY * (bodyWidth / 2) - playerDeltaX * bodyHeight / 2),
                        (int) (playerY + playerDeltaX * (bodyWidth / 2) - playerDeltaY * bodyHeight / 2)),
                new Point((int) (playerX - playerDeltaY * (bodyWidth / 2) + playerDeltaX * bodyHeight / 2),
                        (int) (playerY + playerDeltaX * (bodyWidth / 2) + playerDeltaY * bodyHeight / 2)),
                new Point((int) (playerX + playerDeltaY * (bodyWidth / 2) + playerDeltaX * bodyHeight / 2),
                        (int) (playerY - playerDeltaX * (bodyWidth / 2) + playerDeltaY * bodyHeight / 2))
        };
        g.setColor(bodyColor);
        g.fillPolygon(new int[] { bodyPoints[0].x, bodyPoints[1].x, bodyPoints[2].x, bodyPoints[3].x },
                new int[] { bodyPoints[0].y, bodyPoints[1].y, bodyPoints[2].y, bodyPoints[3].y }, 4);

        Point[] barrelPoints = {
                new Point((int) (playerX + playerDeltaY * (barrelWidth / 2)), (int) (playerY - playerDeltaX * (barrelWidth / 2))),
                new Point((int) (playerX - playerDeltaY * (barrelWidth / 2)), (int) (playerY + playerDeltaX * (barrelWidth / 2))),
                new Point((int) (playerX + playerDeltaX * (bodyHeight / 2 + barrelLength) - playerDeltaY * (barrelWidth / 2)),
                        (int) (playerY + playerDeltaY * (bodyHeight / 2 + barrelLength) + playerDeltaX * (barrelWidth / 2))),
                new Point((int) (playerX + playerDeltaX * (bodyHeight / 2 + barrelLength) + playerDeltaY * (barrelWidth / 2)),
                        (int) (playerY + playerDeltaY * (bodyHeight / 2 + barrelLength) - playerDeltaX * (barrelWidth / 2)))
        };
        g.setColor(barrelColor);
        g.fillPolygon(new int[] { barrelPoints[0].x, barrelPoints[1].x, barrelPoints[2].x, barrelPoints[3].x },
                new int[] { barrelPoints[0].y, barrelPoints[1].y, barrelPoints[2].y, barrelPoints[3].y }, 4);
    }

    public void checkKeys() {
        if (keyStates.contains(KeyEvent.VK_W)) movePlayer(playerSpeed);
        if (keyStates.contains(KeyEvent.VK_S)) movePlayer(-playerSpeed);
        if (keyStates.contains(KeyEvent.VK_A)) turnPlayer(5);
        if (keyStates.contains(KeyEvent.VK_D)) turnPlayer(-5);
    }

    private void movePlayer(float direction) {
        playerX += playerDeltaX * direction;
        playerY += playerDeltaY * direction;
    }

    private void turnPlayer(float angleChange) {
        playerAngle += angleChange;
        playerAngle = fixAngle(playerAngle);
        playerDeltaX = (float) Math.cos(Math.toRadians(playerAngle));
        playerDeltaY = (float) -Math.sin(Math.toRadians(playerAngle));
    }

    public void keyDown(KeyEvent e) {
        keyStates.add(e.getKeyCode());
    }

    public void keyUp(KeyEvent e) {
        keyStates.remove(e.getKeyCode());
    }

    private float fixAngle(float angle) {
        if (angle > 359f) return angle - 360f;
        if (angle < 0f) return angle + 360f;
        return angle;
    }
}