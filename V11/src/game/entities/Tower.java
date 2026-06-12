package game.entities;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * This is the base class for all towers.
 * Towers shoot projectiles at enemies to stop them.
 */
public abstract class Tower extends Entity {
    // How far the tower can shoot
    protected double range;
    // How much damage each shot does
    protected double damage;
    // How many game ticks between shots (lower = faster)
    protected int fireRate;
    // How many ticks until the tower can shoot again
    protected int fireCooldown;
    // What level is the tower (1 = new, 5 = maxed)
    protected int level;
    // How much gold to buy this tower
    protected int cost;
    // How much gold to upgrade to the next level
    protected int upgradeCost;
    // The name of the tower (Cannon, Sniper, Freeze, Mage)
    protected String name;
    // The main color of the tower
    protected Color baseColor;
    // A bright color for details
    protected Color accentColor;
    // Is the tower selected (for upgrading or selling)
    protected boolean selected;
    // What angle is the tower pointing
    protected double aimAngle = 0;
    // The enemy the tower is currently shooting at
    protected Enemy currentTarget;

    protected int moneySpent = 0;

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
        this.moneySpent = cost;
    }

    @Override
    public void update() {
        // Count down the fire cooldown
        // When it reaches 0, the tower can fire again
        if (fireCooldown > 0) fireCooldown--;
    }

    /**
     * Finds the best target from all enemies in range.
     * The best target is the one furthest along the path (closest to the end).
     * This makes sure the tower prioritizes enemies that are about to escape.
     */
    public Enemy acquireTarget(List<Enemy> enemies) {
        Enemy best = null;
        double bestProgress = -1;
        // Loop through all enemies
        for (Enemy e : enemies) {
            if (!e.isActive()) continue; // Skip dead enemies
            // Check if the enemy is in range
            if (distanceTo(e) <= range) {
                // If this enemy is further along the path, pick it
                if (e.getPathProgress() > bestProgress) {
                    bestProgress = e.getPathProgress();
                    best = e;
                }
            }
        }
        currentTarget = best;
        // Point the tower barrel at the target
        if (best != null) {
            aimAngle = Math.atan2(best.getCenterY() - getCenterY(),
                                  best.getCenterX() - getCenterX());
        }
        return best;
    }

    // Check if the tower can fire at its current target
    // Returns true if cooldown is done, has a target, and the target is still alive
    public boolean canFire() {
        return fireCooldown == 0 && currentTarget != null && currentTarget.isActive();
    }

    // Reset the fire cooldown so the tower can shoot again after its fire rate
    public void resetCooldown() {
        fireCooldown = fireRate;
    }

    // Create the projectile for this tower
    // Each tower type creates different projectiles (see subclasses)
    public abstract Projectile createProjectile();

    // Check if this tower can still be upgraded (max level is 5)
    public boolean canUpgrade() { return level < 5; }

    // Upgrade this tower to the next level, making it stronger
    // Increases damage, range, and fire rate based on the tower type
    public void upgrade() {
        if (!canUpgrade()) return; // Already at max level
        level++;
        // Apply level-specific upgrades (defined in subclasses)
        applyUpgrade();
        // Calculate the new upgrade cost for next level
        switch (level) {
            case 5:
                // At max level, no further upgrades possible
                moneySpent += upgradeCost;
                upgradeCost = Integer.MAX_VALUE; // maxed out
                break;
            case 4:
                // Tier 5 is the prestige upgrade — 2.7x scaling PLUS a flat 20k surcharge
                moneySpent += upgradeCost;
                upgradeCost = (int)(upgradeCost * 2.7) + 20_000;
                break;
            default:
                // Each subsequent upgrade costs 50% more (×2.7 = original ×1.8 × 1.5)
                moneySpent += upgradeCost;
                upgradeCost = (int)(upgradeCost * 2.7);
                break;
        }
    }

    // Apply the stat improvements for this level (defined in subclasses)
    protected abstract void applyUpgrade();

    // ── Drawing the tower ──
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

        // Show the range circle when the tower is selected
        if (selected) {
            if (range == Double.MAX_VALUE) {
                // Infinite range (Sniper tower) — show entire map
                g.setColor(new Color(100,255,100,25));
                g.fillRect(0,0,game.util.GameMap.WIDTH,game.util.GameMap.HEIGHT);
                g.setColor(new Color(100,255,100,100));
                g.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{4,4},0));
                g.drawRect(2,2,game.util.GameMap.WIDTH-4,game.util.GameMap.HEIGHT-4);
            } else {
                // Regular range — show a circle
                g.setColor(new Color(255,255,255,30));
                g.fillOval((int)(getCenterX()-range),(int)(getCenterY()-range),(int)(range*2),(int)(range*2));
                g.setColor(new Color(255,255,255,80));
                g.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{4,4},0));
                g.drawOval((int)(getCenterX()-range),(int)(getCenterY()-range),(int)(range*2),(int)(range*2));
            }
        }

        // Draw each part of the tower
        drawBase(g);      // The main body
        drawBarrel(g);    // The barrel that rotates
        drawLevelIndicator(g); // The level number/badge

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

    // Draw the barrel pointing at the target
    protected void drawBarrel(Graphics2D g2d) {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();
        int barrelLen = getBarrelLength();
        int barrelW = getBarrelWidth();

        // Rotate the barrel to aim at the target
        Graphics2D rotated = (Graphics2D) g2d.create();
        rotated.rotate(aimAngle, cx, cy);
        rotated.setColor(accentColor);
        rotated.fillRoundRect(cx, cy - barrelW/2, barrelLen, barrelW, 3, 3);
        rotated.dispose();
    }

    // How long the barrel should be
    protected int getBarrelLength() { return width / 2 + 4; }
    // How thick the barrel should be
    protected int getBarrelWidth() { return 6; }

    // Draw a badge showing the tower's level (1-5)
    protected void drawLevelIndicator(Graphics2D g2d) {
        if (level == 1) return; // Don't show badge for level 1

        // Colour scale: blue → purple → gold → red-gold
        Color levelColor = switch (level) {
            case 2 -> new Color(180, 180, 255); // Blue for level 2
            case 3 -> new Color(255, 215, 0);   // Gold for level 3
            case 4 -> new Color(255, 140, 0);   // Orange for level 4
            case 5 -> new Color(255, 60, 60);   // Red for level 5 (max)
            default -> Color.WHITE;
        };

        int badgeY = (int)y - 8;
        int badgeR = 6 + (level - 2);          // Badge size grows with level
        int badgeDiam = badgeR * 2;
        int bx = (int)x + width/2 - badgeR;

        // Outer glow for high-level towers (Lv4+)
        if (level >= 4) {
            g2d.setColor(new Color(levelColor.getRed(), levelColor.getGreen(), levelColor.getBlue(), 70));
            g2d.fillOval(bx - 4, badgeY - badgeR - 4, badgeDiam + 8, badgeDiam + 8);
        }
        // Badge circle
        g2d.setColor(levelColor);
        g2d.fillOval(bx, badgeY - badgeR, badgeDiam, badgeDiam);
        // Badge border
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.setStroke(new BasicStroke(level >= 4 ? 1.5f : 1f));
        g2d.drawOval(bx, badgeY - badgeR, badgeDiam, badgeDiam);
        // Level number inside the badge
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 8 + (level - 2)));
        FontMetrics fm = g2d.getFontMetrics();
        String lvlStr = String.valueOf(level);
        g2d.drawString(lvlStr,
            bx + badgeR - fm.stringWidth(lvlStr)/2,
            badgeY - badgeR + badgeDiam/2 + fm.getAscent()/2 - 1);
    }

    // Draw the range circle when tower is selected
    // For infinite range, shows the entire game map instead
    public void drawRangeCircle(Graphics2D g2d) {
        if (range == Double.MAX_VALUE) {
            // Infinite range — highlight entire map
            g2d.setColor(new Color(100,255,100,60));
            g2d.fillRect(0,0,game.util.GameMap.WIDTH, game.util.GameMap.HEIGHT);
            g2d.setColor(new Color(100,255,100,160));
            g2d.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{6,4},0));
            g2d.drawRect(2,2,game.util.GameMap.WIDTH-4,game.util.GameMap.HEIGHT-4);
            return;
        }
        // Regular range circle
        g2d.setColor(new Color(200,230,255,40));
        g2d.fillOval((int)(getCenterX()-range),(int)(getCenterY()-range),(int)(range*2),(int)(range*2));
        g2d.setColor(new Color(200,230,255,120));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval((int)(getCenterX()-range),(int)(getCenterY()-range),(int)(range*2),(int)(range*2));
    }

    // ── Getters: Information about this tower ──
    public double getRange()      { return range; }
    public double getDamage()     { return damage; }
    public int getCost()          { return cost; }  // Gold to buy
    public int getUpgradeCost()   { return upgradeCost; }  // Gold to upgrade
    public String getName()       { return name; }  // "Cannon", "Sniper", etc
    public int getLevel()         { return level; }  // 1-5
    public boolean isSelected()   { return selected; }
    public void setSelected(boolean s) { this.selected = s; }
    public String getDescription(){ return name + " Lv." + level; }
    public int getMoneySpent() { return this.moneySpent;}
    // Get stats like damage, range, etc for display
    public abstract String getStatLine();
    // Get description of what the upgrade does
    public abstract String getUpgradeEffect();
    // Calculate the barrel tip position for projectile spawning
    public Point2D.Double getMuzzlePoint() {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();
        int barrelLen = getBarrelLength();
        // Calculate a point at the end of the barrel based on aim angle
        return new Point2D.Double(cx + barrelLen * Math.cos(aimAngle),
                                  cy + barrelLen * Math.sin(aimAngle));
    }
}