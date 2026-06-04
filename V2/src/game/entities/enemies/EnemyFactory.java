package game.entities.enemies;
import game.entities.Enemy;
import java.awt.geom.Point2D;
import java.util.List;

public class EnemyFactory {
    public enum EnemyType { BASIC, FAST, TANK, BOSS }

    public static Enemy create(EnemyType type, List<Point2D.Double> path, double hpMult, double spdMult) {
        return switch(type) {
            case BASIC -> new BasicEnemy(path, hpMult, spdMult);
            case FAST  -> new FastEnemy(path, hpMult, spdMult);
            case TANK  -> new TankEnemy(path, hpMult, spdMult);
            case BOSS  -> new BossEnemy(path, hpMult, spdMult);
        };
    }
}
