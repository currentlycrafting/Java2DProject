import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TimerTask;


/**
 * GamePanel is a custom JPanel that handles the game logic and rendering.
 * It supports player movement, enemy behavior, and a simple obstacle-based map.
 */
public class GamePanel extends JPanel implements Runnable {

    // Constants for screen size and tile properties
    final int originalTileSize = 12; // Base tile size in pixels
    final int scale = 5; // Scale factor for tiles
    final int tileSize = originalTileSize * scale; // Scaled tile size
    final int maxScreenCol = 20; // Number of tiles horizontally
    final int maxScreenRow = 20; // Number of tiles vertically
    final int screenWidth = tileSize * maxScreenCol; // Total screen width in pixels
    final int screenHeight = tileSize * maxScreenRow; // Total screen height in pixels

    private long startTime; // Start time of the game
    private long elapsedTime; // Elapsed time in milliseconds
    private String formattedTime = "0:00"; // Formatted time to display
    private String longestTime = "Longest Time: 0:00"; // For game over display

    // Create instances of the Sound class
    private ArrayList<Sound> sounds = new ArrayList<>();

    private ArrayList<Particle> particles = new ArrayList<>();


    // Game properties
    int FPS = 60; // Frames per second for the game loop
    KeyHandler keyH = new KeyHandler(); // Handles keyboard input
    Thread gameThread; // Thread to run the game loop
    int playerX = (maxScreenCol * tileSize) / 2 - tileSize / 2;
    int playerY = (maxScreenRow * tileSize) / 2 - tileSize / 2;
    int playerSpeed = 4; // Speed of the player in pixels per frame

    private Camera camera; // New Camera

    // Map and enemy definitions
    int[][] map = new int[maxScreenRow][maxScreenCol]; // 2D map for obstacles
    ArrayList<Enemy> enemies = new ArrayList<>(); // List of enemy entities
    ArrayList<Boss> bosses = new ArrayList<>(); // List of boss entities
    private long bossBattleStartTime;
    long lastEnemySpawnTime = 0; // Timestamp for the last enemy spawn
    long bossSpawnInterval = 30000; // Timestamp for the last enemy spawn
    long spawnInterval = 10000; // Interval between enemy spawns in milliseconds

    // Game state
    boolean gameOver = false; // Flag to indicate if the game has ended

    private JButton retryButton;

    private static int level;

    private static int boss_battle_count = 0;

    private JButton attribute_1_Button;
    private JButton attribute_2_Button;
    private JButton attribute_3_Button;

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

        initializeSounds();
        initializeRetryButton();

