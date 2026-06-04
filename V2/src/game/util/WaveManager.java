package game.util;

import game.entities.Enemy;
import game.entities.enemies.EnemyFactory;
import game.entities.enemies.EnemyFactory.EnemyType;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Manages wave spawning. Unlimited waves — after wave 15 it keeps generating harder waves.
 */
public class WaveManager {
    private final List<Point2D.Double> path;
    private final List<SpawnEvent> spawnQueue = new ArrayList<>();
    private int spawnTimer = 0;
    private int currentWave = 0;
    private boolean waveActive = false;
    private Difficulty difficulty = Difficulty.MEDIUM;

    private record SpawnEvent(EnemyType type, int interval, double hpMult, double spdMult) {}

    public WaveManager(List<Point2D.Double> path) { this.path = path; }

    public List<Enemy> update() {
        List<Enemy> spawned = new ArrayList<>();
        if (!waveActive || spawnQueue.isEmpty()) return spawned;

        spawnTimer--;
        if (spawnTimer <= 0 && !spawnQueue.isEmpty()) {
            SpawnEvent e = spawnQueue.remove(0);
            Enemy en = EnemyFactory.create(e.type(), path, e.hpMult(), e.spdMult());
            spawned.add(en);
            spawnTimer = e.interval();
        }
        if (spawnQueue.isEmpty()) waveActive = false;
        return spawned;
    }

    public void startNextWave() {
        if (waveActive) return;
        currentWave++;
        buildWave(currentWave);
        waveActive = true;
    }

    private void buildWave(int wave) {
        spawnQueue.clear();
        // Base HP scales each wave; difficulty multiplier on top
        double waveScale = 1.0 + (wave - 1) * 0.18;
        double hp  = waveScale * difficulty.enemyHpMult;
        double spd = difficulty.enemySpeedMult;

        // Waves 1-15: hand-crafted. Beyond 15: procedural escalation.
        if (wave <= 15) {
            switch (wave) {
                case 1  -> add(EnemyType.BASIC,  8, 60, hp, spd);
                case 2  -> add(EnemyType.BASIC, 12, 50, hp, spd);
                case 3  -> { add(EnemyType.BASIC, 8, 50, hp, spd); add(EnemyType.FAST, 4, 40, hp, spd); }
                case 4  -> add(EnemyType.FAST,  12, 35, hp, spd);
                case 5  -> { add(EnemyType.BASIC,6,45,hp,spd); add(EnemyType.TANK,3,80,hp,spd); add(EnemyType.BOSS,1,0,hp,spd); }
                case 6  -> { add(EnemyType.FAST,8,30,hp,spd); add(EnemyType.BASIC,10,40,hp,spd); }
                case 7  -> { add(EnemyType.TANK,5,70,hp,spd); add(EnemyType.FAST,10,25,hp,spd); }
                case 8  -> { add(EnemyType.BASIC,15,35,hp,spd); add(EnemyType.TANK,4,65,hp,spd); }
                case 9  -> { add(EnemyType.FAST,12,22,hp,spd); add(EnemyType.TANK,5,60,hp,spd); add(EnemyType.BOSS,1,0,hp,spd); }
                case 10 -> { add(EnemyType.BASIC,10,30,hp,spd); add(EnemyType.FAST,10,25,hp,spd); add(EnemyType.TANK,6,55,hp,spd); add(EnemyType.BOSS,2,120,hp,spd); }
                case 11 -> { add(EnemyType.FAST,18,20,hp,spd); add(EnemyType.TANK,6,50,hp,spd); }
                case 12 -> { add(EnemyType.TANK,8,45,hp,spd); add(EnemyType.BOSS,2,100,hp,spd); }
                case 13 -> { add(EnemyType.BASIC,20,25,hp,spd); add(EnemyType.FAST,16,18,hp,spd); add(EnemyType.TANK,8,40,hp,spd); }
                case 14 -> { add(EnemyType.FAST,20,16,hp,spd); add(EnemyType.TANK,10,38,hp,spd); add(EnemyType.BOSS,3,90,hp,spd); }
                case 15 -> { add(EnemyType.BASIC,15,20,hp,spd); add(EnemyType.FAST,15,18,hp,spd); add(EnemyType.TANK,10,35,hp,spd); add(EnemyType.BOSS,4,80,hp,spd); }
            }
        } else {
            // Procedural: escalate counts and types beyond wave 15
            int extra = wave - 15;
            int basicCount = 10 + extra * 2;
            int fastCount  = 8  + extra * 2;
            int tankCount  = 5  + extra;
            int bossCount  = 2  + extra / 3;
            add(EnemyType.BASIC, basicCount, Math.max(15,30-extra), hp, spd);
            add(EnemyType.FAST,  fastCount,  Math.max(12,25-extra), hp, spd);
            add(EnemyType.TANK,  tankCount,  Math.max(25,45-extra), hp, spd);
            add(EnemyType.BOSS,  bossCount,  Math.max(60,80-extra), hp, spd);
        }
        spawnTimer = 30;
    }

    private void add(EnemyType type, int count, int interval, double hp, double spd) {
        for (int i = 0; i < count; i++) spawnQueue.add(new SpawnEvent(type, interval, hp, spd));
    }

    public void setDifficulty(Difficulty d) { this.difficulty = d; }
    public boolean isWaveActive() { return waveActive || !spawnQueue.isEmpty(); }
    public boolean isWaveClear()  { return !waveActive && spawnQueue.isEmpty(); }
    public int getCurrentWave()   { return currentWave; }
    // No longer a fixed cap — unlimited waves
    public int getTotalWaves()    { return -1; } // -1 = unlimited
}
