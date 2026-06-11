package game.util;

import game.entities.Enemy;
import game.entities.enemies.EnemyFactory;
import game.entities.enemies.EnemyFactory.EnemyType;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * This manages all the enemy waves.
 * It spawns enemies in waves, and each wave gets harder.
 * Waves go on forever - after wave 15, it keeps getting harder!
 */
public class WaveManager {
    // The path that enemies walk on
    private final List<Point2D.Double> path;
    // The queue of enemies waiting to spawn
    private final List<SpawnEvent> spawnQueue = new ArrayList<>();
    // How many ticks until the next enemy spawns
    private int spawnTimer = 0;
    // Which wave are we on?
    private int currentWave = 0;
    // Is a wave currently active?
    private boolean waveActive = false;
    // The difficulty setting
    private Difficulty difficulty = Difficulty.MEDIUM;

    // This is a spawn event - when and what to spawn
    private record SpawnEvent(EnemyType type, int interval, double hpMult, double spdMult) {}

    // Create a wave manager
    public WaveManager(List<Point2D.Double> path) { this.path = path; }

    // Update the wave - spawn enemies one at a time
    public List<Enemy> update() {
        List<Enemy> spawned = new ArrayList<>();
        // If no wave is active, don't spawn anything
        if (!waveActive || spawnQueue.isEmpty()) return spawned;

        // Tick down the spawn timer
        spawnTimer--;
        // If the timer is done, spawn an enemy
        if (spawnTimer <= 0 && !spawnQueue.isEmpty()) {
            SpawnEvent e = spawnQueue.remove(0);
            // Create the enemy
            Enemy en = EnemyFactory.create(e.type(), path, e.hpMult(), e.spdMult());
            spawned.add(en);
            // Reset the timer for the next enemy
            spawnTimer = e.interval();
        }
        // If the queue is empty, the wave is over
        if (spawnQueue.isEmpty()) waveActive = false;
        return spawned;
    }

    // Start the next wave
    public void startNextWave() {
        // Don't start a new wave if one is already active
        if (waveActive) return;
        // Go to the next wave
        currentWave++;
        // Build the enemies for this wave
        buildWave(currentWave);
        // Start spawning
        waveActive = true;
    }

    // Build the list of enemies for a wave
    private void buildWave(int wave) {
        // Clear the spawn queue
        spawnQueue.clear();
        // Enemies get stronger each wave
        double waveScale = 1.0 + (wave - 1) * 0.18;
        // Scale the health by the wave and difficulty
        double hp  = waveScale * difficulty.enemyHpMult;
        // Speed is affected by difficulty
        double spd = difficulty.enemySpeedMult;

        // Waves 1-15: hand-crafted waves
        if (wave <= 15) {
            switch (wave) {
                case 1  -> add(EnemyType.BASIC,  10, 30, hp, spd);
                case 2  -> add(EnemyType.BASIC, 14, 30, hp, spd);
                case 3  -> { add(EnemyType.BASIC, 10, 30, hp, spd); add(EnemyType.FAST, 6, 40, hp, spd); }
                case 4  -> add(EnemyType.FAST,  15, 35, hp, spd);
                case 5  -> { add(EnemyType.BASIC,10,45,hp,spd); add(EnemyType.TANK,4,80,hp,spd); add(EnemyType.BOSS,1,0,hp,spd); }
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
            // Waves 16+: automatically get harder
            int extra = wave - 15;
            // More and more enemies
            int basicCount = 10 + extra * 2;
            int fastCount  = 8  + extra * 2;
            int tankCount  = 5  + extra;
            int bossCount  = 2  + extra / 3;
            // Add them to the spawn queue
            add(EnemyType.BASIC, basicCount, Math.max(15,30-extra), hp, spd);
            add(EnemyType.FAST,  fastCount,  Math.max(12,25-extra), hp, spd);
            add(EnemyType.TANK,  tankCount,  Math.max(25,45-extra), hp, spd);
            add(EnemyType.BOSS,  bossCount,  Math.max(60,80-extra), hp, spd);
        }
        // Start spawning after a delay
        spawnTimer = 30;
    }

    // Helper to add enemies to the spawn queue
    private void add(EnemyType type, int count, int interval, double hp, double spd) {
        for (int i = 0; i < count; i++) spawnQueue.add(new SpawnEvent(type, interval, hp, spd));
    }

    // Set the difficulty
    public void setDifficulty(Difficulty d) { this.difficulty = d; }
    // Is a wave currently spawning enemies?
    public boolean isWaveActive() { return waveActive || !spawnQueue.isEmpty(); }
    // Have all enemies been defeated?
    public boolean isWaveClear()  { return !waveActive && spawnQueue.isEmpty(); }
    // Which wave are we on?
    public int getCurrentWave()   { return currentWave; }
    // Unlimited waves - return -1 to mean infinite
    public int getTotalWaves()    { return -1; }
}
