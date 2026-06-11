package game.entities.towers;

import game.entities.Enemy;
import game.entities.Projectile;
import game.entities.Tower;
import java.awt.*;
import java.util.*;
import java.util.List;

// The Mage tower shoots lightning that bounces between enemies
public class MageTower extends Tower {
    // How many times the lightning can bounce
    private int chainBounces;
    // How far the lightning can jump
    private double chainRange;
    // How much damage is lost with each bounce (0.65 = loses 35% damage)
    private double chainFalloff;
    // Animation for the orbiting projectile
    private float orbPulse = 0f;

    // Create a mage tower
    public MageTower(double x, double y) {
        super(x, y, 38, 140, 80, 90, 250, 160,
              "Mage", new Color(60, 20, 100), new Color(160, 80, 255));
        // Start with 2 bounces
        this.chainBounces = 2;
        // Lightning can jump 120 pixels
        this.chainRange   = 120;
        // Each bounce loses 35% damage
        this.chainFalloff = 0.65;
    }

    // Make the mage tower stronger when leveling up
    @Override
    protected void applyUpgrade() {
        switch (level) {
            // Level 2: more bounces and range
            case 2 -> { damage *= 1.6; chainBounces = 3; chainRange += 20; }
            // Level 3: even more bounces and faster firing
            case 3 -> { damage *= 1.8; chainBounces = 4; chainRange += 20; fireRate = Math.max(50, fireRate - 15); }
            // Level 4: lots of bounces, very powerful
            case 4 -> { damage *= 2.5; chainBounces = 5; chainRange += 30; fireRate = Math.max(30, fireRate - 20); chainFalloff = 0.75; }
            // Level 5 (MAXED): INSANE bounces, lightning everywhere
            case 5 -> { damage *= 3.0; chainBounces = 7; chainRange += 40; fireRate = Math.max(18, fireRate - 12); chainFalloff = 0.85; }
        }
    }

    // Create the initial lightning projectile
    @Override
    public Projectile createProjectile() {
        var m = getMuzzlePoint();
        return new Projectile(m.x, m.y, currentTarget,
                              damage, 9,
                              getLightningColor(), 8,
                              false, 0, false, 1.0, 0).setChainStarter(this);
    }

    // Find the next enemy to bounce to
    private Enemy findNextTarget(List<Enemy> allEnemies, Enemy current, Set<Enemy> hit) {
        Enemy nextTarget = null;
        double closestDistance = Double.MAX_VALUE;

        // Prevent chaining too far away
        double maxChainDistance = chainRange + 96;

        for (Enemy enemy : allEnemies) {
            // Skip already-hit enemies and inactive ones
            if (hit.contains(enemy) || !enemy.isActive()) continue;

            // Make sure the enemy is within range
            double distance = current.distanceTo(enemy);
            if (distance > maxChainDistance) continue;

            // Find the closest enemy
            if (distance < closestDistance) {
                closestDistance = distance;
                nextTarget = enemy;
            }
        }

        return nextTarget;
    }

    // Apply the chain lightning damage
    public List<Projectile> applyChainDamage(List<Enemy> allEnemies, Enemy startEnemy,
        java.util.function.BiConsumer<Enemy, Double> damageFunc) {
        List<Projectile> visuals = new ArrayList<>();
        Set<Enemy> hit = new HashSet<>();
        Enemy prev = startEnemy;
        if (prev == null || !prev.isActive()) return visuals;
        hit.add(prev);

        double lastX = prev.getCenterX();
        double lastY = prev.getCenterY();
        double dmg = damage;

        // Jump to nearby enemies
        // Delay between each visual lightning bolt (in frames)
        int delayPerBolt = 15;  // 250 ms (0.25 seconds) at 60 FPS - visible chain animation
        for (int i = 0; i < chainBounces; i++) {
            if (!prev.isActive()) break;
            Enemy next = findNextTarget(allEnemies, prev, hit);
            if (next == null) break;
            hit.add(next);
            // Damage decreases with each bounce
            dmg *= chainFalloff;

            // Deal damage to the enemy
            damageFunc.accept(next, dmg);

            // Create a visual lightning bolt with staggered delay
            visuals.add(Projectile.visualChain(
            lastX, lastY,
            next.getCenterX(), next.getCenterY(),
            getLightningColor(),
            (i + 1) * delayPerBolt  // Each bolt appears 8 frames later
            ));

            lastX = prev.getCenterX();
            lastY = prev.getCenterY();
            prev = next;
        }
        return visuals;
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