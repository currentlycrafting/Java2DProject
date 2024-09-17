
import javax.swing.JPanel;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable{

    // Screen Sizes
    final int originalTileSize = 16; // 16X16 tile
    final int scale = 3;
    final int tileSize = originalTileSize * scale; // 48 * 48 tile
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    final int screenWidth = tileSize * maxScreenCol; // 768 pixels
    final int screenHeight = tileSize * maxScreenRow; // 576 pixels

    // FPS
    int FPS = 60;
    KeyHandler keyH = new KeyHandler();
    Thread gameThread;

    // Initializes the player positions

    int playerX = 100;
    int playerY = 100;
    int playerSpeed = 4;
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.white);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

    }

    public void startGameThread() {

        gameThread = new Thread(this);
        gameThread.start();
    }

    // RUN CODE
    public void run() {

        double drawInterval = (double) 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;
        // DELTA RUN TIME + FPS COUNTER
        while(gameThread != null) {

            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if(delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }
            if(timer >= 1000000000) {
                System.out.println("FPS:" + drawCount);
                drawCount = 0;
                timer = 0;
            }

        }
    }
    public void update(){
        // FIX THIS SO DIAGONALLY PRESSING DOESNT MAKE YOU GO FASTER PYTHOGOREAN
        if(keyH.upPressed) {
            playerY -= playerSpeed;
        }
        else if(keyH.downPressed) {
            playerY += playerSpeed;
        }
        if(keyH.leftPressed) {
            playerX -= playerSpeed;
        }
        else if(keyH.rightPressed) {
            playerX += playerSpeed;
        }
    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        g2.setColor(Color.black);
        g2.fillRect(playerX, playerY, tileSize, tileSize);
        g2.dispose();


    }
}
