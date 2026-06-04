package game.entities.towers;

import game.entities.*;
import java.awt.*;

public class SlowTower extends Tower {
    private double slowAmount;

    public SlowTower(double x, double y) {
        // upgradeCost 90 → bumped 50% in Tower constructor
        super(x, y, 32, 130, 10, 60, 120, 90,
              "Freeze", new Color(50, 150, 200), new Color(20, 100, 160));
        this.slowAmount = 0.45;
    }

    @Override
    protected void applyUpgrade() {
        switch (level) {
            case 2 -> { slowAmount = Math.max(0.2, slowAmount - 0.1); range += 20; fireRate = Math.max(30, fireRate - 10); }
            case 3 -> { slowAmount = Math.max(0.1, slowAmount - 0.1); range += 20; fireRate = Math.max(20, fireRate - 10); }
            // Tier 4: near-total freeze + AoE slow (represented by much stronger slow + range)
            case 4 -> { slowAmount = Math.max(0.05, slowAmount - 0.05); range += 30; damage += 15; }
            // Tier 5: absolute freeze — slowAmount ~0.02, massive range
            case 5 -> { slowAmount = 0.02; range += 40; damage += 25; fireRate = Math.max(15, fireRate - 5); }
        }
    }

    @Override
    public Projectile createProjectile() {
        var muzzle = getMuzzlePoint();
        int slowDuration = 60 + level * 20;
        return new Projectile(muzzle.x, muzzle.y, currentTarget,
                              damage, 7.0,
                              new Color(150, 220, 255), 7,
                              false, 0,
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

    @Override
    protected void drawBarrel(Graphics2D g2d) {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();
        int barrelLen = getBarrelLength();
        int barrelW = getBarrelWidth() + level;

        Graphics2D rotated = (Graphics2D) g2d.create();
        rotated.rotate(aimAngle, cx, cy);

        rotated.setColor(new Color(0, 0, 0, 60));
        rotated.fillRoundRect(cx + 1, cy - barrelW/2 + 1, barrelLen, barrelW, 3, 3);

        Color barrelCol = switch (level) {
            case 2 -> new Color(60, 160, 200);
            case 3 -> new Color(100, 200, 255);
            case 4 -> new Color(150, 230, 255);
            case 5 -> new Color(220, 250, 255);
            default -> accentColor;
        };

        rotated.setColor(barrelCol);
        rotated.fillRoundRect(cx, cy - barrelW/2, barrelLen, barrelW, 3, 3);

        // Icy tips
        rotated.setColor(new Color(150, 220, 255, 150));
        int tipSpikes = level;
        for (int i = 0; i < tipSpikes; i++) {
            int offset = (i - tipSpikes/2) * 3;
            rotated.fillPolygon(
                new int[]{cx + barrelLen, cx + barrelLen + 4, cx + barrelLen},
                new int[]{cy - 3 + offset, cy + offset, cy + 3 + offset},
                3
            );
        }

        rotated.dispose();
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