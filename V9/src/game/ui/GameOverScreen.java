package game.ui;

import game.util.FileManager;
import java.awt.*;
import javax.swing.*;

/**
 * This screen shows when the game ends (win or lose).
 * The player can enter their name and save their score to the leaderboard.
 */
public class GameOverScreen extends JPanel {
    // Called when the player wants to play again
    private Runnable onPlayAgain;
    // Called when the player wants to go back to the main menu
    private Runnable onMainMenu;

    // Did the player win?
    private final boolean victory;
    // The final score
    private final int score;
    // What wave did they get to?
    private final int wave;
    // How many lives did they have left?
    private final int lives;

    // Text field for entering the player's name
    private final JTextField nameField;
    // Button to save the score
    private final JButton    btnSave;
    // Button to play again
    private final JButton    btnPlayAgain;
    // Button to go to main menu
    private final JButton    btnMenu;
    // Label to show save status
    private final JLabel     statusLabel;
    // Has the score been saved?
    private boolean          scoreSaved = false;

    // Pulsing animation
    private float pulse = 0f;
    // Animation timer
    private final Timer anim;

    // Colors for the screen
    private static final Color BG      = new Color(12, 14, 22);
    private static final Color GOLD    = new Color(255, 210, 60);
    private static final Color WIN_COL = new Color(80, 220, 100);
    private static final Color LOSE_COL= new Color(220, 70, 70);
    private static final Color TEXT    = new Color(210, 215, 230);
    private static final Color DIM     = new Color(120, 130, 155);

    public GameOverScreen(boolean victory, int score, int wave, int lives) {
        this.victory = victory;
        this.score   = score;
        this.wave    = wave;
        this.lives   = lives;

        setLayout(null);
        setBackground(BG);

        int cx = 432; // half of 864

        // Name entry
        JLabel nameLabel = new JLabel("Enter your name:");
        nameLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        nameLabel.setForeground(DIM);
        nameLabel.setBounds(cx - 150, 330, 200, 22);
        add(nameLabel);

        nameField = new JTextField("Player");
        nameField.setBounds(cx - 150, 355, 200, 32);
        nameField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        nameField.setBackground(new Color(28, 32, 48));
        nameField.setForeground(TEXT);
        nameField.setCaretColor(TEXT);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 140, 200), 2),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                nameField.selectAll();
        add(nameField);

        btnSave = new JButton("💾  SAVE SCORE");
        btnSave.setBounds(cx + 64, 355, 160, 32);
        styleButton(btnSave, new Color(80, 160, 230));
        btnSave.setUI(new RoundedButtonUI());
        btnSave.setOpaque(false);
        btnSave.setContentAreaFilled(false);
        btnSave.addActionListener(e -> saveScore());
        add(btnSave);

        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusLabel.setForeground(WIN_COL);
        statusLabel.setBounds(cx - 150, 394, 400, 20);
        add(statusLabel);

        btnPlayAgain = new JButton("▶  PLAY AGAIN");
        btnPlayAgain.setBounds(cx - 170, 430, 200, 42);
        styleButton(btnPlayAgain, new Color(90, 200, 90));
        btnPlayAgain.setUI(new RoundedButtonUI());
        btnPlayAgain.setOpaque(false);
        btnPlayAgain.setContentAreaFilled(false);
        btnPlayAgain.addActionListener(e -> { if (onPlayAgain != null) onPlayAgain.run(); });
        add(btnPlayAgain);

        btnMenu = new JButton("⌂  MAIN MENU");
        btnMenu.setBounds(cx + 50, 430, 200, 42);
        styleButton(btnMenu, new Color(60, 80, 130));
        btnMenu.setUI(new RoundedButtonUI());
        btnMenu.setOpaque(false);
        btnMenu.setContentAreaFilled(false);
        btnMenu.addActionListener(e -> { if (onMainMenu != null) onMainMenu.run(); });
        add(btnMenu);

        anim = new Timer(30, e -> { pulse += 0.05f; repaint(); });
        anim.start();
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Monospaced", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void saveScore() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Anonymous";
        if (name.length() > 16) name = name.substring(0, 16);
        FileManager.saveScore(name, score, wave);
        scoreSaved = true;
        statusLabel.setText("✓ Score saved!  Rank #" + FileManager.getRank(score));
        btnSave.setEnabled(false);
        nameField.setEnabled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        Color accent = victory ? WIN_COL : LOSE_COL;

        // Background glow
        float glow = (float)(0.5 + 0.3 * Math.sin(pulse));
        g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(glow * 45)));
        g2d.fillOval(w/2 - 300, 60, 600, 300);

        // Big result text
        g2d.setFont(new Font("Monospaced", Font.BOLD, 56));
        g2d.setColor(accent);
        String headline = victory ? "VICTORY!" : "GAME OVER";
        drawCentered(g2d, headline, w/2, 150);

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g2d.setColor(DIM);
        String sub = victory ? "You survived all 15 waves!" : "Your base was overwhelmed.";
        drawCentered(g2d, sub, w/2, 178);

        // Stats box
        int bx = w/2 - 240, by = 200, bw = 480, bh = 120;
        g2d.setColor(new Color(22, 26, 40));
        g2d.fillRoundRect(bx, by, bw, bh, 16, 16);
        g2d.setColor(accent.darker());
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRoundRect(bx, by, bw, bh, 16, 16);

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        int statY = by + 30;
        drawStat(g2d, "Final Score", String.format("%,d", score), GOLD,   bx + 30, statY);
        drawStat(g2d, "Wave Reached", String.valueOf(wave),       TEXT,   bx + 200, statY);
        drawStat(g2d, "Lives Left",   String.valueOf(lives),      accent, bx + 360, statY);

        // Leaderboard preview
        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2d.setColor(GOLD);
        drawCentered(g2d, "─── LEADERBOARD ───", w/2, by + 75);
        var topScores = FileManager.loadScores();
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        int shown = Math.min(topScores.size(), 3);
        for (int i = 0; i < shown; i++) {
            var e = topScores.get(i);
            String line = String.format("%d. %-12s  %,d pts", i+1, e.name(), e.score());
            g2d.setColor(i == 0 ? GOLD : TEXT);
            drawCentered(g2d, line, w/2, by + 93 + i * 14);
        }
        if (topScores.isEmpty()) {
            g2d.setColor(DIM);
            drawCentered(g2d, "No saved scores yet", w/2, by + 95);
        }
    }

    private void drawStat(Graphics2D g2d, String label, String value, Color valColor, int x, int y) {
        g2d.setColor(DIM);
        g2d.drawString(label, x, y);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2d.setColor(valColor);
        g2d.drawString(value, x, y + 22);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
    }

    private void drawCentered(Graphics2D g, String s, int cx, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s)/2, y);
    }

    public void setOnPlayAgain(Runnable r) { onPlayAgain = r; }
    public void setOnMainMenu(Runnable r)  { onMainMenu  = r; }

    // Custom rounded button UI
    static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private static final int ARC = 16;
        
        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            AbstractButton btn = (AbstractButton) c;
            g2.setColor(btn.getBackground());
            g2.fillRoundRect(0, 0, btn.getWidth()-1, btn.getHeight()-1, ARC, ARC);
            g2.setColor(new Color(255, 255, 255, 100));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, btn.getWidth()-1, btn.getHeight()-1, ARC, ARC);
            super.paint(g, c);
        }
    }
}
