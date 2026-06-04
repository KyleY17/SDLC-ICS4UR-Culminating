package game.entities.towers;

import game.entities.*;
import java.awt.*;

public class CannonTower extends Tower {
    public CannonTower(double x, double y) {
        // upgradeCost 75 → bumped 50% to 113 in Tower constructor (75 * 1.5 = 112.5 → 113)
        super(x, y, 36, 110, 45, 50, 100, 75,
              "Cannon", new Color(80, 80, 100), new Color(50, 50, 70));
    }

    @Override
    protected void applyUpgrade() {
        switch (level) {
            case 2 -> { damage *= 1.5; range += 15; fireRate = Math.max(20, fireRate - 8); }
            case 3 -> { damage *= 1.5; fireRate = Math.max(15, fireRate - 5); }
            // Tier 4: massive damage spike + splash radius boost
            case 4 -> { damage *= 2.0; range += 20; fireRate = Math.max(10, fireRate - 5); }
            // Tier 5: devastating — double damage again, max speed
            case 5 -> { damage *= 2.5; range += 15; fireRate = Math.max(6, fireRate - 4); }
        }
    }

    @Override
    public Projectile createProjectile() {
        var muzzle = getMuzzlePoint();
        // Higher tiers get larger, more powerful projectiles
        int projSize  = level >= 4 ? 14 : (level == 3 ? 10 : 8);
        int splashRad = 45 + level * 12;
        return new Projectile(muzzle.x, muzzle.y, currentTarget,
                              damage, 5.0,
                              new Color(50, 50, 60), projSize,
                              true, splashRad,
                              false, 1.0, 0);
    }

    @Override
    protected void drawBase(Graphics2D g2d) {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval((int)x + 3, (int)y + 3, width, height);

        // Base colour escalates with level
        Color baseCol = switch (level) {
            case 2 -> new Color(100, 100, 140);
            case 3 -> new Color(140, 120, 180);
            case 4 -> new Color(180, 100, 200);
            case 5 -> new Color(220, 80, 80);
            default -> baseColor;
        };

        g2d.setColor(baseCol);
        g2d.fillOval((int)x, (int)y, width, height);

        // Armor plating
        g2d.setColor(new Color(60, 60, 80));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval((int)x, (int)y, width, height);
        g2d.drawOval((int)x + 3, (int)y + 3, width - 6, height - 6);

        // Armor ridges — more for higher levels
        int ridges = level + 2;
        g2d.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < ridges; i++) {
            double angle = Math.PI * 2 * i / ridges;
            int x1 = cx + (int)(5 * Math.cos(angle));
            int y1 = cy + (int)(5 * Math.sin(angle));
            int x2 = cx + (int)(10 * Math.cos(angle));
            int y2 = cy + (int)(10 * Math.sin(angle));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Inner highlight
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.fillOval((int)x + 4, (int)y + 4, width/3, height/3);
    }

    @Override
    protected void drawBarrel(Graphics2D g2d) {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();
        int barrelLen = getBarrelLength();
        int barrelW = getBarrelWidth() + level;

        Graphics2D rotated = (Graphics2D) g2d.create();
        rotated.rotate(aimAngle, cx, cy);

        // Shadow
        rotated.setColor(new Color(0, 0, 0, 80));
        rotated.fillRoundRect(cx + 2, cy - barrelW/2 + 2, barrelLen, barrelW, 3, 3);

        Color barrelCol = switch (level) {
            case 2 -> new Color(80, 80, 120);
            case 3 -> new Color(120, 100, 160);
            case 4 -> new Color(180, 80, 180);
            case 5 -> new Color(220, 60, 60);
            default -> accentColor;
        };

        rotated.setColor(barrelCol);
        rotated.fillRoundRect(cx, cy - barrelW/2, barrelLen, barrelW, 4, 4);

        // Muzzle brake
        rotated.setColor(new Color(100, 100, 140));
        rotated.fillOval(cx + barrelLen - 6, cy - barrelW/2 - 3, barrelW + 6, barrelW + 6);

        rotated.dispose();
    }

    @Override protected int getBarrelLength() { return width / 2 + 4 + level * 2; }
    @Override protected int getBarrelWidth()  { return 6 + level; }

    @Override
    public String getStatLine() {
        return String.format("DMG: %.0f | RNG: %.0f | SPD: %d", damage, range, 60/fireRate);
    }

    @Override
    public String getUpgradeEffect() {
        return switch (level) {
            case 1 -> "+50% dmg, +15 rng";
            case 2 -> "+50% dmg, faster fire";
            case 3 -> "x2 dmg, +20 rng, faster";
            case 4 -> "x2.5 dmg, max fire rate";
            default -> "MAX LEVEL";
        };
    }
}