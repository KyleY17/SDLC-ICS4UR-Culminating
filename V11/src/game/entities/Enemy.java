package game.entities;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * This is the base class for all types of enemies.
 * Enemies walk along a path and try to reach the end.
 * If they do, the player loses lives.
 */
public abstract class Enemy extends Entity {
    // Maximum health (how much damage it can take)
    protected double maxHp;
    // Current health
    protected double hp;
    // How fast the enemy moves
    protected double speed;
    // How much gold you get when you kill it
    protected int reward;
    // How many lives you lose when it reaches the end
    protected int damage;
    // The path the enemy walks on
    protected List<Point2D.Double> path;
    // Which point on the path we're walking to
    protected int pathIndex;
    // How far along the path (0.0 = start, 1.0 = end)
    protected double pathProgress;
    // Did the enemy reach the end?
    protected boolean reachedEnd;
    // The main color of the enemy
    protected Color primaryColor;
    // A darker color for details
    protected Color secondaryColor;
    // What type is this enemy? (Basic, Fast, Tank, Boss)
    protected String typeName;

    // Make the enemy flash when it gets hit
    protected float flashTimer = 0f;
    // Remember when this enemy was hit by lightning
    private long lastLightningHitTime = 0;

    //Amount of times frozen
    protected int freezeCount = 0;

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
        this.freezeCount = 0;
        // Snap to first path point
        if (!path.isEmpty()) {
            this.x = path.get(0).x - size / 2.0;
            this.y = path.get(0).y - size / 2.0;
        }
    }

    // Update the enemy's position each game frame
    @Override
    public void update() {
        if (!active || reachedEnd) return;

        // Fade out the white flash when the enemy gets hit
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

    // Calculate the distance between two points on the path
    private double getTotalSegmentLength(int fromIndex) {
        if (fromIndex < 0 || fromIndex + 1 >= path.size()) return 1;
        Point2D.Double a = path.get(fromIndex);
        Point2D.Double b = path.get(fromIndex + 1);
        // Calculate the distance using the Pythagorean theorem
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Get the enemy's actual speed, accounting for slow effects
    // Returns the base speed multiplied by any slow effects currently active
    protected double getEffectiveSpeed() {
        return speed * getSlowMultiplier();
    }

    private double slowMultiplier = 1.0;
    private int slowTimer = 0;

    // Apply a slow effect to the enemy (slows its movement)
    // multiplier: how much to slow (0.5 = half speed), duration: how long in frames
    public void applySlow(double multiplier, int duration) {
        // Keep the strongest slow effect
        if (multiplier < slowMultiplier) {
            slowMultiplier = multiplier;
        }
        // Extend the duration if a longer slow is already active
        slowTimer = Math.max(slowTimer, duration);
        // Count how many times this enemy has been frozen (max 3 times)
        this.freezeCount++;
    }

    // Get the current slow multiplier and count down the slow duration
    public double getSlowMultiplier() {
        if (slowTimer > 0) {
            slowTimer--; // Count down one frame
            // When the slow effect runs out, return to normal speed
            if (slowTimer == 0) slowMultiplier = 1.0;
            return slowMultiplier;
        }
        return 1.0; // Normal speed
    }

    // Deal damage to this enemy
    // amount: how much damage to apply
    public void takeDamage(double amount) {
        hp -= amount; // Reduce health
        flashTimer = 1.0f; // Make the enemy flash white briefly
        // If health reaches 0, the enemy dies
        if (hp <= 0) {
            hp = 0;
            active = false; // Mark the enemy as dead (will be removed)
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

    // Draw a health bar above the enemy showing how much health it has
    protected void drawHealthBar(Graphics2D g2d) {
        int barWidth = width;
        int barHeight = 4;
        int barX = (int) x;
        int barY = (int) y - 8; // Draw above the enemy

        // Draw the background (dark)
        g2d.setColor(new Color(40, 40, 40, 200));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 2, 2);

        // Determine color based on health percentage (green → yellow → red)
        double ratio = hp / maxHp;
        Color hpColor = ratio > 0.6 ? new Color(50, 220, 50)   // Green when mostly healthy
                      : ratio > 0.3 ? new Color(230, 180, 0)   // Yellow when damaged
                                    : new Color(220, 50, 50);   // Red when critically low
        // Draw the health bar (fills from left based on health percentage)
        g2d.setColor(hpColor);
        g2d.fillRoundRect(barX, barY, (int)(barWidth * ratio), barHeight, 2, 2);
    }

    // ── Getters: Return information about this enemy ──
    public double getHp() { return hp; }
    public double getMaxHp() { return maxHp; }
    public int getReward() { return reward; }  // Gold earned when this enemy dies
    public int getDamage() { return damage; }  // Lives lost if it reaches the end
    public boolean hasReachedEnd() { return reachedEnd; }
    public double getPathProgress() { return pathProgress; }  // 0.0 = start, 1.0 = end
    public String getTypeName() { return typeName; }  // "Basic", "Fast", "Tank", "Boss"
    public int getFreezeCount() { return freezeCount; }  // How many times it's been frozen

    // Record when this enemy was hit by lightning (for visual effects)
    public void recordLightningHit() {
        this.lastLightningHitTime = System.currentTimeMillis();
    }
    
    // Get the time when this enemy was last hit by lightning
    public long getLastLightningHitTime() {
        return lastLightningHitTime;
    }
}
