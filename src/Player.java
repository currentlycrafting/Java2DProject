import java.awt.*;

public class Player {
    // Player properties
    private int x;
    private int y;
    private int speed;
    private final int tileSize;

    public Player(int startX, int startY, int speed, int tileSize) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.tileSize = tileSize;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(int xSpeed, int ySpeed, int[][] map) {
        // Calculate next position
        int nextX = x + xSpeed;
        int nextY = y + ySpeed;

        // Collision detection with obstacles
        if (!isCollidingWithObstacle(nextX / tileSize, nextY / tileSize, map) &&
                !isCollidingWithObstacle((nextX + tileSize - 1) / tileSize, nextY / tileSize, map) &&
                !isCollidingWithObstacle(nextX / tileSize, (nextY + tileSize - 1) / tileSize, map) &&
                !isCollidingWithObstacle((nextX + tileSize - 1) / tileSize, (nextY + tileSize - 1) / tileSize, map)) {
            x = nextX;
            y = nextY;
        }
    }

    private boolean isCollidingWithObstacle(int x, int y, int[][] map) {
        return x >= 0 && y >= 0 && x < map[0].length && y < map.length && map[y][x] == 1;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(x, y, tileSize, tileSize);
    }
}
