package game.entities;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Abstract Tower base class. CannonTower, SniperTower, SlowTower extend this.
 */
public abstract class Tower extends Entity {
    protected double range;
    protected double damage;
    protected int fireRate;     // ticks between shots
    protected int fireCooldown;
    protected int level;        // 1–5
    protected int cost;
    protected int upgradeCost;
    protected String name;
    protected Color baseColor;
    protected Color accentColor;
    protected boolean selected;
    protected double aimAngle = 0;
    protected Enemy currentTarget;

    public Tower(double x, double y, int size, double range, double damage,
                 int fireRate, int cost, int upgradeCost,
                 String name, Color base, Color accent) {
        super(x - size/2.0, y - size/2.0, size, size);
        this.range = range;
        this.damage = damage;
        this.fireRate = fireRate;
        this.fireCooldown = 0;
        this.level = 1;
        this.cost = cost;
        // 50% more expensive than original — multiply constructor upgradeCost by 1.5
        this.upgradeCost = (int)(upgradeCost * 1.5);
        this.name = name;
        this.baseColor = base;
        this.accentColor = accent;
    }

    @Override
    public void update() {
        if (fireCooldown > 0) fireCooldown--;
    }

    /**
     * Finds best target from enemy list (furthest along path in range).
     */
    public Enemy acquireTarget(List<Enemy> enemies) {
        Enemy best = null;
        double bestProgress = -1;
        for (Enemy e : enemies) {
            if (!e.isActive()) continue;
            if (distanceTo(e) <= range) {
                if (e.getPathProgress() > bestProgress) {
                    bestProgress = e.getPathProgress();
                    best = e;
                }
            }
        }
        currentTarget = best;
        if (best != null) {
            aimAngle = Math.atan2(best.getCenterY() - getCenterY(),
                                  best.getCenterX() - getCenterX());
        }
        return best;
    }

    public boolean canFire() {
        return fireCooldown == 0 && currentTarget != null && currentTarget.isActive();
    }

    public void resetCooldown() {
        fireCooldown = fireRate;
    }

    public abstract Projectile createProjectile();

    // Max level is now 5
    public boolean canUpgrade() { return level < 5; }

    public void upgrade() {
        if (!canUpgrade()) return;
        level++;
        applyUpgrade();
        if (level == 5) {
            upgradeCost = Integer.MAX_VALUE; // maxed out
        } else if (level == 4) {
            // Tier 5 is the prestige upgrade — 2.7x scaling PLUS a flat 20k surcharge
            upgradeCost = (int)(upgradeCost * 2.7) + 20_000;
        } else {
            // Each subsequent upgrade costs 50% more (×2.7 = original ×1.8 × 1.5)
            upgradeCost = (int)(upgradeCost * 2.7);
        }
    }

    protected abstract void applyUpgrade();

    @Override
    public void draw(Graphics2D g2d) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Glow effect for upgraded towers — more intense at higher levels
        if (level > 1) {
            int alpha = level >= 4 ? 80 : (level == 3 ? 60 : 40);
            g.setColor(new Color(
                accentColor.getRed(),
                accentColor.getGreen(),
                accentColor.getBlue(),
                alpha
            ));
            int glowSize = level >= 4 ? 12 : (level == 3 ? 8 : 5);
            g.fillOval(
                (int)(getCenterX() - width/2 - glowSize),
                (int)(getCenterY() - height/2 - glowSize),
                width + glowSize*2,
                height + glowSize*2
            );
        }

