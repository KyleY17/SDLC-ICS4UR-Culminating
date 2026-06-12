package game.entities.enemies;
import game.entities.Enemy;
import java.awt.geom.Point2D;
import java.util.List;

// This factory makes enemies of different types
// Instead of saying "new BasicEnemy(...)", we say "EnemyFactory.create(BASIC, ...)"
public class EnemyFactory {
    // The different types of enemies we can make
    public enum EnemyType { BASIC, FAST, TANK, BOSS }

    // Create an enemy of the given type
    public static Enemy create(EnemyType type, List<Point2D.Double> path, double hpMult, double spdMult) {
        return switch(type) {
            // Make a basic enemy
            case BASIC -> new BasicEnemy(path, hpMult, spdMult);
            // Make a fast enemy
            case FAST  -> new FastEnemy(path, hpMult, spdMult);
            // Make a tank enemy
            case TANK  -> new TankEnemy(path, hpMult, spdMult);
            // Make a boss enemy
            case BOSS  -> new BossEnemy(path, hpMult, spdMult);
        };
    }
}
