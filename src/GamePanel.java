import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


/**
 * GamePanel is a custom JPanel that handles the game logic and rendering.
 * It supports player movement, enemy behavior, and a simple obstacle-based map.
 */
public class GamePanel extends JPanel implements Runnable {

    // Constants for screen size and tile properties
    final int originalTileSize = 16; // Base tile size in pixels
    final int scale = 3; // Scale factor for tiles
    final int tileSize = originalTileSize * scale; // Scaled tile size
    final int maxScreenCol = 16; // Number of tiles horizontally
    final int maxScreenRow = 12; // Number of tiles vertically
    final int screenWidth = tileSize * maxScreenCol; // Total screen width in pixels
    final int screenHeight = tileSize * maxScreenRow; // Total screen height in pixels

    private long startTime; // Start time of the game
    private long elapsedTime; // Elapsed time in milliseconds
    private String formattedTime = "0:00"; // Formatted time to display
    private String longestTime = "Longest Time: 0:00"; // For game over display

    // Game properties
    int FPS = 60; // Frames per second for the game loop
    KeyHandler keyH = new KeyHandler(); // Handles keyboard input
    Thread gameThread; // Thread to run the game loop
    int playerX = 100; // Player's X-coordinate
    int playerY = 100; // Player's Y-coordinate
    int playerSpeed = 4; // Speed of the player in pixels per frame

    // Map and enemy definitions
    int[][] map = new int[maxScreenRow][maxScreenCol]; // 2D map for obstacles
    ArrayList<Enemy> enemies = new ArrayList<>(); // List of enemy entities
    long lastEnemySpawnTime = 0; // Timestamp for the last enemy spawn
    final long spawnInterval = 10000; // Interval between enemy spawns in milliseconds

    // Game state
    boolean gameOver = false; // Flag to indicate if the game has ended

