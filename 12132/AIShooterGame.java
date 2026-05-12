import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;



/**

 * AI SHOOTER CHALLENGE - Debugged Version

 * 修正說明：移除了所有非法 [cite] 標籤，確保語法符合 Java 17+ 規範。

 */

public class AIShooterGame extends JFrame {



    public AIShooterGame() {

        setTitle("AI Shooter Challenge");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new GamePanel());

        pack();

        setLocationRelativeTo(null);

        setVisible(true);

    }



    public static void main(String[] args) {

        SwingUtilities.invokeLater(AIShooterGame::new);

    }



    // --- 內部資料類別 ---

    static class Node {

        int x, y;

        Node parent;



        Node(int x, int y, Node parent) {

            this.x = x;

            this.y = y;

            this.parent = parent;

        }



        @Override

        public boolean equals(Object o) {

            if (this == o) return true;

            if (!(o instanceof Node)) return false;

            Node node = (Node) o;

            return x == node.x && y == node.y;

        }



        @Override

        public int hashCode() {

            return Objects.hash(x, y);

        }

    }



    static class Bullet {

        int x, y;

        int dirX, dirY;



        Bullet(int x, int y, int dirX, int dirY) {

            this.x = x;

            this.y = y;

            this.dirX = dirX;

            this.dirY = dirY;

        }

    }



    // --- 核心遊戲面板 ---

    class GamePanel extends JPanel implements ActionListener {

        private final int TILE_SIZE = 40;

        private final int GRID_SIZE = 12;

        private final javax.swing.Timer gameTimer;



        private int playerX = 5, playerY = 10;

        private int enemyX = 0, enemyY = 0;

        private int hp = 3;

        private int score = 0;

        private boolean isGameOver = false;

        private final java.util.List<Bullet> bullets = new ArrayList<>();



        private final boolean[][] obstacles = new boolean[GRID_SIZE][GRID_SIZE];



        public GamePanel() {

            setPreferredSize(new Dimension(TILE_SIZE * GRID_SIZE, TILE_SIZE * GRID_SIZE));

            setBackground(new Color(10, 20, 35));

            setFocusable(true);

           

            initObstacles();

            setupControls();



            gameTimer = new javax.swing.Timer(600, this);

            gameTimer.start();

        }



        private void initObstacles() {

            obstacles[3][3] = true; obstacles[3][4] = true;

            obstacles[7][2] = true; obstacles[8][2] = true;

            obstacles[5][6] = true; obstacles[6][6] = true;

            obstacles[2][8] = true; obstacles[9][7] = true;

        }



        private void setupControls() {

            addKeyListener(new KeyAdapter() {

                @Override

                public void keyPressed(KeyEvent e) {

                    if (isGameOver) {

                        if (e.getKeyCode() == KeyEvent.VK_R) resetGame();

                        return;

                    }



                    int nextX = playerX, nextY = playerY;

                    switch (e.getKeyCode()) {

                        case KeyEvent.VK_UP, KeyEvent.VK_W -> nextY--;

                        case KeyEvent.VK_DOWN, KeyEvent.VK_S -> nextY++;

                        case KeyEvent.VK_LEFT, KeyEvent.VK_A -> nextX--;

                        case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> nextX++;

                        case KeyEvent.VK_SPACE -> {

                            bullets.add(new Bullet(playerX, playerY, 0, -1));

                        }

                    }



                    if (isValid(nextX, nextY)) {

                        playerX = nextX;

                        playerY = nextY;

                    }

                    repaint();

                }

            });

        }



        private boolean isValid(int x, int y) {

            return x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE && !obstacles[x][y];

        }



        private void resetEnemyPosition() {

            Random rand = new Random();

            int newX, newY;

            do {

                newX = rand.nextInt(GRID_SIZE);

                newY = rand.nextInt(GRID_SIZE - 4);

            } while (!isValid(newX, newY) || (newX == playerX && newY == playerY));

            enemyX = newX;

            enemyY = newY;

        }



        private void resetGame() {

            playerX = 5; playerY = 10;

            enemyX = 0; enemyY = 0;

            hp = 3; score = 0;

            isGameOver = false;

            bullets.clear();

            gameTimer.start();

            repaint();

        }



        private void moveEnemy() {

            Node start = new Node(enemyX, enemyY, null);

            Node target = new Node(playerX, playerY, null);

            Queue<Node> queue = new LinkedList<>();

            Set<Node> visited = new HashSet<>();



            queue.add(start);

            visited.add(start);



            Node endNode = null;

            int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};



            while (!queue.isEmpty()) {

                Node current = queue.poll();

                if (current.equals(target)) {

                    endNode = current;

                    break;

                }

                for (int[] d : dirs) {

                    int nx = current.x + d[0], ny = current.y + d[1];

                    if (isValid(nx, ny)) {

                        Node neighbor = new Node(nx, ny, current);

                        if (!visited.contains(neighbor)) {

                            visited.add(neighbor);

                            queue.add(neighbor);

                        }

                    }

                }

            }



            if (endNode != null) {

                Node step = endNode;

                while (step.parent != null && !step.parent.equals(start)) {

                    step = step.parent;

                }

                enemyX = step.x;

                enemyY = step.y;

            }

        }



        @Override

        public void actionPerformed(ActionEvent e) {

            if (!isGameOver) {

                moveEnemy();

               

                // 移動和檢測射弹
                java.util.List<Bullet> bulletsToRemove = new ArrayList<>();
                for (Bullet bullet : bullets) {
                    // 逐格移動檢測（避免穿過障礙物）
                    for (int step = 0; step < 3; step++) {
                        bullet.x += bullet.dirX;
                        bullet.y += bullet.dirY;
                        
                        // 移除超出邊界的射弹
                        if (bullet.x < 0 || bullet.x >= GRID_SIZE || bullet.y < 0 || bullet.y >= GRID_SIZE) {
                            bulletsToRemove.add(bullet);
                            break;
                        }
                        
                        // 檢測與障礙物的碰撞（立即停止）
                        if (obstacles[bullet.x][bullet.y]) {
                            bulletsToRemove.add(bullet);
                            break;
                        }
                        
                        // 檢測與敵人的碰撞
                        if (bullet.x == enemyX && bullet.y == enemyY) {
                            score += 10;
                            resetEnemyPosition();
                            bulletsToRemove.add(bullet);
                            break;
                        }
                    }
                }

                bullets.removeAll(bulletsToRemove);

               

                // 檢測敵人與玩家碰撞

                if (enemyX == playerX && enemyY == playerY) {

                    hp--;

                    resetEnemyPosition();

                    if (hp <= 0) {

                        isGameOver = true;

                        gameTimer.stop();

                    }

                }

                score++;

                repaint();

            }

        }



        @Override

        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

           

            // 繪製格線

            g.setColor(new Color(40, 60, 80));

            for (int i = 0; i <= GRID_SIZE; i++) {

                g.drawLine(i * TILE_SIZE, 0, i * TILE_SIZE, getHeight());

                g.drawLine(0, i * TILE_SIZE, getWidth(), i * TILE_SIZE);

            }



            // 繪製障礙物

            g.setColor(Color.GRAY);

            for (int x = 0; x < GRID_SIZE; x++) {

                for (int y = 0; y < GRID_SIZE; y++) {

                    if (obstacles[x][y]) {

                        g.fill3DRect(x * TILE_SIZE + 2, y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4, true);

                    }

                }

            }



            // 繪製玩家 (藍色)

            g.setColor(Color.CYAN);

            g.fillRect(playerX * TILE_SIZE + 5, playerY * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);



            // 繪製敵人 (紅色)

            g.setColor(Color.RED);

            g.fillOval(enemyX * TILE_SIZE + 5, enemyY * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);



            // 繪製射弹 (黃色)

            g.setColor(Color.YELLOW);

            for (Bullet bullet : bullets) {

                g.fillOval(bullet.x * TILE_SIZE + 15, bullet.y * TILE_SIZE + 15, 10, 10);

            }



            // UI

            g.setColor(Color.WHITE);

            g.setFont(new Font("Arial", Font.BOLD, 14));

            g.drawString("HP: " + hp, 10, 20);

            g.drawString("SCORE: " + score, 10, 40);

            g.setFont(new Font("Arial", Font.PLAIN, 12));

            g.drawString("SPACE: Shoot", 10, 60);

           

            if (isGameOver) {

                g.setColor(new Color(0, 0, 0, 150));

                g.fillRect(0, 0, getWidth(), getHeight());

                g.setColor(Color.RED);

                g.setFont(new Font("Arial", Font.BOLD, 30));

                g.drawString("GAME OVER", getWidth() / 2 - 90, getHeight() / 2);

                g.setFont(new Font("Arial", Font.PLAIN, 15));

                g.setColor(Color.WHITE);

                g.drawString("Press 'R' to Restart", getWidth() / 2 - 65, getHeight() / 2 + 40);

            }

        }

    }
}
