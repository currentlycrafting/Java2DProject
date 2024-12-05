public class Camera {
    private int x;
    private int y;
    private final int mapWidth;
    private final int mapHeight;
    private final int screenWidth;
    private final int screenHeight;

    public Camera(int mapWidth, int mapHeight, int screenWidth, int screenHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.x = 0;
        this.y = 0;
    }

    public void update(int playerX, int playerY) {
        int halfScreenWidth = screenWidth / 2;
        int halfScreenHeight = screenHeight / 2;

        int newX = playerX - halfScreenWidth;
        int newY = playerY - halfScreenHeight;

        this.x = newX;
        this.y = newY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
