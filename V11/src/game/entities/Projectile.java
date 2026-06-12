package game.entities;

import java.awt.*;

/**
 * This is a bullet or projectile shot by a tower.
 * It travels towards an enemy and deals damage when it hits.
 */
public class Projectile extends Entity {
    // Speed in X direction
    private double vx;
    // Speed in Y direction
    private double vy;
    // How much damage this does
    private double damage;
    // How fast the projectile travels
    private double speed;
    // The enemy we're shooting at
    private Enemy target;
    // What color to draw this projectile
    private Color color;
    // How big the projectile is
    private int size;
    // Does this projectile hit multiple enemies in an area?
    private boolean isSplash;
    // How big the splash area is
    private double splashRadius;
    // Does this projectile slow enemies?
    private boolean isSlow;
    // How much to slow enemies (0.5 = half speed)
    private double slowMultiplier;
    // How long the slow effect lasts
    private int slowDuration;
    // For drawing a trail behind the projectile
    private float trail = 0f;
    // Starting position for chain projectiles
    private double originX, originY;
    // Is this a chain projectile (for mage tower)?
    private boolean isChain = false;
    // Is this an instant-hit projectile (for visual effects)?
    private boolean instant = false;
    // How many frames this projectile stays alive
    private int lifespan = 1;
    // How many frames until this projectile fades away
    private int fadeTimer = 0;
    // Number of frames to fade (frames at 60fps)
    private static final int FADE_FRAMES = 6;
    // How many frames to wait before showing this projectile (for chain lightning animation)
    private int delayFrames = 0;
    // Random seed for consistent jitter in lightning bolt (prevents recalculating each frame)
    private long jitterSeed = 0;
    private boolean isChainStarter = false;
    // Reference to the tower that shot this
    private Object tower = null;
    // Did this projectile already hit something?
    private boolean hitRegistered = false;
    // Should this projectile render? (for invisible chain starters)
    private boolean invisible = false;

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
        this.originX = x;
        this.originY = y;
        updateVelocity();
    }

    public Projectile setChain(double originX, double originY) {
        this.isChain = true;
        this.originX = originX;
        this.originY = originY;
        this.fadeTimer = FADE_FRAMES;  // Start fade timer for chain
        return this;
    }

    public Projectile setInstant() {
        this.instant = true;
        this.lifespan = 2;  // Live for 2 frames: first to render, second to hit
        if (this.fadeTimer == 0) this.fadeTimer = FADE_FRAMES;  // FIX: ensure fadeTimer is set even if setChain wasn't called first
        return this;
    }

    public Projectile setInvisible() {
        this.invisible = true;
        return this;
    }

    public Projectile setChainStarter(Object towerReference) {
        this.isChainStarter = true;
        this.tower = towerReference;
        return this;
    }

    // Set how many frames to delay before showing this projectile
    public Projectile setDelay(int frames) {
        this.delayFrames = frames;
        this.jitterSeed = System.nanoTime();  // Generate seed once, not per frame
        return this;
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
        // Chain fade logic runs regardless of active state
        if (isChain && fadeTimer > 0) {
            fadeTimer--;
            if (fadeTimer <= 0) active = false;
            return;  // Chain projectiles don't move or do anything else
        }
        
        if (!active) return;

        // Count down the delay before showing this projectile
        if (delayFrames > 0) {
            delayFrames--;
            return;  // Don't update anything until delay is done
        }

        if (instant) return;  // visual-only: skip all movement/hit logic

        if (target == null || !target.isActive()) { active = false; return; }
        updateVelocity();
        x += vx;
        y += vy;
        trail = 1.0f;
        if (distanceTo(target) < speed + size) active = false;
    }

    @Override
    public void draw(Graphics2D g2d) {
        // Don't draw if invisible or still in delay period
        if (invisible || delayFrames > 0) return;
        
        boolean shouldDraw = active || (isChain && fadeTimer > 0);
        if (!shouldDraw) return;

        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fade based on remaining fadeTimer
        float alpha = 1.0f;
        if (isChain) {
            alpha = fadeTimer > 0 ? (float) fadeTimer / FADE_FRAMES : 0f;
        }

        if (isChain) {
            int alphaValue = (int)(255 * alpha);  // Full opacity
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaValue));
            g.setStroke(new BasicStroke(4.0f));  // Thicker stroke
            drawLightning(g, x, y, originX, originY, 12);  // More segments for visible jagged pattern
        }

        int glowAlpha  = (int)(80  * alpha);
        int coreAlpha  = (int)(255 * alpha);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), glowAlpha));
        g.fillOval((int)(x - size), (int)(y - size), size * 3, size * 3);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), coreAlpha));
        g.fillOval((int)x, (int)y, size, size);
        g.setColor(new Color(255, 255, 255, coreAlpha));
        g.fillOval((int)x + size/4, (int)y + size/4, size/3, size/3);

        g.dispose();
    }

    private void drawLightning(Graphics2D g, double x1, double y1, double x2, double y2, int segments) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.hypot(dx, dy);
        if (length < 1) return;
        double maxJitter = Math.min(2.5, Math.max(1.0, length / 25.0));

        // Use seed-based random instead of Math.random() for consistent jitter
        java.util.Random rng = new java.util.Random(jitterSeed);
        
        // Jitter along the perpendicular axis
        double nx = -dy / length;
        double ny =  dx / length;

        for (int i = 0; i < segments; i++) {
            double t1 = (double) i / segments;
            double t2 = (double) (i + 1) / segments;
            double sx1 = x1 + dx * t1;
            double sy1 = y1 + dy * t1;
            double sx2 = x1 + dx * t2;
            double sy2 = y1 + dy * t2;
            double jitter = (rng.nextDouble() - 0.5) * maxJitter;
            g.drawLine((int)sx1, (int)sy1,
                       (int)(sx2 + jitter * nx),
                       (int)(sy2 + jitter * ny));
        }
    }

    public boolean hasHit() {
        // Instant chain projectiles only hit after they've lived their lifespan
        if (instant) return lifespan <= 0;
        return !active;
    }

    public static Projectile visualChain(double fromX, double fromY,
        double toX,   double toY,
        Color color) {
        return visualChain(fromX, fromY, toX, toY, color, 0);
    }

    public static Projectile visualChain(double fromX, double fromY,
        double toX,   double toY,
        Color color,  int delayFrames) {
        Projectile p = new Projectile(toX, toY, null, 0, 0,
            color, 6,
            false, 0, false, 1.0, 0);
        p.originX  = fromX;
        p.originY  = fromY;
        p.isChain  = true;
        p.instant  = true;
        p.damage   = 0;
        p.fadeTimer = FADE_FRAMES;
        p.delayFrames = delayFrames;
        p.jitterSeed = System.nanoTime();  // Generate once per bolt
        return p;
        }

    public double getDamage() { return damage; }
    public Enemy getTarget() { return target; }
    public boolean isSplash() { return isSplash; }
    public double getSplashRadius() { return splashRadius; }
    public boolean isSlow() { return isSlow; }
    public double getSlowMultiplier() { return slowMultiplier; }
    public int getSlowDuration() { return slowDuration; }
    public boolean isChainStarter() { return isChainStarter; }
    public Object getTower() { return tower; }
}