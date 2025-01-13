import java.awt.Color;
import java.awt.Graphics;

public class Projectile implements IMovingGameObject {
    private float x, y;
    private float deltaX, deltaY;
    private float size;
    private int currentTileSize = 0;

    public Projectile(float x, float y, float deltaX, float deltaY, float size) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.size = size;
    }

    @Override
    public void update() {
        System.out.println("Projectile update");
        x = x + deltaX;
        y = y + deltaY;
    }

    @Override
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
}