package game.entities.towers;

import game.entities.*;
import java.awt.*;

// The Slow (Freeze) tower shoots projectiles that slow down enemies
public class SlowTower extends Tower {
    // How much to slow enemies (0.5 = half speed)
    private double slowAmount;

    // Create a slow tower
    public SlowTower(double x, double y) {
        super(x, y, 32, 60, 10, 60, 120, 90,
              "Freeze", new Color(50, 150, 200), new Color(20, 100, 160));
        // Start with a slow of 0.45 (55% speed)
        this.slowAmount = 0.0f;
    }

    // Make the tower slower when leveling up
    @Override
    protected void applyUpgrade() {
        switch (level) {
            // Level 2: 
            case 2 -> fireRate = Math.max(30, fireRate - 10);
            // Level 3: 
            case 3 -> { 
                        range += 10; 
                        fireRate = Math.max(20, fireRate - 10); 
                        }
            // Level 4: 
            case 4 -> { 
                        range += 10; 
                        damage += 15; 
                        }

            // Level 5 (MAXED): Absolute zero or something
            case 5 -> { 
                        range += 20; 
                        damage += 25; 
                        fireRate = Math.max(15, fireRate - 5); 
                        }
        }
    }

    // Create the projectile this tower shoots
    @Override
    public Projectile createProjectile() {
        var muzzle = getMuzzlePoint();
        // Longer slow duration at higher levels
        int slowDuration = 20 + level * 20;
        return new Projectile(muzzle.x, muzzle.y, currentTarget,
                              damage, 100,
                              new Color(150, 220, 255), 7,
                              true, 30,
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