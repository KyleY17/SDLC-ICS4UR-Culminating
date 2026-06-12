package game.entities;

import java.awt.Graphics2D;

/**
 * This is the base class for all things in the game (towers and enemies).
 * It keeps track of basic stuff like position, size, and whether it's active.
 */
public abstract class Entity {
    // X and Y position (left and top)
    protected double x, y;
    // How wide and tall this thing is
    protected int width, height;
    // Is this thing still in the game?
    protected boolean active;

    // Create an entity at a position with a size
    public Entity(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        // It's active when we first make it
        this.active = true;
    }

    // Update the entity (towers and enemies override this)
    public abstract void update();
    // Draw the entity on the screen (towers and enemies override this)
    public abstract void draw(Graphics2D g2d);

    // Get the X position
    public double getX() { return x; }
    // Get the Y position
    public double getY() { return y; }
    // Get the width
    public int getWidth() { return width; }
    // Get the height
    public int getHeight() { return height; }
    // Is this thing still active?
    public boolean isActive() { return active; }
    // Turn it on or off
    public void setActive(boolean active) { this.active = active; }

    // Get the X position of the center
    public double getCenterX() { return x + width / 2.0; }
    // Get the Y position of the center
    public double getCenterY() { return y + height / 2.0; }

    // Calculate how far away another entity is
    public double distanceTo(Entity other) {
        double dx = this.getCenterX() - other.getCenterX();
        double dy = this.getCenterY() - other.getCenterY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Calculate how far away a point is
    public double distanceTo(double px, double py) {
        double dx = this.getCenterX() - px;
        double dy = this.getCenterY() - py;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
