package game.entities.towers;

import game.entities.*;
import java.awt.*;
import java.util.List;


/**
 * Slow/Freeze tower.
 *
 * Behavior:
 * - Prioritizes enemies by proximity (closest enemy within range).
 * - Shoots splash-less freeze projectiles that apply a slow effect for a duration.
 * - Leveling reduces fire cadence and/or expands effective stats (range/damage) to
 *   make crowd control stronger as the game progresses.
 */
public class SlowTower extends Tower {

    // How much to slow enemies (0.5 = half speed)
    private double slowAmount;

    public SlowTower(double x, double y) {

        super(x, y, 32, 60, 10, 60, 120, 90,
              "Freeze", new Color(50, 150, 200), new Color(20, 100, 160));
        // Start with a slow multiplier.
        // 0.0 means "freeze to 0 speed"; the projectile will clamp slow stacking logic
        // in Enemy (max freeze count).
        this.slowAmount = 0.0f;

    }

    /**
     * Chooses the closest enemy in range.
     *
     * This differs from other towers in the sense that it tries to freeze the nearest
     * threat first (rather than the furthest-along-the-path).
     */
    @Override
    public Enemy acquireTarget(List<Enemy> enemies) {

        Enemy best = null;
        int close = 60;
        for (Enemy e : enemies) {
            if (!e.isActive()) continue;
            if (distanceTo(e) <= range) {
                if (e.distanceTo(this) < close){
                    close = (int) e.distanceTo(this.x, this.y);
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
    /**
     * Applies level-specific stat changes.
     *
     * Note: despite the method name "applyUpgrade", different towers scale different
     * stats at different levels (SlowTower focuses on range/damage and adjusts fire rate).
     */

    @Override
    protected void applyUpgrade() {
        switch (level) {
            // Level 2: 
            case 2 -> fireRate = Math.max(30, fireRate - 10);
            // Level 3: 
            case 3 -> { range += 10; fireRate = Math.max(20, fireRate - 10); }
            // Level 4: 
            case 4 -> { range += 10; damage += 15; }
            // Level 5 (MAXED): Absolute zero or something
            case 5 -> { range += 20; damage += 35; fireRate = Math.max(15, fireRate - 5); }
        }
    }

    /**
     * Creates the projectile that applies slow on hit.
     *
     * The projectile uses the Tower's current target, and encodes:
     * - slow multiplier (how much to reduce speed)
     * - slow duration in frames
     */
    @Override
    public Projectile createProjectile() {

        var muzzle = getMuzzlePoint();
        // Longer slow duration at higher levels
        int slowDuration = 20 + level * 20;
        return new Projectile(muzzle.x, muzzle.y, currentTarget,
                              damage, 100,
                              new Color(150, 220, 255), 7,
                              true, 40,
                              true, slowAmount, slowDuration);
    }

    @Override
    protected void drawBase(Graphics2D g2d) {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval((int)x + 3, (int)y + 3, width, height);

        Color baseCol = switch (level) {
            case 2 -> new Color(80, 180, 220);
            case 3 -> new Color(120, 200, 255);
            case 4 -> new Color(160, 230, 255);
            case 5 -> new Color(200, 245, 255);  // near-white ice
            default -> baseColor;
        };

        g2d.setColor(baseCol);
        g2d.fillOval((int)x, (int)y, width, height);

        // Ice crystal border
        g2d.setColor(new Color(150, 220, 255, 150));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval((int)x, (int)y, width, height);

        // Crystal spikes — more at higher levels
        int spikes = 4 + level * 2;
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(150, 220, 255, 180));
        for (int i = 0; i < spikes; i++) {
            double angle = Math.PI * 2 * i / spikes;
            int spikeLen = 8 + level * 2;
            int x1 = cx + (int)(10 * Math.cos(angle));
            int y1 = cy + (int)(10 * Math.sin(angle));
            int x2 = cx + (int)((10 + spikeLen) * Math.cos(angle));
            int y2 = cy + (int)((10 + spikeLen) * Math.sin(angle));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Frosted glass inner glow
        g2d.setColor(new Color(200, 240, 255, 80));
        g2d.fillOval((int)x + 3, (int)y + 3, width - 6, height - 6);
    }


    @Override protected int getBarrelLength() { return width / 2 + 6 + level * 2; }
    @Override protected int getBarrelWidth()  { return 5 + level; }

    @Override
    public String getStatLine() {
        return String.format("SLOW: %.0f%% | RNG: %.0f | DMG: %.0f", (1 - slowAmount)*100, range, damage);
    }

    @Override
    public String getUpgradeEffect() {
        return switch (level) {
            case 1 -> "Stronger slow, +rng";
            case 2 -> "Max slow, +rng";
            case 3 -> "Deep freeze +rng, +dmg";
            case 4 -> "Near-total freeze, huge rng";
            default -> "MAX LEVEL";
        };
    }
}