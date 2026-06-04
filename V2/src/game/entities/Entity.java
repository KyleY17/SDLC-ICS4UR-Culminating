package game.entities;

import java.awt.Graphics2D;

/**
 * Abstract base class for all game entities (towers and enemies).
 * Demonstrates inheritance as required by the assignment.
 */
public abstract class Entity {
    protected double x, y;
    protected int width, height;
    protected boolean active;

    public Entity(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = true;
    }

    public abstract void update();
    public abstract void draw(Graphics2D g2d);

    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }

    public double distanceTo(Entity other) {
        double dx = this.getCenterX() - other.getCenterX();
        double dy = this.getCenterY() - other.getCenterY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceTo(double px, double py) {
        double dx = this.getCenterX() - px;
        double dy = this.getCenterY() - py;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
