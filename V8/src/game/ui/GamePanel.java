package game.ui;

import game.entities.*;
import game.entities.towers.TowerFactory;
import game.entities.towers.TowerFactory.TowerType;
import game.util.Difficulty;
import game.util.GameMap;
import game.util.WaveManager;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * This is the main game screen where you place towers and watch enemies walk down the path.
 * It handles all the game logic and drawing.
 */
public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    // How many frames per second (updates per second)
    private static final int FPS      = 60;
    // How many milliseconds between each update
    private static final int TICK_MS  = 1000 / FPS;

    // The different states the game can be in
    public enum GameState { PLAYING, PAUSED, GAME_OVER, VICTORY }
    // What state is the game in?
    private GameState state = GameState.PLAYING;

    // How much gold the player has
    private int gold;
    // How many lives the player has
    private int lives;
    // The player's score
    private int score;
    // The difficulty setting
    private final Difficulty difficulty;
    // Is the game running at 2x speed?
    private boolean fastMode = false;
    // Should the next wave start automatically?
    private boolean autostart = false;

    // All the towers on the map
    private final List<Tower>      towers      = new ArrayList<>();
    // All the enemies on the map
    private final List<Enemy>      enemies     = new ArrayList<>();
    // All the projectiles in flight
    private final List<Projectile> projectiles = new ArrayList<>();
    // Floating text (damage numbers, etc)
    private final List<FloatText>  floatTexts  = new ArrayList<>();

    // The game map
    private final GameMap     map;
    // Manages spawning enemies in waves
    private final WaveManager waveManager;
    // The side panel with tower buttons
    private final SidePanel   sidePanel;

    // Called when the player loses
    private Runnable onGameOver;
    // Called when the player wins
    private Runnable onVictory;

    // What tower type is being placed? (null if not placing)
    private TowerType placingType  = null;
    // Where is the mouse currently?
    private Point     mousePos     = new Point(0,0);
    // Which tower is selected for upgrading/selling?
    private Tower     selectedTower = null;

    // The game loop timer
    private final javax.swing.Timer gameTimer;

    public GamePanel(SidePanel sidePanel, Difficulty difficulty) {
        this.sidePanel  = sidePanel;
        this.difficulty = difficulty;

        // Starting gold/lives scaled by difficulty
        this.gold  = difficulty == Difficulty.EASY ? 200 : difficulty == Difficulty.HARD ? 100000 : 150;
        this.lives = difficulty == Difficulty.EASY ?  25 : difficulty == Difficulty.HARD ?  5 :  20;
        this.score = 0;

        map = new GameMap();
        waveManager = new WaveManager(map.getWaypoints());
        waveManager.setDifficulty(difficulty);

        setPreferredSize(new Dimension(GameMap.WIDTH, GameMap.HEIGHT));
        setBackground(new Color(62,122,56));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        
        sidePanel.setOnStartWave(this::startWave);
        sidePanel.setOnSelectTower(this::beginPlacing);
        sidePanel.setOnUpgrade(this::upgradeSelected);
        sidePanel.setOnSellTower(this::sellSelected);
        sidePanel.setOnToggleSpeed(this::toggleSpeed);
        sidePanel.setOnTogglePause(this::togglePause);
        sidePanel.setOnToggleAutostart(this::toggleAutostart);

        gameTimer = new javax.swing.Timer(TICK_MS, e -> tick());
        gameTimer.start();
    }

    // ── Game loop ────────────────────────────────────────────────
    private void tick() {
        if (state == GameState.PAUSED) { repaint(); return; }
        if (state != GameState.PLAYING) return;

        int steps = fastMode ? 3 : 2;
        for (int s = 0; s < steps; s++) {
            stepGame();
        }
        
        // Autostart next wave if enabled and wave is complete
        if (autostart && waveManager.getCurrentWave() > 0 && waveManager.isWaveClear() && !waveManager.isWaveActive()) {
            startWave();
        }
        
        updateSidePanel();
        repaint();
    }

    private void stepGame() {
        enemies.addAll(waveManager.update());
    
        for (Enemy en : enemies) en.update();
    
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy en = it.next();
            if (en.hasReachedEnd()) {
                lives -= en.getDamage();
                addFloat("-" + en.getDamage() + " ♥", GameMap.WIDTH/2.0, 30, new Color(255,80,80));
                it.remove();
            } else if (!en.isActive()) {
                it.remove();
            }
        }
    
        for (Tower t : towers) {
            t.update();
            Enemy target = t.acquireTarget(enemies);
            if (target != null && t.canFire()) {
                Projectile proj = t.createProjectile();
                if (t instanceof game.entities.towers.MageTower) {
                    proj.setChainStarter(t);
                }
                projectiles.add(proj);
                t.resetCooldown();
            }
        }
    
        // Collect chain projectiles separately — never add to projectiles mid-iteration
        List<Projectile> dead  = new ArrayList<>();
        List<Projectile> chain = new ArrayList<>();
        for (Projectile p : projectiles) {
            p.update();
            if (p.hasHit()) {
                dead.add(p);
                resolveHit(p, chain);  // pass chain list to collect into
            }
        }
        projectiles.removeAll(dead);
        projectiles.addAll(chain);  // safe: loop is done
    
        enemies.removeIf(en -> !en.isActive());
    
        floatTexts.forEach(FloatText::update);
        floatTexts.removeIf(f -> !f.isAlive());
    
        if (lives <= 0) {
            lives = 0;
            state = GameState.GAME_OVER;
            gameTimer.stop();
            if (onGameOver != null) onGameOver.run();
        }
    }

    private void resolveHit(Projectile p, List<Projectile> chainOut) {
        Enemy primary = p.getTarget();
        if (primary != null && primary.isActive()) {
            applyDamage(primary, p.getDamage(), p.isSlow(), p.getSlowMultiplier(), p.getSlowDuration());
            primary.recordLightningHit();
    
            if (p.isChainStarter() && p.getTower() instanceof game.entities.towers.MageTower mage) {
                chainOut.addAll(mage.applyChainDamage(
                    new ArrayList<>(enemies),
                    primary,
                    (en, dmg) -> applyDamage(en, dmg, false, 1.0, 0)
                ));
            }
        }
        if (p.isSplash()) {
            double cx = primary != null ? primary.getCenterX() : p.getCenterX();
            double cy = primary != null ? primary.getCenterY() : p.getCenterY();
            for (Enemy en : enemies) {
                if (en == primary || !en.isActive()) continue;
                double dx = en.getCenterX() - cx, dy = en.getCenterY() - cy;
                if (Math.sqrt(dx*dx + dy*dy) <= p.getSplashRadius())
                    applyDamage(en, p.getDamage() * 0.5, false, 1, 0);
            }
        }
    }

    private void applyDamage(Enemy en, double dmg, boolean slow, double slowMult, int slowDur) {
        en.takeDamage(dmg);
        if (slow) en.applySlow(slowMult, slowDur);
        if (!en.isActive()) {
            int reward = en.getReward();
            gold  += reward;
            score += reward * 10;
            addFloat("+$"+reward, en.getCenterX(), en.getCenterY(),     new Color(255,210,60));
            addFloat("+"+reward*10, en.getCenterX(), en.getCenterY()-16, new Color(100,210,255));
        }
    }

    // ── Rendering ────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        map.draw(g2d);

        // Placement preview
        if (placingType != null && contains(mousePos)) {
            int col = GameMap.toCol(mousePos.x), row = GameMap.toRow(mousePos.y);
            int cx  = col*GameMap.TILE + GameMap.TILE/2;
            int cy  = row*GameMap.TILE + GameMap.TILE/2;
            boolean canPlace = map.canPlace(col, row);
            Tower preview = TowerFactory.create(placingType, cx, cy);
            preview.drawRangeCircle(g2d);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, canPlace?0.75f:0.35f));
            preview.draw(g2d);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            if (!canPlace) {
                g2d.setColor(new Color(255,60,60,180));
                g2d.setStroke(new BasicStroke(3f));
                int tx=col*GameMap.TILE, ty=row*GameMap.TILE, ts=GameMap.TILE;
                g2d.drawLine(tx+6,ty+6,tx+ts-6,ty+ts-6);
                g2d.drawLine(tx+ts-6,ty+6,tx+6,ty+ts-6);
            }
        }

        for (Tower t : towers)      t.draw(g2d);
        for (Enemy en : enemies)    en.draw(g2d);
        for (Projectile p : projectiles) p.draw(g2d);
        for (FloatText f : floatTexts)   f.draw(g2d);

        // Pause overlay
        if (state == GameState.PAUSED) {
            g2d.setColor(new Color(0,0,0,180));
            g2d.fillRect(0,0,getWidth(),getHeight());
            g2d.setColor(new Color(30,30,30));
            int pw = 300, ph = 120;
            int px = getWidth()/2 - pw/2, py = getHeight()/2 - ph/2;
            g2d.fillRoundRect(px, py, pw, ph, 20, 20);
            g2d.setColor(new Color(200,160,60));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRoundRect(px, py, pw, ph, 20, 20);
            g2d.setFont(new Font("Arial",Font.BOLD,42));
            g2d.setColor(Color.WHITE);
            drawCentred(g2d,"PAUSED",getWidth()/2,getHeight()/2-10);
            g2d.setFont(new Font("Arial",Font.PLAIN,15));
            g2d.setColor(new Color(200,200,200));
            drawCentred(g2d,"Press P or click Pause to resume",getWidth()/2,getHeight()/2+22);
        }

        // Wave-cleared banner
        if (waveManager.getCurrentWave()>0 && waveManager.isWaveClear() && state==GameState.PLAYING) {
            drawBanner(g2d,"Wave "+waveManager.getCurrentWave()+" cleared!  Press SPACE for next wave");
        }

        // Speed indicator
        if (fastMode) {
            g2d.setColor(new Color(255,220,50,200));
            g2d.setFont(new Font("Arial",Font.BOLD,13));
            g2d.drawString("▶▶ 2x", 8, GameMap.HEIGHT-8);
        }
    }

    private void drawBanner(Graphics2D g2d, String msg) {
        g2d.setColor(new Color(20,20,20,200));
        g2d.fillRoundRect(60,14,GameMap.WIDTH-120,32,15,15);
        g2d.setColor(new Color(255,200,50));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(60,14,GameMap.WIDTH-120,32,15,15);
        g2d.setFont(new Font("Arial",Font.BOLD,13));
        g2d.setColor(new Color(255,210,60));
        drawCentred(g2d, msg, GameMap.WIDTH/2, 34);
    }

    private void drawCentred(Graphics2D g, String s, int cx, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s)/2, y);
    }

    // ── Actions ──────────────────────────────────────────────────
    private void startWave() {
        if (waveManager.isWaveActive()) return;
        waveManager.startNextWave();
        updateSidePanel();
    }

    private void beginPlacing(TowerType type) {
        int cost = (int)(TowerFactory.getCost(type) * difficulty.costMult);
        if (gold < cost) return;
        placingType = type;
        deselectTower();
        sidePanel.setPlacingType(type);
        requestFocus();
    }

    private void cancelPlacing() { placingType=null; sidePanel.setPlacingType(null); }

    private void placeTower(int col, int row) {
        if (!map.canPlace(col, row)) return;
        int cost = (int)(TowerFactory.getCost(placingType) * difficulty.costMult);
        if (gold < cost) { cancelPlacing(); return; }
        gold -= cost;
        // Centre tower on tile
        int cx = col*GameMap.TILE + GameMap.TILE/2;
        int cy = row*GameMap.TILE + GameMap.TILE/2;
        Tower t = TowerFactory.create(placingType, cx, cy);
        towers.add(t);
        map.setOccupied(col, row, true);
        addFloat("-$"+cost, cx, cy-10, new Color(255,180,60));
        cancelPlacing();
        updateSidePanel();
    }

    private void selectTower(int col, int row) {
        deselectTower();
        for (Tower t : towers) {
            int tc = GameMap.toCol((int)t.getCenterX());
            int tr = GameMap.toRow((int)t.getCenterY());
            if (tc==col && tr==row) {
                selectedTower=t; t.setSelected(true); sidePanel.setSelectedTower(t); return;
            }
        }
    }

    private void deselectTower() {
        if (selectedTower!=null) { selectedTower.setSelected(false); selectedTower=null; }
        sidePanel.setSelectedTower(null);
    }

    private void upgradeSelected() {
        if (selectedTower==null || !selectedTower.canUpgrade()) return;
        int cost = (int)(selectedTower.getUpgradeCost() * difficulty.costMult);
        if (gold<cost) return;
        gold -= cost;
        selectedTower.upgrade();
        addFloat("UPGRADED!", selectedTower.getCenterX()-28, selectedTower.getCenterY()-20, new Color(100,210,255));
        sidePanel.setSelectedTower(selectedTower);
        updateSidePanel();
    }

    private void sellSelected() {
        if (selectedTower==null) return;
        int refund = (int)Math.round(selectedTower.getCost()*0.75f);
        gold += refund;
        addFloat("+$"+refund, selectedTower.getCenterX(), selectedTower.getCenterY(), new Color(255,210,60));
        int col=GameMap.toCol((int)selectedTower.getCenterX());
        int row=GameMap.toRow((int)selectedTower.getCenterY());
        map.setOccupied(col, row, false);
        towers.remove(selectedTower);
        selectedTower=null; sidePanel.setSelectedTower(null);
        updateSidePanel();
    }

    private void toggleSpeed() {
        fastMode=!fastMode; updateSidePanel();
    }

    private void togglePause() {
        if (state==GameState.PLAYING)  state=GameState.PAUSED;
        else if (state==GameState.PAUSED) state=GameState.PLAYING;
        updateSidePanel();
    }

    private void toggleAutostart() {
        autostart = !autostart;
        updateSidePanel();
    }

    private void updateSidePanel() {
        sidePanel.updateStats(gold, lives, score, waveManager.getCurrentWave(),
                              waveManager.isWaveActive(), fastMode, state==GameState.PAUSED, autostart, difficulty.costMult);
        if (selectedTower!=null) sidePanel.setSelectedTower(selectedTower);
    }

    private void addFloat(String text, double x, double y, Color color) {
        floatTexts.add(new FloatText(text,x,y,color));
    }

    // ── Mouse ────────────────────────────────────────────────────
    @Override public void mousePressed(MouseEvent e) {
        requestFocus();
        if (state!=GameState.PLAYING && state!=GameState.PAUSED) return;
        int col=GameMap.toCol(e.getX()), row=GameMap.toRow(e.getY());
        if (SwingUtilities.isRightMouseButton(e)) { cancelPlacing(); deselectTower(); return; }
        if (state==GameState.PAUSED) return;
        if (placingType!=null) placeTower(col,row);
        else selectTower(col,row);
    }
    @Override public void mouseMoved(MouseEvent e)   { mousePos=e.getPoint(); repaint(); }
    @Override public void mouseDragged(MouseEvent e) { mousePos=e.getPoint(); repaint(); }
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    // ── Keys ─────────────────────────────────────────────────────
    @Override public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> { if(placingType!=null) cancelPlacing(); else deselectTower(); }
            case KeyEvent.VK_P      -> togglePause();
            case KeyEvent.VK_SPACE  -> startWave();
            case KeyEvent.VK_F      -> toggleSpeed();
            case KeyEvent.VK_BACK_SPACE -> sellSelected();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}

    public int getScore()  { return score; }
    public int getWave()   { return waveManager.getCurrentWave(); }
    public int getLives()  { return lives; }
    public void setOnGameOver(Runnable r) { onGameOver=r; }
    public void setOnVictory(Runnable r)  { onVictory=r; }

    // ── FloatText ────────────────────────────────────────────────
    private static class FloatText {
        private final String text; private double x,y;
        private final Color color; private float alpha=1f;
        private static final Font F=new Font("Arial",Font.BOLD,12);
        FloatText(String t,double x,double y,Color c){text=t;this.x=x;this.y=y;color=c;}
        void update(){y-=0.9;alpha-=0.018f;}
        boolean isAlive(){return alpha>0;}
        void draw(Graphics2D g){
            g.setFont(F);
            g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),Math.max(0,(int)(alpha*255))));
            g.drawString(text,(int)x,(int)y);
        }
    }
}
