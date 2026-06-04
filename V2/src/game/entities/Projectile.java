package game.entities;

import java.awt.*;

/**
 * Projectile fired by towers. Travels toward a target enemy.
 */
public class Projectile extends Entity {
    private double vx, vy;
    private double damage;
    private double speed;
    private Enemy target;
    private Color color;
    private int size;
    private boolean isSplash;
    private double splashRadius;
    private boolean isSlow;
    private double slowMultiplier;
    private int slowDuration;
    private float trail = 0f;

    public Projectile(double x, double y, Enemy target, double damage, double speed,
                      Color color, int size, boolean isSplash, double splashRadius,
                      boolean isSlow, double slowMultiplier, int slowDuration) {
        super(x, y, size, size);
        this.target = target;
        this.damage = damage;
        this.speed = speed;
        this.color = color;
        this.size = size;
        this.isSplash = isSplash;
        this.splashRadius = splashRadius;
        this.isSlow = isSlow;
        this.slowMultiplier = slowMultiplier;
        this.slowDuration = slowDuration;
        updateVelocity();
    }

    private void updateVelocity() {
        if (target == null || !target.isActive()) {
            active = false;
            return;
        }
        double dx = target.getCenterX() - x;
        double dy = target.getCenterY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1) {
            active = false;
            return;
        }
        vx = (dx / dist) * speed;
        vy = (dy / dist) * speed;
    }

    @Override
    public void update() {
        if (!active) return;

        if (target == null || !target.isActive()) {
            active = false;
            return;
        }

        updateVelocity();
        x += vx;
        y += vy;
        trail = 1.0f;

        // Check hit
        if (distanceTo(target) < speed + size) {
            active = false;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!active) return;
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Glow
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g.fillOval((int)(x - size), (int)(y - size), size * 3, size * 3);

        // Core
        g.setColor(color);
        g.fillOval((int)x, (int)y, size, size);
        g.setColor(Color.WHITE);
        g.fillOval((int)x + size/4, (int)y + size/4, size/3, size/3);

        g.dispose();
    }

    public boolean hasHit() {
        return !active;
    }

    public double getDamage() { return damage; }
    public Enemy getTarget() { return target; }
    public boolean isSplash() { return isSplash; }
    public double getSplashRadius() { return splashRadius; }
    public boolean isSlow() { return isSlow; }
    public double getSlowMultiplier() { return slowMultiplier; }
    public int getSlowDuration() { return slowDuration; }
}