    /**
     * Constructor initializes the game panel and sets up the map.
     */
    public GamePanel() {
        // Set panel properties
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.white);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        // Initialize the map with obstacles along the edges
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                if (row == 0 || row == maxScreenRow - 1 || col == 0 || col == maxScreenCol - 1) {
                    map[row][col] = 1; // Mark edges as obstacles
                } else {
                    map[row][col] = 0; // Open space
                }
            }
        }

        // Spawn the initial enemy
        spawnNewEnemy();
    }

    /**
     * Starts the game loop in a separate thread.
     */
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
        startTime = System.currentTimeMillis();
    }

    /**
     * Main game loop that handles updates and rendering.
     */
    @Override
    public void run() {
        double drawInterval = (double) 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }
            if (timer >= 1000000000) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    /**
     * Updates the game state, including player movement and enemy behavior.
     */

    public void update() {
        if (gameOver) {
            long currentSeconds = elapsedTime;
            long longestSeconds = parseTimeToSeconds(longestTime);

            if (currentSeconds > longestSeconds) {
                longestTime = "Longest Time: " + formattedTime;
            }
            return;
        }

        // Update timer
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // Convert to seconds

        // Format elapsed time
        long minutes = elapsedTime / 60;
        long seconds = elapsedTime % 60;
        formattedTime = String.format("%d:%02d", minutes, seconds);

        // Handle player movement based on keyboard input
        int xSpeed = 0;
        int ySpeed = 0;

        if (keyH.upPressed) {
            ySpeed -= playerSpeed;
        }
        if (keyH.downPressed) {
            ySpeed += playerSpeed;
        }
        if (keyH.leftPressed) {
            xSpeed -= playerSpeed;
        }
        if (keyH.rightPressed) {
            xSpeed += playerSpeed;
        }

        // Handle diagonal movement using Pythagorean Theory
        if (xSpeed != 0 && ySpeed != 0) {
            double scale = playerSpeed / Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed);
            xSpeed *= scale;
            ySpeed *= scale;
        }

        // Calculate next position
        int nextX = playerX + xSpeed;
        int nextY = playerY + ySpeed;

        // Collision detection
        if (!isCollidingWithObstacle(nextX / tileSize, nextY / tileSize) &&
                !isCollidingWithObstacle((nextX + tileSize - 1) / tileSize, nextY / tileSize) &&
                !isCollidingWithObstacle(nextX / tileSize, (nextY + tileSize - 1) / tileSize) &&
                !isCollidingWithObstacle((nextX + tileSize - 1) / tileSize, (nextY + tileSize - 1) / tileSize)) {
            playerX = nextX;
            playerY = nextY;
        }

        // Enemy movement
        double slowDownFactor = 0.8;
        double circleRadius = 100; // Circle Radius for Circling Mechanic

        for (Enemy enemy : enemies) {
            int enemyX = enemy.x;
            int enemyY = enemy.y;

            // Randomize angle slightly for varied movement
            enemy.angle += Math.random() * Math.PI / 8 - Math.PI / 16; // Slightly randomized angle increment

            // Calculate circular target position relative to player
            int targetX = (int) (playerX + circleRadius * Math.cos(enemy.angle));
            int targetY = (int) (playerY + circleRadius * Math.sin(enemy.angle));

            // Calculate direction to target
            int deltaX = targetX - enemyX;
            int deltaY = targetY - enemyY;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (distance > 0) {
                // Normalize movement and apply slowdown factor
                double moveX = (deltaX / distance) * enemy.speed * slowDownFactor;
                double moveY = (deltaY / distance) * enemy.speed * slowDownFactor;

                int newEnemyX = (int) (enemy.x + moveX);
                int newEnemyY = (int) (enemy.y + moveY);

                // Handle collision with other enemies
                for (Enemy otherEnemy : enemies) {
                    if (enemy != otherEnemy) {
                        double dx = newEnemyX - otherEnemy.x;
                        double dy = newEnemyY - otherEnemy.y;
                        double distanceToOther = Math.sqrt(dx * dx + dy * dy);

                        if (distanceToOther < tileSize) {
                            double moveAwayX = (enemy.x - otherEnemy.x) * 0.5;
                            double moveAwayY = (enemy.y - otherEnemy.y) * 0.5;
                            newEnemyX += moveAwayX;
                            newEnemyY += moveAwayY;
                        }
                    }
                }

                // Check if new position is valid
                if (!isCollidingWithObstacle(newEnemyX / tileSize, newEnemyY / tileSize)) {
                    enemy.x = newEnemyX;
                    enemy.y = newEnemyY;
                }
            }

            // Check collision with player
            if (isCollidingWithEntity(playerX, playerY, enemy.x, enemy.y)) {
                gameOver = true;
                return;
            }
        }

        // Check if 10 seconds have passed to spawn a new enemy
        if (System.currentTimeMillis() - lastEnemySpawnTime >= spawnInterval) {
            spawnNewEnemy();
            lastEnemySpawnTime = System.currentTimeMillis();
        }

    }

    // Helper method to parse time (e.g., "2:45") to seconds
    private long parseTimeToSeconds(String time) {
        String[] parts = time.replace("Longest Time: ", "").split(":");
        long minutes = Long.parseLong(parts[0]);
        long seconds = Long.parseLong(parts[1]);
        return minutes * 60 + seconds;
    }

    private void spawnNewEnemy() {
        int newX;
        int newY;
        int safeDistance = 3; // Minimum distance from the player to spawn an enemy (in tiles)

        do {
            // Generate random coordinates for the enemy
            newX = (int) (Math.random() * (maxScreenCol - 2) + 1) * tileSize;
            newY = (int) (Math.random() * (maxScreenRow - 2) + 1) * tileSize;

            // Check if the enemy is far enough from the player
        } while (map[newY / tileSize][newX / tileSize] == 1 ||
                isOccupiedByEnemy(newX, newY) ||
                Math.abs(playerX - newX) < safeDistance * tileSize ||
                Math.abs(playerY - newY) < safeDistance * tileSize);

        // Create and add the new enemy to the list
        Enemy newEnemy = new Enemy(newX, newY, 2, tileSize);
        enemies.add(newEnemy);
    }


    private boolean isOccupiedByEnemy(int x, int y) {
        for (Enemy enemy : enemies) {
            if (enemy.x == x && enemy.y == y) {
                return true;
            }
        }
        return false;
    }

    private boolean isCollidingWithObstacle(int x, int y) {
        return x >= 0 && x < maxScreenCol && y >= 0 && y < maxScreenRow && map[y][x] == 1;
    }

    private boolean isCollidingWithEntity(int x1, int y1, int x2, int y2) {
        return x1 < x2 + tileSize && x1 + tileSize > x2 &&
                y1 < y2 + tileSize && y1 + tileSize > y2;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameOver) {
            // Display the "Game Over" message and the longest time only when the game is over
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("GAME OVER", screenWidth / 2 - 100, screenHeight / 2);

            // Display the longest time next to the game-over message
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString(longestTime, screenWidth / 2 - 100, screenHeight / 2 + 40);
        } else {
            // Draw the timer when the game is not over
            g.setColor(Color.darkGray);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Time: " + formattedTime, screenWidth / 2 - 106, screenHeight / 2);

            // Draw the player and enemies, and fill the map as usual
            g2.setColor(Color.black);
            g2.fillRect(playerX, playerY, tileSize, tileSize);

            for (Enemy enemy : enemies) {
                enemy.draw(g2, tileSize);
            }

            for (int row = 0; row < maxScreenRow; row++) {
                for (int col = 0; col < maxScreenCol; col++) {
                    if (map[row][col] == 1) {
                        g2.setColor(Color.gray);
                        g2.fillRect(col * tileSize, row * tileSize, tileSize, tileSize);
                    }
                }
            }
        }

        g2.dispose();
    }
}

