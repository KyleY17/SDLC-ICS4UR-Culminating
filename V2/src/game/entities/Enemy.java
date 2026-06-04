package game.entities;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Abstract Enemy class. Specific enemy types extend this.
 */
public abstract class Enemy extends Entity {
    protected double maxHp;
    protected double hp;
    protected double speed;
    protected int reward;       // gold on kill
    protected int damage;       // lives lost when reaching end
    protected List<Point2D.Double> path;
    protected int pathIndex;
    protected double pathProgress; // 0.0–1.0 progress along total path (for sorting)
    protected boolean reachedEnd;
    protected Color primaryColor;
    protected Color secondaryColor;
    protected String typeName;

    // Visual
    protected float flashTimer = 0f;

    public Enemy(double x, double y, int size, double hp, double speed,
                 int reward, int damage, List<Point2D.Double> path,
                 Color primary, Color secondary, String typeName, double speedMultiplier) {
        super(x, y, size, size);
        this.maxHp = hp;
        this.hp = hp;
        this.speed = speed * speedMultiplier;
        this.reward = reward;
        this.damage = damage;
        this.path = path;
        this.pathIndex = 1; // start moving toward index 1
        this.reachedEnd = false;
        this.primaryColor = primary;
        this.secondaryColor = secondary;
        this.typeName = typeName;
        // Snap to first path point
        if (!path.isEmpty()) {
            this.x = path.get(0).x - size / 2.0;
            this.y = path.get(0).y - size / 2.0;
        }
    }

    @Override
    public void update() {
        if (!active || reachedEnd) return;

        if (flashTimer > 0) flashTimer -= 0.1f;

        if (pathIndex >= path.size()) {
            reachedEnd = true;
            active = false;
            return;
        }

        Point2D.Double target = path.get(pathIndex);
        double tx = target.x - width / 2.0;
        double ty = target.y - height / 2.0;
        double dx = tx - x;
        double dy = ty - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        double effectiveSpeed = getEffectiveSpeed();

        if (dist <= effectiveSpeed) {
            x = tx;
            y = ty;
            pathIndex++;
            // Update path progress
            pathProgress = (double) pathIndex / path.size();
        } else {
            x += (dx / dist) * effectiveSpeed;
            y += (dy / dist) * effectiveSpeed;
            pathProgress = ((double)(pathIndex - 1) + (1.0 - dist / getTotalSegmentLength(pathIndex - 1))) / path.size();
        }
    }

    private double getTotalSegmentLength(int fromIndex) {
        if (fromIndex < 0 || fromIndex + 1 >= path.size()) return 1;
        Point2D.Double a = path.get(fromIndex);
        Point2D.Double b = path.get(fromIndex + 1);
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Subclasses can override for slow effects */
    protected double getEffectiveSpeed() {
        return speed * getSlowMultiplier();
    }

    private double slowMultiplier = 1.0;
    private int slowTimer = 0;

    public void applySlow(double multiplier, int duration) {
        if (multiplier < slowMultiplier) {
            slowMultiplier = multiplier;
        }
        slowTimer = Math.max(slowTimer, duration);
    }

    public double getSlowMultiplier() {
        if (slowTimer > 0) {
            slowTimer--;
            if (slowTimer == 0) slowMultiplier = 1.0;
            return slowMultiplier;
        }
        return 1.0;
    }

    public void takeDamage(double amount) {
        hp -= amount;
        flashTimer = 1.0f;
        if (hp <= 0) {
            hp = 0;
            active = false;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!active) return;

        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Flash white on hit
        Color drawColor = primaryColor;
        if (flashTimer > 0.5f) {
            drawColor = Color.WHITE;
        }

        drawBody(g, drawColor);

        // Slow effect overlay
        if (slowTimer > 0) {
            g.setColor(new Color(100, 100, 255, 80));
            g.fillOval((int)x, (int)y, width, height);
        }

        drawHealthBar(g);
        g.dispose();
    }

    protected abstract void drawBody(Graphics2D g2d, Color color);

    protected void drawHealthBar(Graphics2D g2d) {
        int barWidth = width;
        int barHeight = 4;
        int barX = (int) x;
        int barY = (int) y - 8;

        g2d.setColor(new Color(40, 40, 40, 200));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 2, 2);

        double ratio = hp / maxHp;
        Color hpColor = ratio > 0.6 ? new Color(50, 220, 50)
                      : ratio > 0.3 ? new Color(230, 180, 0)
                                    : new Color(220, 50, 50);
        g2d.setColor(hpColor);
        g2d.fillRoundRect(barX, barY, (int)(barWidth * ratio), barHeight, 2, 2);
    }

    // Getters
    public double getHp() { return hp; }
    public double getMaxHp() { return maxHp; }
    public int getReward() { return reward; }
    public int getDamage() { return damage; }
    public boolean hasReachedEnd() { return reachedEnd; }
    public double getPathProgress() { return pathProgress; }
    public String getTypeName() { return typeName; }
}
