import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Projectile {
    private float x, y;
    private float deltaX, deltaY;
    private float rows, cols;
    private int currentTileSize = 0;
    private long startTime = 0;
    public boolean shouldBeDestroyed = false;

    List<List<Integer>> mapData = new ArrayList<>();

    public Projectile(float x, float y, float deltaX, float deltaY, List<List<Integer>> mapData) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.mapData = mapData;
        this.rows = mapData.size();
        this.cols = rows > 0 ? mapData.get(0).size() : 0;
        this.startTime = System.currentTimeMillis();
    }

    public void update() {
        float nextMoveX = x + deltaX;
        float nextMoveY = y + deltaY;

        boolean canMoveX = canTileBePassed(nextMoveX, y);
        boolean canMoveY = canTileBePassed(x, nextMoveY);

        if (canMoveX && canMoveY) {
            x = nextMoveX;
            y = nextMoveY;
        } else if (canMoveX) {
            x = nextMoveX;
            deltaY = -deltaY;
        } else if (canMoveY) {
            y = nextMoveY;
            deltaX = -deltaX;
        } else {
            deltaX = -deltaX;
            deltaY = -deltaY;
        }
        if (startTime + 7000 < System.currentTimeMillis()) {
            shouldBeDestroyed = true;
        }
    }

    public void draw(Graphics g, int tileSize) {
        g.setColor(Color.BLACK);
        if (currentTileSize == 0) {
            currentTileSize = tileSize;
        }
        if (currentTileSize != tileSize) {
            x = (x / currentTileSize) * tileSize;
            y = (y / currentTileSize) * tileSize; 
            deltaX = (deltaX / currentTileSize) * tileSize;
            deltaY = (deltaY / currentTileSize) * tileSize;
            currentTileSize = tileSize;
        }      
        g.fillOval((int) x, (int) y, (int) tileSize/8, (int) tileSize/8);
    }

    private boolean canTileBePassed(float x, float y) {
        int tileX = (int) (x / currentTileSize);
        int tileY = (int) (y / currentTileSize);
        
        // Out of bounds check
        if (tileX < 0 || tileX >= cols || tileY < 0 || tileY >= rows) {
            return false;
        }

        return mapData.get(tileY).get(tileX) != 1;
    }
}