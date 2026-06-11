package game.ui;

import game.entities.Tower;
import game.entities.towers.TowerFactory;
import game.entities.towers.TowerFactory.TowerType;
import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * This is the side panel on the right side of the game.
 * It shows tower buttons, gold, lives, wave number, and control buttons.
 */
public class SidePanel extends JPanel {
    // How wide this panel is
    public static final int PANEL_WIDTH = 220;

    // Callbacks for button presses
    private Runnable            onStartWave;
    private Consumer<TowerType> onSelectTower;
    private Runnable            onUpgrade;
    private Runnable            onSellTower;
    private Runnable            onToggleSpeed;
    private Runnable            onTogglePause;
    private Runnable            onToggleAutostart;

    // Current game state
    private int gold=150, lives=20, score=0, wave=0;
    // Is a wave currently active?
    private boolean waveInProgress=false;
    // Is 2x speed enabled?
    private boolean fastMode=false;
    // Is the game paused?
    private boolean paused=false;
    // Should waves autostart?
    private boolean autostart=false;
    // Which tower is selected?
    private Tower selectedTower=null;
    // Which tower type is being placed?
    private TowerType placingType=null;

    // Buttons for UI
    private final JButton btnStartWave;
    private final JButton btnSpeed;
    private final JButton btnPause;
    private final JButton btnAutostart;
    private final JButton btnUpgrade;
    private final JButton btnSell;
    // Buttons for each tower type (4 towers)
    private final JButton[] towerBtns = new JButton[4];
    // The 4 tower types
    private final TowerType[] towerTypes = {TowerType.CANNON, TowerType.SNIPER, TowerType.SLOW, TowerType.MAGE};

