package game.util;

/**
 * Difficulty settings affecting enemy stats and tower costs.
 */
public enum Difficulty {
    EASY   ("Easy",   0.75, 0.80, 0.85),
    MEDIUM ("Medium", 1.00, 1.00, 1.00),
    HARD   ("Hard",   1.40, 1.25, 1.20);

    public final String label;
    public final double enemyHpMult;
    public final double enemySpeedMult;
    public final double costMult;   // tower purchase/upgrade costs

    Difficulty(String label, double hp, double spd, double cost) {
        this.label          = label;
        this.enemyHpMult    = hp;
        this.enemySpeedMult = spd;
        this.costMult       = cost;
    }
}
