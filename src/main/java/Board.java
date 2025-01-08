import java.awt.*;
        import java.io.*;
        import java.util.ArrayList;
import java.util.List;

public class Board {

    private final List<List<Integer>> mapData;
    private final int tileSize = 175;

    public Board(Maps map) {
        this.mapData = csvTo2DArray(map.getFilePath());
    }


    private static List<List<Integer>> csvTo2DArray(String filePath) {
        List<List<Integer>> result = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                List<Integer> row = new ArrayList<>();
                for (String value : values) {
                    row.add(Integer.parseInt(value.trim()));
                }
                result.add(row);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format in CSV file: " + e.getMessage(), e);
        }
        return result;
    }

    public void paintMap(Graphics g, int panelWidth, int panelHeight) {
        int rows = mapData.size();
        int cols = rows > 0 ? mapData.get(0).size() : 0; //Amount of columns in map
        if (rows == 0 || cols == 0) {
            return;
        }

        int tileSize = Math.min(panelWidth / cols, panelHeight / rows); // Calculate tile size to fit map

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int tileType = mapData.get(row).get(col);
                paintTile(g, col * tileSize, row * tileSize, tileSize, tileType);
            }
        }
    }

    private void paintTile(Graphics g, int x, int y, int tileSize, int tileType) {
        switch (tileType) {
            case 0 -> g.setColor(Color.LIGHT_GRAY); // Ground
            case 1 -> g.setColor(Color.DARK_GRAY); // Wall
            default -> g.setColor(Color.BLACK); // Unknown tile type
        }
        g.fillRect(x, y, tileSize, tileSize);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, tileSize, tileSize);
    }
}