        // New Camera Instance
        camera = new Camera(maxScreenCol, maxScreenRow, screenWidth, screenHeight);

    }

    private void initializeRetryButton() {
        retryButton = new JButton("Retry");
        retryButton.setFont(new Font("Arial", Font.BOLD, 20));
        retryButton.setBounds(screenWidth / 2 - 90, screenHeight / 2 + 90, 150, 50);
        retryButton.setFocusPainted(false);
        retryButton.setVisible(false);

        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });

        this.setLayout(null);
        this.add(retryButton);
    }

    /**
     * Initialize sound effects and background music.
     */
    private void initializeSounds() {
        try {
            Sound backgroundMusic = new Sound();
            backgroundMusic.setFile("game_music.wav"); // Relative to src
            sounds.add(backgroundMusic);
            backgroundMusic.loop();

            Sound bossTheme = new Sound();
            bossTheme.setFile("boss_theme.wav"); // Relative to src
            sounds.add(bossTheme);


        } catch (Exception e) {
            System.err.println("Error initializing sounds: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Play a specific sound effect.
     *
     * @param index Index of the sound effect in the sounds list
     */
    public void playSound(int index) {
        if (index >= 0 && index < sounds.size()) {
            sounds.get(index).play();
        } else {
            System.err.println("Invalid sound index: " + index);
        }
    }

    /**
     * Stop a specific sound effect.
     *
     * @param index Index of the sound effect in the sounds list
     */
    public void stopSound(int index) {
        if (index >= 0 && index < sounds.size()) {
            sounds.get(index).stop();
        } else {
            System.err.println("Invalid sound index: " + index);
        }
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
     * Updates the game state, including player movement, enemy behavior, and level logic.
     */
    public void update() {
        if (gameOver) {
            handleGameOver();
            return;
        }

        // Update elapsed time and check for level progression
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // Convert to seconds
        long minutes = elapsedTime / 60;
        long seconds = elapsedTime % 60;
        formattedTime = String.format("%d:%02d", minutes, seconds);

        // Trigger level up every 2 minutes
        if (!bossBattleActive) {
            if (elapsedTime % 120 == 0 && elapsedTime != 0) {
                levelUp();
            }
        }

        // For 5 every level the boss battle begins
        if (!bossBattleActive && level > 0 && level % 5 == 0) {
            playSound(1);
            stopSound(0);
            bossBattle(); // Start boss battle when the level is a valid values

            // Spawns in waves for boss enemies
            spawnNewWaveBoss();
            boss_wave_enemy_count++;

            if (boss_battle_count > 0) {
                for (int i = 0; i < boss_battle_count; i++) {
                    spawnBossEnemy();
                }
            }
            boss_battle_count++;

        }



        // Check if the boss battle is over or if 10 seconds have passed
        if (bossBattleActive) {
            long timeElapsedInBossBattle = (System.currentTimeMillis() - bossBattleStartTime) / 1000;
            if (timeElapsedInBossBattle >= 10) {
                stopSound(1);
                playSound(0);
                level++;
                bossBattleActive = false;
            }
        }


        // Handle player movement
        handlePlayerMovement();

        // Update enemy movement
        handleEnemyMovement();

        // Update boss movement
        handleBossMovement();

        // Update camera position
        camera.update(playerX, playerY);

    }

    /**
     * Handles game over logic and the retry button visibility.
     */
    private void handleGameOver() {
        long currentSeconds = elapsedTime;
        long longestSeconds = parseTimeToSeconds(longestTime);

        if (currentSeconds > longestSeconds) {
            longestTime = "Longest Time: " + formattedTime;
        }

        retryButton.setVisible(true);
    }

    /**
     * Handles player movement based on keyboard input.
     */
    private void handlePlayerMovement() {
        int xSpeed = 0;
        int ySpeed = 0;

        if (keyH.upPressed) ySpeed -= playerSpeed;
        if (keyH.downPressed) ySpeed += playerSpeed;
        if (keyH.leftPressed) xSpeed -= playerSpeed;
        if (keyH.rightPressed) xSpeed += playerSpeed;

        // Normalize diagonal movement
        if (xSpeed != 0 && ySpeed != 0) {
            double scale = playerSpeed / Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed);
            xSpeed *= scale;
            ySpeed *= scale;
        }

        // Calculate next position and check collisions
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
    }

    /**
     * Handles enemy movement and behavior.
     */
    private void handleEnemyMovement() {
        // Enemy movement
        double slowDownFactor = 0.8;
        double circleRadius = 70; // Circle Radius for Circling Mechanic

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

    private void handleBossMovement() {
        // Boss movement logic
        double slowDownFactor = 0.8;

        for (Boss boss : bosses) {
            int bossX = boss.x;
            int bossY = boss.y;

            // Calculate direction to the player
            int deltaX = playerX - bossX;
            int deltaY = playerY - bossY;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (distance > 0) {
                // Normalize movement and apply slowdown factor
                double moveX = (deltaX / distance) * boss.speed * slowDownFactor;
                double moveY = (deltaY / distance) * boss.speed * slowDownFactor;

                int newBossX = (int) (boss.x + moveX);
                int newBossY = (int) (boss.y + moveY);

                // Handle collision with other bosses (optional)
                if (isCollidingWithOtherBoss(boss, newBossX, newBossY)) {
                    double moveAwayX = (boss.x - newBossX) * 0.5;
                    double moveAwayY = (boss.y - newBossY) * 0.5;
                    newBossX += moveAwayX;
                    newBossY += moveAwayY;
                }


                // Handle collision with enemies
                for (Enemy enemy : enemies) {
                    double dx = newBossX - enemy.x;
                    double dy = newBossY - enemy.y;
                    double distanceToEnemy = Math.sqrt(dx * dx + dy * dy);

                    if (distanceToEnemy < tileSize) {
                        double moveAwayX = (boss.x - enemy.x) * 0.5;
                        double moveAwayY = (boss.y - enemy.y) * 0.5;
                        newBossX += moveAwayX;
                        newBossY += moveAwayY;
                    }
                }


                // Check if new position is valid
                if (!isCollidingWithObstacle(newBossX / tileSize, newBossY / tileSize)) {
                    boss.x = newBossX;
                    boss.y = newBossY;
                }
            }

            // Check collision with player
            if (isCollidingWithEntity(playerX, playerY, boss.x, boss.y)) {
                gameOver = true;
                return;
            }
        }
    }

    // Helper method to check for collisions with other bosses
    private boolean isCollidingWithOtherBoss(Boss boss, int newBossX, int newBossY) {
        for (Boss otherBoss : bosses) {
            if (boss != otherBoss) {
                double dx = newBossX - otherBoss.x;
                double dy = newBossY - otherBoss.y;
                double distanceToOther = Math.sqrt(dx * dx + dy * dy);
                if (distanceToOther < tileSize) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method to handle level up: clears enemies, shows animation, and prepares for a new wave.
     */
    public boolean isLevelingUp = false;

    public void levelUp() {
        if (isLevelingUp) return; // Prevent re-entry
        isLevelingUp = true;

        level++;
        clearEnemies();
        clearBosses();

        JLabel levelLabel = new JLabel("Level " + level);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 64)); // Larger font size
        levelLabel.setForeground(Color.RED);
        levelLabel.setHorizontalAlignment(SwingConstants.CENTER);

        int labelWidth = 400;
        int labelHeight = 100;
        int labelX = (screenWidth - labelWidth) / 2;
        int labelY = (screenHeight - labelHeight) / 2;
        levelLabel.setBounds(labelX, labelY - 300, labelWidth, labelHeight);

        this.setLayout(null);
        this.add(levelLabel);
        this.revalidate();
        this.repaint();


        Timer timer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(levelLabel);
                revalidate();
                repaint();
                isLevelingUp = false; // Reset the flag
                spawnNewWave();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    /**
     * Clears all enemies from the game.
     */
    private void clearEnemies() {
        enemies.clear();
    }
    private void clearBosses() {
        bosses.clear();
    }

    /**
     * Spawns a new wave of enemies based on the current level.
     */
    private void spawnNewWave() {
        int enemyCount = level * 2; // Increase enemy count with each level
        for (int i = 0; i < enemyCount; i++) {
            spawnNewEnemy();
        }
    }

    /**
     * Spawns a new wave of enemies based on the current level.
     */
    private static int boss_wave_enemy_count = 1;
    private void spawnNewWaveBoss() {
        int enemyCount = boss_wave_enemy_count * 3; // Increase enemy count with each level
        for (int i = 0; i < enemyCount; i++) {
            spawnNewEnemy();
        }
    }

    // Helper method to parse time (e.g., "2:45") to seconds
    private long parseTimeToSeconds(String time) {
        String[] parts = time.replace("Longest Time: ", "").split(":");
        long minutes = Long.parseLong(parts[0]);
        long seconds = Long.parseLong(parts[1]);
        return minutes * 60 + seconds;
    }

    private void restartGame() {
        gameOver = false;
        bossBattleActive = false;
        level = 0;
        boss_battle_count = 0;
        retryButton.setVisible(false);
        playerX = (maxScreenCol * tileSize) / 2 - tileSize / 2;
        playerY = (maxScreenRow * tileSize) / 2 - tileSize / 2;
        enemies.clear();
        bosses.clear();
        stopSound(1);
        playSound(0);

        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        formattedTime = "0:00";
        level = 0;

        if (longestTime == null || longestTime.isEmpty()) {
            longestTime = "0:00";
        }

        System.out.println("Game restarted.");
    }

    /**
     * Initiates a boss battle when the player reaches a level that is divisible by 5.
     */

    private boolean bossBattleActive = false;
    private void bossBattle() {
        if (bossBattleActive) return; // Prevent duplicate triggers
        bossBattleActive = true;
        bossBattleStartTime = System.currentTimeMillis();


        JLabel bossLabel = new JLabel("Boss Battle!");
        bossLabel.setFont(new Font("Times Roman", Font.BOLD, 80)); // Large font size for emphasis
        bossLabel.setForeground(Color.RED);
        bossLabel.setHorizontalAlignment(SwingConstants.CENTER);

        int labelWidth = 600;
        int labelHeight = 100;
        int labelX = (screenWidth - labelWidth) - 290;
        int labelY = (screenHeight - labelHeight - 380);
        bossLabel.setBounds(labelX, labelY - 300, labelWidth, labelHeight);

        this.setLayout(null);
        this.add(bossLabel);
        this.revalidate();
        this.repaint();

        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(bossLabel);
                revalidate();
                repaint();
                // Start boss fight or special enemy spawn logic here
                spawnBossEnemy();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Spawns a boss enemy. Customize this as needed for your game.
     */
    private void spawnBossEnemy() {
        int newX;
        int newY;
        int safeDistance = 5;

        do {
            newX = (int) (Math.random() * (maxScreenCol - 2) + 1) * tileSize;
            newY = (int) (Math.random() * (maxScreenRow - 2) + 1) * tileSize;

        } while (map[newY / tileSize][newX / tileSize] == 1 ||
                isOccupiedByEnemy(newX, newY) ||
                Math.abs(playerX - newX) < safeDistance * tileSize ||
                Math.abs(playerY - newY) < safeDistance * tileSize);

        // Create a new Boss instance at the generated coordinates
        Boss boss = new Boss(newX, newY, 2, tileSize, enemies);
        bosses.add(boss);

        // Print a confirmation message (optional)
        System.out.println("Boss spawned at (" + newX + ", " + newY + ")");

        // Any additional logic to set up the boss battle can go here
    }

    public void spawnNewEnemy() {
        int newX;
        int newY;
        int safeDistance = 5; // Minimum distance from the player to spawn an enemy (in tiles)

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
        return x >= 0 && y >= 0 && x < map[0].length && y < map.length && map[y][x] == 1;
    }

    private boolean isCollidingWithEntity(int x1, int y1, int x2, int y2) {
        return x1 < x2 + tileSize && x1 + tileSize > x2 &&
                y1 < y2 + tileSize && y1 + tileSize > y2;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Set the background color to black for contrast
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);

        if (gameOver) {
            // Draw "GAME OVER" text with a shadow for better readability
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("GAME OVER", screenWidth / 2 - 105, screenHeight / 2 + 2); // Shadow offset
            g.setColor(Color.RED);
            g.drawString("GAME OVER", screenWidth / 2 - 100, screenHeight / 2);

            // Draw the longest time with a shadow
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString(longestTime + " -- Level: " + level, screenWidth / 2 - 140, screenHeight / 2 + 40);

            // Draw Level
        } else {
            // Draw the map with a white background and black borders for contrast
            int scaledTileSize = tileSize;
            for (int row = 0; row < maxScreenRow; row++) {
                for (int col = 0; col < maxScreenCol; col++) {
                    int x = col * scaledTileSize - camera.getX();
                    int y = row * scaledTileSize - camera.getY();

                    // Check if the tile is within the visible screen area
                    if (x + scaledTileSize > 0 && x < screenWidth && y + scaledTileSize > 0 && y < screenHeight) {
                        // Set the tile color to white and add a border
                        g.setColor(Color.WHITE);
                        g.fillRect(x, y, scaledTileSize, scaledTileSize);
                        g.setColor(Color.BLACK);
                        g.drawRect(x, y, scaledTileSize, scaledTileSize);
                    }
                }
            }

            // Draw the timer with a drop shadow
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Time: " + formattedTime, screenWidth / 2 - 100, 80);

            // Draw the player with a slight shadow for a 3D effect
            drawPlayer(g);

            // Draw the enemies with a subtle shadow for enhanced visibility
            drawEnemies(g);

            // Draw the boss
            drawBoss(g);

            for (Particle particle : particles) {
                if (particle.isAlive()) {
                    particle.draw(g);
                }
            }
            particles.removeIf(particle -> !particle.isAlive()); // Remove expired particles
        }
    }

    public void drawPlayer(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        // Define the shadow color with a higher opacity for better visibility
        Color shadowColor = new Color(0, 0, 0, 0); // Dark shadow color with partial transparency
        int shadowOffsetX = -14; // Increased horizontal offset for a larger shadow
        int shadowOffsetY = -13; // Increased vertical offset for a larger shadow

        int adjustedX = playerX - camera.getX() + shadowOffsetX;
        int adjustedY = playerY - camera.getY() + shadowOffsetY;

        // Draw shadow using a gradient for a smoother, rounded effect
        g2d.setPaint(new GradientPaint(
                adjustedX, adjustedY, shadowColor,
                adjustedX + (int) (tileSize * 1.5), adjustedY + (int) (tileSize * 1.5), new Color(0, 0, 0, 0)
        ));
        g2d.fillRoundRect(adjustedX, adjustedY, (int) (tileSize * 1.5), (int) (tileSize * 1.5), 50, 50);

        // Draw the player on top of the shadow
        g.setColor(Color.BLACK);
        g.fillRect(playerX - camera.getX(), playerY - camera.getY(), tileSize, tileSize);
    }

    public void drawEnemies(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (Enemy enemy : enemies) {
            // Define the shadow color with a higher opacity for better visibility
            Color shadowColor = new Color(0, 0, 0, 0); // Dark shadow color with partial transparency
            int shadowOffsetX = -14; // Increased horizontal offset for a larger shadow
            int shadowOffsetY = -13; // Increased vertical offset for a larger shadow

            int adjustedX = enemy.x - camera.getX() + shadowOffsetX;
            int adjustedY = enemy.y - camera.getY() + shadowOffsetY;

            // Draw shadow using a gradient for a smoother, rounded effect
            g2d.setPaint(new GradientPaint(
                    adjustedX, adjustedY, shadowColor,
                    adjustedX + (int) (tileSize * 1.5), adjustedY + (int) (tileSize * 1.5), new Color(0, 0, 0, 0)
            ));
            g2d.fillRoundRect(adjustedX, adjustedY, (int) (tileSize * 1.5), (int) (tileSize * 1.5), 50, 50);

            // Draw the enemy on top of the shadow
            g.setColor(Color.darkGray);
            g.fillRect(enemy.x - camera.getX(), enemy.y - camera.getY(), tileSize, tileSize);
        }
    }

    public void drawBoss(Graphics g) {
        for (Boss boss : bosses) {
            int adjustedX = boss.x - camera.getX();
            int adjustedY = boss.y - camera.getY();
            int bossSize = (int) (tileSize * 1.5);

            // Draw the boss body with black and dark purple colors
            g.setColor(new Color(0, 0, 0)); // Black for part of the boss body
            g.fillRoundRect(adjustedX, adjustedY, bossSize, bossSize, 50, 50);

            // Add a dark purple section
            g.setColor(new Color(50, 0, 50)); // Dark purple for another part of the boss body
            g.fillRoundRect(adjustedX, adjustedY, bossSize, bossSize / 2, 50, 50); // Top half in dark purple

            // Draw glowing white eyes for intimidation
            g.setColor(Color.white);
            g.fillOval(adjustedX + (int) (bossSize * 0.3), adjustedY + (int) (bossSize * 0.4), bossSize / 6, bossSize / 6);
            g.fillOval(adjustedX + (int) (bossSize * 0.6), adjustedY + (int) (bossSize * 0.4), bossSize / 6, bossSize / 6);

            // Add a black outline to highlight the boss
            g.setColor(Color.BLACK);
            g.drawRoundRect(adjustedX, adjustedY, bossSize, bossSize, 50, 50);

        }
    }
}



