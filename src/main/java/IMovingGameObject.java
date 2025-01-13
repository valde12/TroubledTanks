import java.awt.Graphics;
import java.util.List;

public interface IMovingGameObject {
    void update();
    void draw(Graphics g, int tileSize);
    
    private boolean canTileBePassed(int x, int y, int tileSize, int rows, int cols, List<List<Integer>> mapData) {
        int tileX = x / tileSize;
        int tileY = y / tileSize;

        // Out of bounds check
        if (tileX < 0 || tileX >= cols || tileY < 0 || tileY >= rows) {
            return false;
        }

        return mapData.get(tileY).get(tileX) != 1;
    }

}