    private static final Color BG         = new Color(62, 42, 18);
    private static final Color BG_CARD    = new Color(80, 55, 25);
    private static final Color BG_DARK    = new Color(45, 30, 12);
    private static final Color BORDER     = new Color(110, 80, 35);
    private static final Color GOLD_COL   = new Color(255, 215, 50);
    private static final Color LIVES_COL  = new Color(255, 80, 80);
    private static final Color TEXT       = new Color(240, 225, 190);
    private static final Color TEXT_DIM   = new Color(170, 145, 100);
    private static final Color GREEN_BTN  = new Color(50, 170, 50);
    private static final Color BLUE_BTN   = new Color(50, 130, 210);
    private static final Color MAGE_CARD  = new Color(55, 25, 85);   // purple tint for mage card

    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 13);
    private static final Font FONT_BODY  = new Font("Arial", Font.PLAIN, 11);
    private static final Font FONT_SMALL = new Font("Arial", Font.PLAIN, 10);

    public SidePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, 624));
        setBackground(BG);
        setLayout(null);

        // ── Tower shop buttons (4 cards) ────────────────────────
        String[] descs = {"Splash damage", "Infinite range", "Slows enemies", "Chain lightning"};
        String[] costs = {"$100", "$150", "$120", "$250"};
        for (int i = 0; i < 4; i++) {
            final TowerType type = towerTypes[i];
            // Mage card gets a purple tint
            Color cardBg = (type == TowerType.MAGE) ? MAGE_CARD : BG_CARD;
            JButton btn = new JButton(
                "<html><center><b>" + TowerFactory.getName(type) + "</b><br>"
                + "<font size='2'>" + descs[i] + " — " + costs[i] + "</font></center></html>");
            btn.setBounds(8, 170 + i * 56, PANEL_WIDTH - 16, 48);
            btn.setFont(FONT_SMALL);
            btn.setForeground(TEXT);
            btn.setBackground(cardBg);
            btn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setContentAreaFilled(true);
            btn.setOpaque(false);
            btn.setUI(new RoundedButtonUI());
            btn.addActionListener(e -> { if (onSelectTower != null) onSelectTower.accept(type); });
            add(btn);
            towerBtns[i] = btn;
        }

        // ── Start Wave button ───────────────────────────────────
        btnStartWave = new JButton("▶  START WAVE 1");
        btnStartWave.setBounds(8, 398, PANEL_WIDTH - 16, 38);
        style(btnStartWave, GREEN_BTN, Color.WHITE, FONT_TITLE);
        btnStartWave.setUI(new RoundedButtonUI());
        btnStartWave.addActionListener(e -> { if (onStartWave != null) onStartWave.run(); });
        add(btnStartWave);

        // ── Speed / Pause / Autostart ────────────────────────────
        btnSpeed = new JButton("▶▶ 2x");
        btnSpeed.setBounds(8, 442, (PANEL_WIDTH - 20) / 3, 30);
        style(btnSpeed, BG_DARK, GOLD_COL, FONT_BODY);
        btnSpeed.setUI(new RoundedButtonUI());
        btnSpeed.setOpaque(false);
        btnSpeed.addActionListener(e -> { if (onToggleSpeed != null) onToggleSpeed.run(); });
        add(btnSpeed);

        btnPause = new JButton("⏸ Pause");
        btnPause.setBounds(8 + (PANEL_WIDTH - 20) / 3 + 4, 442, (PANEL_WIDTH - 20) / 3, 30);
        style(btnPause, BG_DARK, TEXT, FONT_BODY);
        btnPause.setUI(new RoundedButtonUI());
        btnPause.setOpaque(false);
        btnPause.addActionListener(e -> { if (onTogglePause != null) onTogglePause.run(); });
        add(btnPause);

        btnAutostart = new JButton("⚡ Auto");
        btnAutostart.setBounds(8 + 2 * ((PANEL_WIDTH - 20) / 3 + 4), 442, (PANEL_WIDTH - 20) / 3, 30);
        style(btnAutostart, BG_DARK, TEXT_DIM, FONT_BODY);
        btnAutostart.setUI(new RoundedButtonUI());
        btnAutostart.setOpaque(false);
        btnAutostart.addActionListener(e -> { if (onToggleAutostart != null) onToggleAutostart.run(); });
        add(btnAutostart);

        // ── Upgrade button ──────────────────────────────────────
        btnUpgrade = new JButton("⬆  UPGRADE");
        btnUpgrade.setBounds(8, 478, PANEL_WIDTH - 16, 34);
        style(btnUpgrade, BLUE_BTN, Color.WHITE, FONT_BODY);
        btnUpgrade.setUI(new RoundedButtonUI());
        btnUpgrade.setEnabled(false);
        btnUpgrade.addActionListener(e -> { if (onUpgrade != null) onUpgrade.run(); });
        add(btnUpgrade);

        // ── Sell button ─────────────────────────────────────────
        btnSell = new JButton("✕  SELL");
        btnSell.setBounds(8, 518, PANEL_WIDTH - 16, 34);
        style(btnSell, new Color(180, 50, 50), Color.WHITE, FONT_BODY);
        btnSell.setUI(new RoundedButtonUI());
        btnSell.setEnabled(false);
        btnSell.addActionListener(e -> { if (onSellTower != null) onSellTower.run(); });
        add(btnSell);
    }

    private void style(JButton b, Color bg, Color fg, Font f) {
        b.setBackground(bg); b.setForeground(fg); b.setFont(f);
        b.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();

        // ── Top resource bar ────────────────────────────────────
        g2.setColor(BG_DARK);
        g2.fillRoundRect(0, 0, w, 62, 0, 0);
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, 62, w, 62);

        // Lives
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(LIVES_COL);
        g2.drawString("♥", 10, 40);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(TEXT);
        g2.drawString(String.valueOf(lives), 36, 40);

        // Gold coin
        g2.setColor(GOLD_COL);
        g2.fillOval(90, 18, 22, 22);
        g2.setColor(new Color(180, 140, 0));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(90, 18, 22, 22);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(BG_DARK);
        g2.drawString("$", 97, 33);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(GOLD_COL);
        g2.drawString(String.valueOf(gold), 116, 36);

        // Wave / Score
        g2.setColor(BG_DARK);
        g2.fillRoundRect(0, 62, w, 50, 8, 8);
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 62, w - 1, 50, 8, 8);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(TEXT);
        String waveStr = "ROUND  " + wave;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(waveStr, w / 2 - fm.stringWidth(waveStr) / 2, 82);
        g2.setFont(FONT_SMALL); g2.setColor(TEXT_DIM);
        String scoreStr = String.format("Score: %,d", score);
        fm = g2.getFontMetrics();
        g2.drawString(scoreStr, w / 2 - fm.stringWidth(scoreStr) / 2, 98);

        // ── Shop label ──────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(TEXT_DIM);
        g2.drawString("TOWERS", 12, 162);
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(65, 155, w - 10, 155);

        // ── Selected tower panel ────────────────────────────────
        if (selectedTower != null) {
            int ty = 556;
            g2.setColor(BG_DARK);
            g2.fillRoundRect(6, ty, w - 12, 62, 12, 12);
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(6, ty, w - 12, 62, 12, 12);

            g2.setFont(FONT_TITLE);
            g2.setColor(TEXT);
            g2.drawString(selectedTower.getName() + "  Lv." + selectedTower.getLevel(), 12, ty + 16);
            g2.setFont(FONT_SMALL); g2.setColor(TEXT_DIM);
            g2.drawString(selectedTower.getStatLine(), 12, ty + 29);
            if (selectedTower.canUpgrade()) {
                g2.setColor(GOLD_COL);
                g2.drawString("Upgrade: $" + selectedTower.getUpgradeCost(), 12, ty + 42);
                g2.setColor(new Color(150, 220, 150));
                g2.drawString(selectedTower.getUpgradeEffect(), 12, ty + 55);
            } else {
                g2.setColor(GOLD_COL); g2.drawString("MAX LEVEL", 12, ty + 42);
            }
        }

        // ── Placing hint ────────────────────────────────────────
        if (placingType != null) {
            g2.setColor(new Color(255, 220, 80, 210));
            g2.setFont(FONT_SMALL);
            String hint1 = "Click map to place";
            String hint2 = "[Right-click] cancel";
            g2.drawString(hint1, w / 2 - g2.getFontMetrics().stringWidth(hint1) / 2, 554);
            g2.drawString(hint2, w / 2 - g2.getFontMetrics().stringWidth(hint2) / 2, 567);
        }
    }

    public void updateStats(int gold, int lives, int score, int wave, boolean waveInProgress,
                            boolean fast, boolean paused, boolean autostart, double costMult) {
        this.gold = gold; this.lives = lives; this.score = score;
        this.wave = wave; this.waveInProgress = waveInProgress;
        this.fastMode = fast; this.paused = paused; this.autostart = autostart;

        btnStartWave.setEnabled(!waveInProgress);
        btnStartWave.setText(waveInProgress ? "⏳  WAVE IN PROGRESS" : "▶  START WAVE " + (wave + 1));
        btnStartWave.setBackground(waveInProgress ? new Color(80, 60, 30) : GREEN_BTN);

        btnSpeed.setText(fast ? "▶  1x" : "▶▶ 2x");
        btnSpeed.setForeground(fast ? new Color(100, 255, 100) : GOLD_COL);
        btnPause.setText(paused ? "▶ Resume" : "⏸ Pause");
        btnAutostart.setText(autostart ? "⚡ ON" : "⚡ Auto");
        btnAutostart.setForeground(autostart ? new Color(100, 255, 100) : TEXT_DIM);
        btnAutostart.setBackground(autostart ? new Color(60, 80, 40) : BG_DARK);

        for (int i = 0; i < towerBtns.length; i++) {
            int actualCost = (int)(TowerFactory.getCost(towerTypes[i]) * costMult);
            boolean canAfford = gold >= actualCost;
            towerBtns[i].setEnabled(canAfford);
            boolean isMage = towerTypes[i] == TowerType.MAGE;
            towerBtns[i].setBackground(canAfford
                ? (isMage ? MAGE_CARD : BG_CARD)
                : new Color(55, 38, 18));
        }
        repaint();
    }

    public void setSelectedTower(Tower t) {
        selectedTower = t;
        boolean has = (t != null);
        btnUpgrade.setEnabled(has && t.canUpgrade() && gold >= t.getUpgradeCost());
        btnSell.setEnabled(has);
        repaint();
    }

    public void setPlacingType(TowerType type) { placingType = type; repaint(); }

    // Callback setters
    public void setOnStartWave(Runnable r)            { onStartWave = r; }
    public void setOnSelectTower(Consumer<TowerType> c){ onSelectTower = c; }
    public void setOnUpgrade(Runnable r)              { onUpgrade = r; }
    public void setOnSellTower(Runnable r)            { onSellTower = r; }
    public void setOnToggleSpeed(Runnable r)          { onToggleSpeed = r; }
    public void setOnTogglePause(Runnable r)          { onTogglePause = r; }
    public void setOnToggleAutostart(Runnable r)      { onToggleAutostart = r; }

    // Custom rounded button UI
    static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private static final int ARC = 12;

        @Override
        protected void paintButtonPressed(Graphics g, AbstractButton b) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = b.getBackground();
            g2.setColor(new Color(Math.max(0, bg.getRed()-30), Math.max(0, bg.getGreen()-30), Math.max(0, bg.getBlue()-30)));
            g2.fillRoundRect(0, 0, b.getWidth()-1, b.getHeight()-1, ARC, ARC);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            AbstractButton btn = (AbstractButton) c;
            Color bg = btn.isEnabled() ? btn.getBackground() : new Color(80, 60, 40);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, btn.getWidth()-1, btn.getHeight()-1, ARC, ARC);
            g2.setColor(new Color(150, 120, 60, 100));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, btn.getWidth()-1, btn.getHeight()-1, ARC, ARC);
            super.paint(g, c);
        }
    }
}