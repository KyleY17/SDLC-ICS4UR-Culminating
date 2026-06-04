package game.entities.towers;

import game.entities.Enemy;
import game.entities.Projectile;
import game.entities.Tower;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class MageTower extends Tower {

    private int chainBounces;
    private double chainRange;
    private double chainFalloff;
    private float orbPulse = 0f;

    public MageTower(double x, double y) {
        super(x, y, 38, 140, 80, 90, 250, 160,
              "Mage", new Color(60, 20, 100), new Color(160, 80, 255));
        this.chainBounces = 2;
        this.chainRange   = 120;
        this.chainFalloff = 0.65;
    }

    @Override
    protected void applyUpgrade() {
        switch (level) {
            case 2 -> { damage *= 1.6; chainBounces = 3; chainRange += 20; }
            case 3 -> { damage *= 1.8; chainBounces = 4; chainRange += 20; fireRate = Math.max(50, fireRate - 15); }
            case 4 -> { damage *= 2.5; chainBounces = 5; chainRange += 30; fireRate = Math.max(30, fireRate - 20); chainFalloff = 0.75; }
            case 5 -> { damage *= 3.0; chainBounces = 7; chainRange += 40; fireRate = Math.max(18, fireRate - 12); chainFalloff = 0.85; }
        }
    }

    @Override
    public Projectile createProjectile() {
        var m = getMuzzlePoint();
        return new Projectile(m.x, m.y, currentTarget,
                              damage, 9.0,
                              getLightningColor(), 8,
                              false, 0, false, 1.0, 0);
    }

    public List<Projectile> createChainProjectiles(List<Enemy> allEnemies) {
        List<Projectile> chain = new ArrayList<>();
        Set<Enemy> hit = new HashSet<>();

        Enemy prev = currentTarget;
        if (prev == null || !prev.isActive()) return chain;

        double dmg = damage;
        Point2D.Double origin = getMuzzlePoint();

        for (int i = 0; i <= chainBounces; i++) {
            hit.add(prev);
            double px = (i == 0) ? origin.x : prev.getCenterX();
            double py = (i == 0) ? origin.y : prev.getCenterY();

            chain.add(new Projectile(px, py, prev,
                                     dmg, 10.0 + i,
                                     getLightningColor(), 7,
                                     false, 0, false, 1.0, 0));
            dmg *= chainFalloff;

            Enemy next = null;
            double best = Double.MAX_VALUE;
            for (Enemy e : allEnemies) {
                if (!e.isActive() || hit.contains(e)) continue;
                double dx = e.getCenterX() - prev.getCenterX();
                double dy = e.getCenterY() - prev.getCenterY();
                double dist = Math.sqrt(dx*dx + dy*dy);
                if (dist <= chainRange && dist < best) {
                    best = dist;
                    next = e;
                }
            }
            if (next == null) break;
            prev = next;
        }
        return chain;
    }

    private Color getLightningColor() {
        return switch (level) {
            case 4  -> new Color(255, 200, 80);
            case 5  -> new Color(255, 100, 255);
            default -> new Color(160, 80, 255);
        };
    }

    @Override
    protected void drawBase(Graphics2D g2d) {
        orbPulse += 0.08f;
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();

        float glowAlpha = (float)(0.35 + 0.25 * Math.sin(orbPulse));
        Color glowCol = getLightningColor();
        int glowR = 14 + level * 3;
        g2d.setColor(new Color(glowCol.getRed(), glowCol.getGreen(), glowCol.getBlue(),
                               (int)(glowAlpha * 140)));
        g2d.fillOval(cx - glowR, cy - glowR, glowR * 2, glowR * 2);

        g2d.setColor(new Color(0, 0, 0, 70));
        g2d.fillOval((int)x + 3, (int)y + 3, width, height);

        Color baseCol = switch (level) {
            case 2  -> new Color(80, 30, 130);
            case 3  -> new Color(100, 40, 160);
            case 4  -> new Color(130, 50, 180);
            case 5  -> new Color(160, 20, 200);
            default -> baseColor;
        };
        g2d.setColor(baseCol);
        g2d.fillOval((int)x, (int)y, width, height);

        g2d.setColor(getLightningColor());
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawOval((int)x, (int)y, width, height);

        g2d.setColor(new Color(getLightningColor().getRed(),
                               getLightningColor().getGreen(),
                               getLightningColor().getBlue(), 100));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawOval((int)x + 5, (int)y + 5, width - 10, height - 10);

        int runes = 4 + level;
        g2d.setColor(new Color(getLightningColor().getRed(),
                               getLightningColor().getGreen(),
                               getLightningColor().getBlue(), 200));
        for (int i = 0; i < runes; i++) {
            double angle = orbPulse * 0.5 + Math.PI * 2 * i / runes;
            int rx = cx + (int)(10 * Math.cos(angle));
            int ry = cy + (int)(10 * Math.sin(angle));
            g2d.fillOval(rx - 2, ry - 2, 5, 5);
        }

        g2d.setColor(new Color(220, 180, 255, 90));
        g2d.fillOval((int)x + 5, (int)y + 5, width / 3, height / 3);
    }

    @Override
    protected void drawBarrel(Graphics2D g2d) {
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();
        int staffLen = getBarrelLength();

        Graphics2D rotated = (Graphics2D) g2d.create();
        rotated.rotate(aimAngle, cx, cy);

        rotated.setColor(new Color(120, 70, 180));
        rotated.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        rotated.drawLine(cx, cy, cx + staffLen, cy);

        Color tipCol = getLightningColor();
        int orbSize = 8 + (level >= 4 ? 4 : level >= 2 ? 2 : 0);
        rotated.setColor(new Color(tipCol.getRed(), tipCol.getGreen(), tipCol.getBlue(), 80));
        rotated.fillOval(cx + staffLen - orbSize/2 - 2, cy - orbSize/2 - 2, orbSize + 4, orbSize + 4);
        rotated.setColor(tipCol);
        rotated.fillOval(cx + staffLen - orbSize/2, cy - orbSize/2, orbSize, orbSize);
        rotated.setColor(new Color(255, 255, 255, 120));
        rotated.fillOval(cx + staffLen - orbSize/2 + 2, cy - orbSize/2 + 1, orbSize/3, orbSize/3);

        rotated.dispose();
    }

    @Override protected int getBarrelLength() { return width / 2 + 6 + level * 2; }
    @Override protected int getBarrelWidth()  { return 3; }

    public int    getChainBounces() { return chainBounces; }
    public double getChainRange()   { return chainRange; }

    @Override
    public String getStatLine() {
        return String.format("DMG: %.0f | RNG: %.0f | CHAIN: %d", damage, range, chainBounces + 1);
    }

    @Override
    public String getUpgradeEffect() {
        return switch (level) {
            case 1  -> "+60% dmg, 4 targets";
            case 2  -> "+80% dmg, 5 targets";
            case 3  -> "x2.5 dmg, 6 targets, faster";
            case 4  -> "x3 dmg, 8 targets, less falloff";
            default -> "MAX LEVEL";
        };
    }
}