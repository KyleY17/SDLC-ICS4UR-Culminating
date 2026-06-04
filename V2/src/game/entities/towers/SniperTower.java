package game.entities.towers;
import game.entities.*;
import java.awt.*;

public class SniperTower extends Tower {
    public SniperTower(double x, double y) {
        // upgradeCost 100 → bumped 50% in Tower constructor
        super(x, y, 30, Double.MAX_VALUE, 120, 120, 150, 100,
              "Sniper", new Color(40,100,60), new Color(20,60,35));
    }

    @Override
    protected void applyUpgrade() {
        switch (level) {
            case 2 -> { damage *= 2.0; }
            case 3 -> { damage *= 2.0; fireRate = Math.max(60, fireRate - 20); }
            // Tier 4: triple damage, much faster firing
            case 4 -> { damage *= 3.0; fireRate = Math.max(35, fireRate - 25); }
            // Tier 5: devastating single-shot — x4 damage, near-instant reload
            case 5 -> { damage *= 4.0; fireRate = Math.max(20, fireRate - 15); }
        }
    }

    @Override
    public Projectile createProjectile() {
        var m = getMuzzlePoint();
        // Higher levels fire faster, more visible projectile
        double speed = 14.0 + level * 2;
        int size = 4 + level;
        Color projCol = switch (level) {
            case 4 -> new Color(255, 200, 50);
            case 5 -> new Color(255, 80, 80);
            default -> new Color(100, 255, 100);
        };
        return new Projectile(m.x, m.y, currentTarget, damage, speed,
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