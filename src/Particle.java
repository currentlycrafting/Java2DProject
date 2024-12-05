import java.awt.*;

class Particle {
    int x, y;
    Color color;
    long lifeTime; // In milliseconds
    long creationTime;

    public Particle(int x, int y, Color color, long lifeTime) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.lifeTime = lifeTime;
        this.creationTime = System.currentTimeMillis();
    }

    public boolean isAlive() {
        return System.currentTimeMillis() - creationTime < lifeTime;
    }

    public void draw(Graphics g) {
        if (isAlive()) {
            Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);
            g.setColor(transparentColor);
            g.fillOval(x, y, 10, 10); // Larger particle size (10x10)
        }
    }
}
