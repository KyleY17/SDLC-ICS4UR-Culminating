package game.util;

/**
 * This sets how hard the game is.
 * Easy = easier enemies and more starting gold
 * Medium = normal
 * Hard = harder enemies and less starting gold
 */
public enum Difficulty {
    // Easy mode
    EASY   ("Easy",   0.75, 0.80, 0.85),
    // Normal mode
    MEDIUM ("Medium", 1.00, 1.00, 1.00),
    // Hard mode
    HARD   ("Hard",   1.40, 1.25, 1.20);

    // The name shown to the player
    public final String label;
    // Multiply enemy health by this (higher = harder)
    public final double enemyHpMult;
    // Multiply enemy speed by this (higher = harder)
    public final double enemySpeedMult;
    // Multiply tower cost by this (higher = more expensive)
    public final double costMult;

    // Create a difficulty setting with these multipliers
    Difficulty(String label, double hp, double spd, double cost) {
        this.label          = label;
        this.enemyHpMult    = hp;
        this.enemySpeedMult = spd;
        this.costMult       = cost;
    }
}
