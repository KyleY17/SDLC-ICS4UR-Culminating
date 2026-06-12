package game.entities.towers;

import game.entities.Tower;
import java.awt.*;

/**
 * Central tower factory.
 *
 * Converts TowerType values (used by the UI/shop and the game placement logic)
 * into concrete Tower instances.
 */
public class TowerFactory {

    // The different types of towers we can make
    public enum TowerType { CANNON, SNIPER, SLOW, MAGE }

    /**
     * Creates a tower at the given position.
     *
     * The caller typically passes tile-centered pixel coordinates.
     */
    public static Tower create(TowerType type, double x, double y) {

        return switch (type) {
            // Make a cannon tower
            case CANNON -> new CannonTower(x, y);
            // Make a sniper tower
            case SNIPER -> new SniperTower(x, y);
            // Make a slow tower
            case SLOW   -> new SlowTower(x, y);
            // Make a mage tower
            case MAGE   -> new MageTower(x, y);
        };
    }

    /** Base gold cost (before difficulty multipliers). */
    public static int getCost(TowerType type) {
        return switch (type) {
            case CANNON -> 100;
            case SNIPER -> 150;
            case SLOW   -> 120;
            case MAGE   -> 250;
        };
    }

    // Get the name of a tower type
    public static String getName(TowerType type) {
        return switch (type) {
            case CANNON -> "Cannon";
            case SNIPER -> "Sniper";
            case SLOW   -> "Freeze";
            case MAGE   -> "Mage";
        };
    }

    /** Short description shown in the tower shop UI. */
    public static String getDescription(TowerType type) {
        return switch (type) {
            case CANNON -> "Balanced damage with splash";
            case SNIPER -> "Long range, high single-target dmg";
            case SLOW   -> "Freeze enemies on hit. Enemies can only be frozen 3 times before they become immune.";
            case MAGE   -> "Chain lightning — jumps between enemies";
        };
    }

    // Get the color of a tower type
    public static Color getColor(TowerType type) {
        return switch (type) {
            case CANNON -> new Color(80, 80, 100);
            case SNIPER -> new Color(40, 100, 60);
            case SLOW   -> new Color(50, 150, 200);
            case MAGE   -> new Color(60, 20, 100);
        };
    }
}