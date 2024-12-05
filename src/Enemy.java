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

}
