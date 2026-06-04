package game.ui;

import game.util.Difficulty;
import game.util.FileManager;
import game.util.FileManager.ScoreEntry;
import game.util.GameMap;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * Main menu with difficulty selection and leaderboard.
 */
public class MainMenu extends JPanel {
    private Consumer<Difficulty> onStartGame;
    private Difficulty selected = Difficulty.MEDIUM;
    private List<ScoreEntry> scores;
    private float pulse=0f;
    private final javax.swing.Timer anim;

    private static final Color BG       = new Color(45, 30, 12);
    private static final Color BG_CARD  = new Color(62, 42, 18);
    private static final Color BORDER   = new Color(110, 80, 35);
    private static final Color GOLD     = new Color(255, 215, 50);
    private static final Color TEXT     = new Color(240, 225, 190);
    private static final Color TEXT_DIM = new Color(170, 145, 100);

    public MainMenu() {
        setLayout(null);
        setBackground(BG);
        scores = FileManager.loadScores();
        int w = GameMap.WIDTH + SidePanel.PANEL_WIDTH;  // 1084

        // ── Difficulty buttons ───────────────────────────────────
        Difficulty[] diffs = Difficulty.values();
        Color[] cols = {new Color(50,180,50), new Color(200,160,30), new Color(200,50,50)};
        for (int i=0; i<3; i++) {
            final Difficulty d = diffs[i];
            JButton btn = new JButton(d.label);
            btn.setBounds(w/2 - 210 + i*145, 320, 130, 44);
            btn.setFont(new Font("Arial", Font.BOLD, 15));
            btn.setForeground(Color.WHITE);
            btn.setBackground(cols[i]);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            btn.setFocusPainted(false);
            btn.setUI(new RoundedButtonUI());
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                selected = d;
                if (onStartGame!=null) onStartGame.accept(d);
            });
            add(btn);
        }

        // ── Quit ────────────────────────────────────────────────
        JButton btnQuit = new JButton("Quit");
        btnQuit.setBounds(w/2-60, 380, 120, 32);
        btnQuit.setFont(new Font("Arial", Font.PLAIN, 12));
        btnQuit.setForeground(TEXT_DIM);
        btnQuit.setBackground(new Color(55,38,18));
        btnQuit.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnQuit.setFocusPainted(false);
        btnQuit.setUI(new RoundedButtonUI());
        btnQuit.setOpaque(false);
        btnQuit.setContentAreaFilled(false);
        btnQuit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnQuit.addActionListener(e -> System.exit(0));
        add(btnQuit);

        anim = new javax.swing.Timer(30, e -> { pulse+=0.04f; repaint(); });
        anim.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w=getWidth();

        // Background grid
        g2.setColor(new Color(55,38,18));
        g2.setStroke(new BasicStroke(0.5f));
        for (int x=0;x<w;x+=48) g2.drawLine(x,0,x,getHeight());
        for (int y=0;y<getHeight();y+=48) g2.drawLine(0,y,w,y);

        // Title glow
        float gl=(float)(0.6+0.3*Math.sin(pulse));
        g2.setColor(new Color(255,180,30,(int)(gl*50)));
        g2.fillOval(w/2-250,60,500,130);

        // Title
        g2.setFont(new Font("Arial",Font.BOLD,52));
        g2.setColor(GOLD);
        drawC(g2,"TOWER DEFENSE",w/2,138);
        g2.setFont(new Font("Arial",Font.PLAIN,14));
        g2.setColor(TEXT_DIM);
        drawC(g2,"Survive endless waves. Build, upgrade, destroy.",w/2,164);

        // Controls box
        g2.setColor(new Color(62,42,18,220));
        g2.fillRoundRect(w/2-300,178,600,80,12,12);
        g2.setColor(BORDER); g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(w/2-300,178,600,80,12,12);
        g2.setFont(new Font("Arial",Font.PLAIN,11));
        g2.setColor(TEXT_DIM);
        drawC(g2,"Click tower button → click map to place  |  Right-click to cancel  |  Click tower to select",w/2,198);
        drawC(g2,"[SPACE] next wave  |  [P] pause  |  [F] 2x speed  |  [ESC] deselect",w/2,214);
        drawC(g2,"Cannon $100 (splash)  ·  Sniper $150 (infinite range)  ·  Freeze $120 (slows)",w/2,230);
        drawC(g2,"Enemies that reach the exit cost lives.  Waves are unlimited — survive as long as you can!",w/2,246);

        // Difficulty label
        g2.setFont(new Font("Arial",Font.BOLD,14));
        g2.setColor(TEXT);
        drawC(g2,"SELECT DIFFICULTY",w/2,308);

        // Leaderboard
        drawLeaderboard(g2, w/2, 430);
    }

    private void drawLeaderboard(Graphics2D g2, int cx, int y) {
        int bw=500, bh=150;
        g2.setColor(new Color(45,30,12,240));
        g2.fillRoundRect(cx-bw/2,y,bw,bh,15,15);
        g2.setColor(BORDER); g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(cx-bw/2,y,bw,bh,15,15);
        g2.setFont(new Font("Arial",Font.BOLD,13));
        g2.setColor(GOLD);
        drawC(g2,"★  HIGH SCORES  ★",cx,y+20);
        scores=FileManager.loadScores();
        if (scores.isEmpty()) {
            g2.setFont(new Font("Arial",Font.PLAIN,11));
            g2.setColor(TEXT_DIM);
            drawC(g2,"No scores yet — be the first!",cx,y+70);
        } else {
            g2.setFont(new Font("Arial",Font.PLAIN,11));
            int shown=Math.min(scores.size(),5);
            for (int i=0;i<shown;i++) {
                ScoreEntry e=scores.get(i);
                String line=String.format("%d.  %-14s  %,8d pts  Wave %d", i+1, e.name(), e.score(), e.wave());
                g2.setColor(i==0?GOLD:TEXT);
                drawC(g2,line,cx,y+38+i*20);
            }
        }
    }

    private void drawC(Graphics2D g, String s, int cx, int y) {
        FontMetrics fm=g.getFontMetrics();
        g.drawString(s, cx-fm.stringWidth(s)/2, y);
    }

    public void setOnStartGame(Consumer<Difficulty> c) { onStartGame=c; }

    // Custom rounded button UI
    static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private static final int ARC = 14;
        
        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            AbstractButton btn = (AbstractButton) c;
            g2.setColor(btn.getBackground());
            g2.fillRoundRect(0, 0, btn.getWidth()-1, btn.getHeight()-1, ARC, ARC);
            g2.setColor(new Color(255, 255, 255, 80));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(0, 0, btn.getWidth()-1, btn.getHeight()-1, ARC, ARC);
            super.paint(g, c);
        }
    }
}
