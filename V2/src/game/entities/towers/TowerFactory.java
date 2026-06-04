package game.entities.towers;

import game.entities.Tower;
import java.awt.*;

public class TowerFactory {
    public enum TowerType { CANNON, SNIPER, SLOW, MAGE }

    public static Tower create(TowerType type, double x, double y) {
        return switch (type) {
            case CANNON -> new CannonTower(x, y);
            case SNIPER -> new SniperTower(x, y);
            case SLOW   -> new SlowTower(x, y);
            case MAGE   -> new MageTower(x, y);
        };
    }

    public static int getCost(TowerType type) {
        return switch (type) {
            case CANNON -> 100;
            case SNIPER -> 150;
            case SLOW   -> 120;
            case MAGE   -> 250;
        };
    }

    public static String getName(TowerType type) {
        return switch (type) {
            case CANNON -> "Cannon";
            case SNIPER -> "Sniper";
            case SLOW   -> "Freeze";
            case MAGE   -> "Mage";
        };
    }

    public static String getDescription(TowerType type) {
        return switch (type) {
            case CANNON -> "Balanced damage with splash";
            case SNIPER -> "Long range, high single-target dmg";
            case SLOW   -> "Slows enemies in range";
            case MAGE   -> "Chain lightning — jumps between enemies";
        };
    }

    public static Color getColor(TowerType type) {
        return switch (type) {
            case CANNON -> new Color(80, 80, 100);
            case SNIPER -> new Color(40, 100, 60);
            case SLOW   -> new Color(50, 150, 200);
            case MAGE   -> new Color(60, 20, 100);
        };
    }
}