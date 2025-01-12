import java.awt.Color;
import java.awt.Graphics;

public class Projectile {
    private float x, y;
    private float deltaX, deltaY;
    private float size;
    private float direction;

    public Projectile(float x, float y, float deltaX, float deltaY, float size) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.size = size;
    }

    public void update() {
        System.out.println("Projectile update");
        x = x + deltaX;
        y = y + deltaY;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval((int) x, (int) y, (int) size, (int) size);
    }
}