        if (selected) {
            if (range == Double.MAX_VALUE) {
                g.setColor(new Color(100,255,100,25));
                g.fillRect(0,0,game.util.GameMap.WIDTH,game.util.GameMap.HEIGHT);
                g.setColor(new Color(100,255,100,100));
                g.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{4,4},0));
                g.drawRect(2,2,game.util.GameMap.WIDTH-4,game.util.GameMap.HEIGHT-4);
            } else {
                g.setColor(new Color(255,255,255,30));
                g.fillOval((int)(getCenterX()-range),(int)(getCenterY()-range),(int)(range*2),(int)(range*2));
                g.setColor(new Color(255,255,255,80));
                g.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{4,4},0));
                g.drawOval((int)(getCenterX()-range),(int)(getCenterY()-range),(int)(range*2),(int)(range*2));
            }
        }

        drawBase(g);
        drawBarrel(g);
        drawLevelIndicator(g);

        g.dispose();
    }

    protected void drawBase(Graphics2D g2d) {
        // Shadow
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval((int)x + 3, (int)y + 3, width, height);
        // Base circle
        g2d.setColor(baseColor);
        g2d.fillOval((int)x, (int)y, width, height);
        g2d.setColor(accentColor);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval((int)x, (int)y, width, height);
        // Inner highlight
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.fillOval((int)x + 4, (int)y + 4, width/3, height/3);
    }

    protected void drawBarrel(Graphics2D g2d) {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();
        int barrelLen = getBarrelLength();
        int barrelW = getBarrelWidth();

        Graphics2D rotated = (Graphics2D) g2d.create();
        rotated.rotate(aimAngle, cx, cy);
        rotated.setColor(accentColor);
        rotated.fillRoundRect(cx, cy - barrelW/2, barrelLen, barrelW, 3, 3);
        rotated.dispose();
    }

    protected int getBarrelLength() { return width / 2 + 4; }
    protected int getBarrelWidth() { return 6; }

    protected void drawLevelIndicator(Graphics2D g2d) {
        if (level == 1) return;

        // Colour scale: blue → purple → gold → red-gold
        Color levelColor = switch (level) {
            case 2 -> new Color(180, 180, 255);
            case 3 -> new Color(255, 215, 0);
            case 4 -> new Color(255, 140, 0);
            case 5 -> new Color(255, 60, 60);
            default -> Color.WHITE;
        };

        int badgeY = (int)y - 8;
        int badgeR = 6 + (level - 2);          // grows with level
        int badgeDiam = badgeR * 2;
        int bx = (int)x + width/2 - badgeR;

        // Outer glow for Lv4+
        if (level >= 4) {
            g2d.setColor(new Color(levelColor.getRed(), levelColor.getGreen(), levelColor.getBlue(), 70));
            g2d.fillOval(bx - 4, badgeY - badgeR - 4, badgeDiam + 8, badgeDiam + 8);
        }
        // Badge fill
        g2d.setColor(levelColor);
        g2d.fillOval(bx, badgeY - badgeR, badgeDiam, badgeDiam);
        // Badge border
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.setStroke(new BasicStroke(level >= 4 ? 1.5f : 1f));
        g2d.drawOval(bx, badgeY - badgeR, badgeDiam, badgeDiam);
        // Level number
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 8 + (level - 2)));
        FontMetrics fm = g2d.getFontMetrics();
        String lvlStr = String.valueOf(level);
        g2d.drawString(lvlStr,
            bx + badgeR - fm.stringWidth(lvlStr)/2,
            badgeY - badgeR + badgeDiam/2 + fm.getAscent()/2 - 1);
    }

    public void drawRangeCircle(Graphics2D g2d) {
        if (range == Double.MAX_VALUE) {
            g2d.setColor(new Color(100,255,100,60));
            g2d.fillRect(0,0,game.util.GameMap.WIDTH, game.util.GameMap.HEIGHT);
            g2d.setColor(new Color(100,255,100,160));
            g2d.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{6,4},0));
            g2d.drawRect(2,2,game.util.GameMap.WIDTH-4,game.util.GameMap.HEIGHT-4);
            return;
        }
        g2d.setColor(new Color(200,230,255,40));
        g2d.fillOval((int)(getCenterX()-range),(int)(getCenterY()-range),(int)(range*2),(int)(range*2));
        g2d.setColor(new Color(200,230,255,120));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval((int)(getCenterX()-range),(int)(getCenterY()-range),(int)(range*2),(int)(range*2));
    }

    // Getters
    public double getRange()      { return range; }
    public double getDamage()     { return damage; }
    public int getCost()          { return cost; }
    public int getUpgradeCost()   { return upgradeCost; }
    public String getName()       { return name; }
    public int getLevel()         { return level; }
    public boolean isSelected()   { return selected; }
    public void setSelected(boolean s) { this.selected = s; }
    public String getDescription(){ return name + " Lv." + level; }
    public abstract String getStatLine();
    public abstract String getUpgradeEffect();
    public Point2D.Double getMuzzlePoint() {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();
        int barrelLen = getBarrelLength();
        return new Point2D.Double(cx + barrelLen * Math.cos(aimAngle),
                                  cy + barrelLen * Math.sin(aimAngle));
    }
}