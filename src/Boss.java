import java.util.List;

public class Boss extends Enemy {
    private int spawnRate; // Determines how often the boss spawns new enemies
    private long lastSpawnTime;
    private long spawnTime; // Time the boss was spawned
    private List<Enemy> enemyList; // Reference to the list of enemies in the game
    private int level = 1; // Initial level of the boss
    private boolean defeated; // defeated bool

    public Boss(int x, int y, int speed, int tileSize, List<Enemy> enemyList) {
        super(x, y, speed, tileSize);
        this.spawnRate = 3000; // Spawn new enemies every 3000ms (3 seconds)
        this.lastSpawnTime = System.currentTimeMillis();
        this.spawnTime = System.currentTimeMillis(); // Record the time the boss was created
        this.enemyList = enemyList;
        this.defeated = false;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

}
