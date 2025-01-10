import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;

public class Tank {
    private Color bodyColor;
    private Color barrelColor;
    private float playerX = 100f;
    private float playerY = 100f;
    private float playerAngle = 0f;
    private float playerDeltaX = 1f;
    private float playerDeltaY = 0f;
    private int playerSpeed = 15;
    private int bodyWidth, bodyHeight, barrelWidth, barrelLength;
    private int currentTileSize = 0;

    private HashSet<Integer> keyStates = new HashSet<>();  // Track key presses

    public Tank(TankColor color) {
        switch (color) {
            case Red:
                bodyColor = new Color(139, 0, 0);  // Dark Red
                barrelColor = Color.RED;
                break;
            case Green:
                bodyColor = new Color(0, 100, 0);  // Dark Green
                barrelColor = Color.GREEN;
                break;
            case Blue:
                bodyColor = new Color(0, 0, 139);  // Dark Blue
                barrelColor = Color.BLUE;
                break;
            case Yellow:
                bodyColor = new Color(184, 134, 11);  // Dark Goldenrod
                barrelColor = new Color(218, 165, 32);  // Goldenrod
                break;
            case Black:
                bodyColor = Color.BLACK;
                barrelColor = new Color(169, 169, 169);  // Dark Gray
                break;
            default:
                bodyColor = Color.GRAY;
                barrelColor = Color.DARK_GRAY;
                break;
        }
    }

    public void paintTank(Graphics g, int tileSize) {
        if (currentTileSize == 0) {
            currentTileSize = tileSize;
        }
        // Scale all sizes based on the tile size
        bodyWidth = tileSize / 3;
        bodyHeight = tileSize / 2;
        barrelWidth = tileSize / 8;
        barrelLength = tileSize / 4;
        playerSpeed = tileSize / 12;
        playerX = (playerX / currentTileSize) * tileSize;
        playerY = (playerY / currentTileSize) * tileSize;
        currentTileSize = tileSize;


        Point[] bodyPoints = calculateBodyPoints(playerX, playerY, playerDeltaX, playerDeltaY);

        g.setColor(bodyColor);
        g.fillPolygon(new int[] { bodyPoints[0].x, bodyPoints[1].x, bodyPoints[2].x, bodyPoints[3].x },
                new int[] { bodyPoints[0].y, bodyPoints[1].y, bodyPoints[2].y, bodyPoints[3].y }, 4);

        // Barrel Points
        Point[] barrelPoints = calculateBarrelPoints(playerX, playerY, playerDeltaX, playerDeltaY);

        g.setColor(barrelColor);
        g.fillPolygon(new int[] { barrelPoints[0].x, barrelPoints[1].x, barrelPoints[2].x, barrelPoints[3].x },
                new int[] { barrelPoints[0].y, barrelPoints[1].y, barrelPoints[2].y, barrelPoints[3].y }, 4);
    }

    public void keystateCheck(List<List<Integer>> mapData, int tileSize) {
        if (keyStates.contains(KeyEvent.VK_W)) movePlayer(playerSpeed, tileSize, mapData);
        if (keyStates.contains(KeyEvent.VK_S)) movePlayer(-playerSpeed, tileSize, mapData);
        if (keyStates.contains(KeyEvent.VK_A)) turnPlayer(5f, tileSize, mapData);
        if (keyStates.contains(KeyEvent.VK_D)) turnPlayer(-5f, tileSize, mapData);
    }

    public void keyDown(KeyEvent e) {
        keyStates.add(e.getKeyCode());
    }

    public void keyUp(KeyEvent e) {
        keyStates.remove(e.getKeyCode());
    }

    private void movePlayer(float direction, int tileSize, List<List<Integer>> mapData) {
        float newX = playerX + playerDeltaX * direction;
        float newY = playerY + playerDeltaY * direction;

        boolean canMoveX = canMoveTo(newX, playerY, playerDeltaX, playerDeltaY, tileSize, mapData);
        boolean canMoveY = canMoveTo(playerX, newY, playerDeltaX, playerDeltaY, tileSize, mapData);

        if (canMoveX && canMoveY) {
            playerX = newX;
            playerY = newY;
        } else if (canMoveX) {
            playerX = newX;
            // Turn slightly away from wall when sliding against it
            turnPlayer(playerDeltaY > 0 ? (playerDeltaX < 0 ? -2f : 2f) : (playerDeltaX < 0 ? 2f : -2f), tileSize, mapData);
        } else if (canMoveY) {
            playerY = newY;
            // Turn slightly away from wall when sliding against it
            turnPlayer(playerDeltaY > 0 ? (playerDeltaX < 0 ? 2f : -2f) : (playerDeltaX < 0 ? -2f : 2f), tileSize, mapData);
        }
    }

