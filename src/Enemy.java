import java.awt.*;

public class Enemy {
    int x;
    int y;
    int speed;
    int tileSize;
    double angle;


    public Enemy(int x, int y, int speed, int tileSize) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.tileSize = tileSize;
        this.angle = Math.random() * 2 * Math.PI;
    }

    // Method to move the enemy toward a specific target position while avoiding obstacles
    public void moveTowards(int targetX, int targetY, int[][] map, int maxScreenRow, int maxScreenCol) {
        int deltaX = targetX - x;
        int deltaY = targetY - y;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance > 0) {
            double moveX = (deltaX / distance) * speed;
            double moveY = (deltaY / distance) * speed;

            int newX = (int) (x + moveX);
            int newY = (int) (y + moveY);

            // Check if the new position is valid (no collision with obstacles)
            if (!isCollidingWithObstacle(newX / tileSize, newY / tileSize, map)) {
                x += moveX;
                y += moveY;
            }
        }
    }

    private boolean isCollidingWithObstacle(int x, int y, int[][] map) {
        // Ensure the position is within bounds and not an obstacle
        return x < 0 || y < 0 || x >= map[0].length || y >= map.length || map[y][x] == 1;
    }

    public void draw(Graphics g, int tileSize) {
        g.setColor(Color.red);
        g.fillRect(x, y, tileSize, tileSize);
    }
}
