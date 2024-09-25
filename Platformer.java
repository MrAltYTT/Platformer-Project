import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Platformer extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int playerX, playerY;
    private int playerWidth, playerHeight;
    private int velocityY;
    private final int gravity = 1;
    private final int jumpStrength = 15;
    private final int playerSpeed = 4;

    private ArrayList<Rectangle> blocks;
    private ArrayList<Polygon> spikes;
    private Random rand;

    private int cameraX;
    private boolean gameOver;

    public Platformer() {
        timer = new Timer(20, this);
        timer.start();
        playerWidth = 50;
        playerHeight = 50;
        velocityY = 0;
        rand = new Random();

        initGame();

        setFocusable(true);
        addKeyListener(this);
    }

    private void initGame() {
        blocks = new ArrayList<>();
        spikes = new ArrayList<>();

        playerX = 100;
        playerY = 300;
        velocityY = 0;
        cameraX = 0;
        gameOver = false;

        
        generateObstacles();
    }

    private void generateObstacles() {
        int xPosition = playerX + 800; 
        while (xPosition < playerX + 5000) { 
            if (rand.nextBoolean()) {
                // Add a block
                int blockHeight = rand.nextInt(50) + 50; 
                Rectangle block = new Rectangle(xPosition, 350 - blockHeight, 50, blockHeight);
                blocks.add(block);
            } else {
                
                int spikeX = xPosition;
                int spikeY = 350 - 50;
                Polygon spike = createSpike(spikeX, spikeY, 50, 50);
                spikes.add(spike);
            }
            xPosition += rand.nextInt(200) + 200; 
        }
    }

    private Polygon createSpike(int x, int y, int width, int height) {
        int[] xPoints = { x, x + width / 2, x + width };
        int[] yPoints = { y + height, y, y + height };
        return new Polygon(xPoints, yPoints, 3);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Ground
        g.setColor(Color.GREEN);
        g.fillRect(0, 350, getWidth(), 50);

        // Blocks
        g.setColor(Color.GRAY);
        for (Rectangle block : blocks) {
            g.fillRect(block.x - cameraX, block.y, block.width, block.height);
        }

        // Spikes
        g.setColor(Color.BLACK);
        for (Polygon spike : spikes) {
            Polygon shiftedSpike = shiftPolygon(spike, -cameraX, 0);
            g.fillPolygon(shiftedSpike);
        }

        // Player
        g.setColor(Color.RED);
        g.fillRect(playerX - cameraX, playerY, playerWidth, playerHeight);

        // Game Over Message
        if (gameOver) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Game Over!", getWidth() / 2 - 150, getHeight() / 2);
        }
    }

    private Polygon shiftPolygon(Polygon p, int dx, int dy) {
        Polygon shifted = new Polygon();
        for (int i = 0; i < p.npoints; i++) {
            shifted.addPoint(p.xpoints[i] + dx, p.ypoints[i] + dy);
        }
        return shifted;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            update();
            repaint();
        }
    }

    private void update() {
      
        playerX += playerSpeed;

      
        cameraX = playerX - 100;

       
        playerY += velocityY;
        velocityY += gravity;

        // Ground collision
        if (playerY + playerHeight >= 350) {
            playerY = 350 - playerHeight;
            velocityY = 0;
        }

        // Collision detection with blocks
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        for (Rectangle block : blocks) {
            if (playerRect.intersects(block)) {
                Rectangle intersection = playerRect.intersection(block);

                if (intersection.getWidth() < intersection.getHeight()) {
                    
                    gameOver();
                    return;
                } else if (playerY + playerHeight - intersection.getHeight() <= block.y) {
                   
                    playerY = block.y - playerHeight;
                    velocityY = 0;
                } else {
                    
                    playerY = block.y + block.height;
                    velocityY = 0;
                }
            }
        }

        // Collision detection with spikes
        for (Polygon spike : spikes) {
            if (spike.intersects(playerRect)) {
                
                gameOver();
                return;
            }
        }

        // Remove obstacles that pass
        blocks.removeIf(block -> block.x + block.width < playerX - 200);
        spikes.removeIf(spike -> {
            Rectangle bounds = spike.getBounds();
            return bounds.x + bounds.width < playerX - 200;
        });

        // Generate new obstacles
        if (blocks.size() + spikes.size() < 10) {
            generateObstacles();
        }
    }

    private void gameOver() {
        gameOver = true;
        timer.stop();
        repaint();
        int response = JOptionPane.showConfirmDialog(this, "You trash lil bro", "Game Over",
                JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            initGame();
            timer.start();
        } else {
            System.exit(0);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

       
        if ((key == KeyEvent.VK_SPACE || key == KeyEvent.VK_UP) && playerY + playerHeight >= 350) {
            velocityY = -jumpStrength;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
       
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Sigma Dash");
        Platformer game = new Platformer();
        frame.add(game);
        frame.setSize(800, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