    private void turnPlayer(float angleChange, int tileSize, List<List<Integer>> mapData) {
        float newAngle = fixAngle(playerAngle + angleChange);
        float newDeltaX = (float) Math.cos(Math.toRadians(newAngle));
        float newDeltaY = (float) -Math.sin(Math.toRadians(newAngle));

        boolean canRotate = canMoveTo(playerX, playerY, newDeltaX, newDeltaY, tileSize, mapData);

        if (canRotate) {
            playerAngle = newAngle;
            playerDeltaX = newDeltaX;
            playerDeltaY = newDeltaY;
        } else {
            boolean frontBlocked = !canMoveTo(playerX + playerDeltaX * tileSize, playerY + playerDeltaY * tileSize, playerDeltaX, playerDeltaY, tileSize, mapData);
            boolean rearBlocked = !canMoveTo(playerX - playerDeltaX * tileSize, playerY - playerDeltaY * tileSize, -playerDeltaX, -playerDeltaY, tileSize, mapData);

            if (frontBlocked) {
                movePlayer((newDeltaY > 0 ? -newDeltaY : newDeltaY), tileSize, mapData);
            } else if (rearBlocked) {
                movePlayer((newDeltaY > 0 ? newDeltaY : -newDeltaY), tileSize, mapData);
            }
        }
    }

    private boolean canMoveTo(float newX, float newY, float newPlayerDeltaX, float newPlayerDeltaY, int tileSize, List<List<Integer>> mapData) {
        int rows = mapData.size();
        int cols = rows > 0 ? mapData.get(0).size() : 0;

        Point[] newBodyPoints = calculateBodyPoints(newX, newY, newPlayerDeltaX, newPlayerDeltaY);
        Point[] newBarrelPoints = calculateBarrelPoints(newX, newY, newPlayerDeltaX, newPlayerDeltaY);

        // Check body collision
        for (Point point : newBodyPoints) {
            if (!canTileBePassed(point.x, point.y, tileSize, rows, cols, mapData)) {
                return false;
            }
        }

        // Check barrel collision
        if (!canTileBePassed(newBarrelPoints[2].x, newBarrelPoints[2].y, tileSize, rows, cols, mapData) ||
                !canTileBePassed(newBarrelPoints[3].x, newBarrelPoints[3].y, tileSize, rows, cols, mapData)) {
            return false;
        }

        return true;
    }

    private boolean canTileBePassed(int x, int y, int tileSize, int rows, int cols, List<List<Integer>> mapData) {
        int tileX = x / tileSize;
        int tileY = y / tileSize;

        // Out of bounds check
        if (tileX < 0 || tileX >= cols || tileY < 0 || tileY >= rows) {
            return false;
        }

        return mapData.get(tileY).get(tileX) != 1;
    }

    private float fixAngle(float angle) {
        if (angle > 359f) return angle - 360f;
        if (angle < 0f) return angle + 360f;
        return angle;
    }

    private Point[] calculateBodyPoints(float playerX, float playerY, float playerDeltaX, float playerDeltaY) {
        Point[] bodyPoints = new Point[] {
                new Point((int) (playerX + playerDeltaY * (bodyWidth / 2) - playerDeltaX * bodyHeight / 2),
                        (int) (playerY - playerDeltaX * (bodyWidth / 2) - playerDeltaY * bodyHeight / 2)),
                new Point((int) (playerX - playerDeltaY * (bodyWidth / 2) - playerDeltaX * bodyHeight / 2),
                        (int) (playerY + playerDeltaX * (bodyWidth / 2) - playerDeltaY * bodyHeight / 2)),
                new Point((int) (playerX - playerDeltaY * (bodyWidth / 2) + playerDeltaX * bodyHeight / 2),
                        (int) (playerY + playerDeltaX * (bodyWidth / 2) + playerDeltaY * bodyHeight / 2)),
                new Point((int) (playerX + playerDeltaY * (bodyWidth / 2) + playerDeltaX * bodyHeight / 2),
                        (int) (playerY - playerDeltaX * (bodyWidth / 2) + playerDeltaY * bodyHeight / 2))
        };
        return bodyPoints;
    }

    private Point[] calculateBarrelPoints(float playerX, float playerY, float playerDeltaX, float playerDeltaY) {
        Point[] barrelPoints = new Point[] {
                new Point((int) (playerX + playerDeltaY * (barrelWidth / 2)), (int) (playerY - playerDeltaX * (barrelWidth / 2))),
                new Point((int) (playerX - playerDeltaY * (barrelWidth / 2)), (int) (playerY + playerDeltaX * (barrelWidth / 2))),
                new Point((int) (playerX + playerDeltaX * (bodyHeight / 2 + barrelLength) - playerDeltaY * (barrelWidth / 2)),
                        (int) (playerY + playerDeltaY * (bodyHeight / 2 + barrelLength) + playerDeltaX * (barrelWidth / 2))),
                new Point((int) (playerX + playerDeltaX * (bodyHeight / 2 + barrelLength) + playerDeltaY * (barrelWidth / 2)),
                        (int) (playerY + playerDeltaY * (bodyHeight / 2 + barrelLength) - playerDeltaX * (barrelWidth / 2)))
        };
        return barrelPoints;
    }

}