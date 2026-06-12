package game.entities.towers;
import game.entities.*;
import java.awt.*;

/**
 * Sniper tower.
 *
 * Design:
 * - Has effectively infinite range 
 * - Focuses on single-target damage with a faster/stronger cadence as it levels.
 * - Visuals include a multi-ring scope and a level-scaled projectile size.
 */
public class SniperTower extends Tower {

    public SniperTower(double x, double y) {
        super(x, y, 30, Double.MAX_VALUE, 120, 120, 150, 100,

              "Sniper", new Color(40,100,60), new Color(20,60,35));
    }

    /**
     * Adjusts damage and firing cadence for levels 2-5.
     */
    @Override
    protected void applyUpgrade() {


        switch (level) {
            // Level 2: double damage
            case 2 -> { damage *= 1.5; }
            // Level 3: even more damage, faster firing
            case 3 -> { damage *= 1.3; fireRate = Math.max(60, fireRate - 20); }
            // Level 4: triple damage and much faster
            case 4 -> { damage *= 1.3; fireRate = Math.max(35, fireRate - 25); }
            // Level 5 (MAXED): DEVASTATING - almost instant one-shots
            case 5 -> { damage *= 2; fireRate = Math.max(20, fireRate - 15); }
        }
    }

    /**
     * Creates a single-target, instant-moving projectile.

     *
     * Sniper bolts do not splash or slow; they are visualized with a small core and
     * the projectile size grows with tower level.
     */
    @Override
    public Projectile createProjectile() {


        var m = getMuzzlePoint();
        int size = 4 + level;
        Color projCol = switch (level) {
            case 4 -> new Color(255, 200, 50);  // Gold color at level 4
            case 5 -> new Color(255, 80, 80);   // Red color at level 5
            default -> new Color(100, 255, 100); // Green color at lower levels
        };
        return new Projectile(m.x, m.y, currentTarget, damage, 1000,
                              projCol, size, false, 0, false, 1.0, 0);
    }

    @Override
    protected void drawBase(Graphics2D g2d) {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();

        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval((int)x + 3, (int)y + 3, width, height);

        Color baseCol = switch (level) {
            case 2 -> new Color(60, 120, 80);
            case 3 -> new Color(80, 150, 100);
            case 4 -> new Color(40, 120, 150);
            case 5 -> new Color(100, 60, 160);  // deep purple — apex predator
            default -> baseColor;
        };

        g2d.setColor(baseCol);
        g2d.fillOval((int)x, (int)y, width, height);

        // Crosshair — more rings at higher levels
        g2d.setColor(new Color(100, 200, 120));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(cx, cy - 6, cx, cy + 6);
        g2d.drawLine(cx - 6, cy, cx + 6, cy);
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawOval(cx - 4, cy - 4, 8, 8);
        if (level >= 2) g2d.drawOval(cx - 7, cy - 7, 14, 14);
        if (level >= 4) g2d.drawOval(cx - 10, cy - 10, 20, 20);

        g2d.setColor(new Color(150, 255, 150, 100));
        g2d.fillOval((int)x + 2, (int)y + 2, width/3, height/3);
    }

    @Override
    protected void drawBarrel(Graphics2D g2d) {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();
        int barrelLen = getBarrelLength();
        int barrelW = getBarrelWidth();

        Graphics2D rotated = (Graphics2D) g2d.create();
        rotated.rotate(aimAngle, cx, cy);

        rotated.setColor(new Color(0, 0, 0, 80));
        rotated.fillRect(cx + 2, cy - barrelW/2 + 2, barrelLen, barrelW);

        Color barrelCol = switch (level) {
            case 2 -> new Color(40, 100, 60);
            case 3 -> new Color(60, 150, 80);
            case 4 -> new Color(40, 130, 170);
            case 5 -> new Color(130, 80, 200);
            default -> accentColor;
        };

        rotated.setColor(barrelCol);
        rotated.fillRect(cx, cy - barrelW/2, barrelLen, barrelW);

        // Scope body
        int scopeW = 4 + level;
        rotated.setColor(new Color(80, 130, 100));
        rotated.fillRect(cx + barrelLen/2, cy - scopeW/2, barrelLen/3, scopeW);

        // Scope glass
        rotated.setColor(new Color(150, 255, 150, 80));
        rotated.fillRect(cx + barrelLen/2 + 2, cy - scopeW/2 + 1, barrelLen/4, scopeW - 2);

        rotated.dispose();
    }

    @Override protected int getBarrelLength() { return width/2 + 12 + level * 3; }
    @Override protected int getBarrelWidth()  { return 3; }

    @Override
    public String getStatLine() {
        return String.format("DMG: %.0f | RNG: ∞ | SPD: %d", damage, 60/fireRate);
    }

    @Override
    public String getUpgradeEffect() {
        return switch (level) {
            case 1 -> "x2 damage";
            case 2 -> "x2 dmg, faster reload";
            case 3 -> "x3 dmg, much faster";
            case 4 -> "x4 dmg, rapid fire";
            default -> "MAX LEVEL";
        };
    }
}