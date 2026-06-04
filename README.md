# Tower Defense

A Java Swing tower defense game built as a group project.

**Group Members:** Kyle Ye, Junn Hayashi, Ray He

---

## Overview

Tower Defense is a 2D strategy game where players place and upgrade towers to stop waves of enemies from reaching the exit. The game features multiple tower types, an enemy wave system, difficulty settings, a persistent leaderboard, and an unlimited wave mode after wave 15.

---

## Features

- **4 Tower Types** — each with unique mechanics and 5 upgrade tiers
- **4 Enemy Types** — Basic, Fast, Tank, and Boss, scaling in difficulty each wave
- **3 Difficulty Modes** — Easy, Medium, and Hard (affect enemy HP, speed, and tower costs)
- **Unlimited Waves** — hand-crafted waves 1–15, procedurally generated beyond that
- **Persistent Leaderboard** — top 10 scores saved to `scores.txt` via file I/O
- **Game Controls** — pause, 2x speed, and auto-start next wave
- **Tower Selling & Upgrading** — sell towers for 50% refund, upgrade up to tier 5

---

## Towers

| Tower | Cost | Mechanic |
|-------|------|----------|
| **Cannon** | $100 | Moderate damage with splash radius |
| **Sniper** | $150 | Infinite range, high single-target damage, slow fire rate |
| **Freeze** | $120 | Low damage, slows enemies significantly |
| **Mage** | $250 | Chain lightning — bounces between nearby enemies |

### Upgrade Tiers
All towers upgrade from **Tier 1 to Tier 5**. Each upgrade costs 50% more than the base pricing, with costs scaling ×2.7 per tier. The **Tier 5 upgrade** has an additional $20,000 surcharge on top of the scaling cost, making it a late-game prestige upgrade.

---

## Enemies

| Enemy | Description |
|-------|-------------|
| **Basic** | Standard speed and HP |
| **Fast** | Low HP but moves quickly |
| **Tank** | Slow but very high HP |
| **Boss** | High HP, appears from wave 5 onward |

Enemy HP scales by 18% per wave. Difficulty multipliers apply on top of that.

---

## Controls

| Input | Action |
|-------|--------|
| Click tower button → click map | Place a tower |
| Right-click | Cancel placement |
| Click placed tower | Select it (shows range, stats, upgrade/sell) |
| `SPACE` | Start next wave |
| `P` | Pause / resume |
| `F` | Toggle 2x speed |
| `ESC` | Deselect tower |

---

## Project Structure

```
src/
├── game/
│   ├── entities/
│   │   ├── Entity.java          # Abstract base class for all entities
│   │   ├── Tower.java           # Abstract tower base (extends Entity)
│   │   ├── Enemy.java           # Enemy class with path-following logic
│   │   ├── Projectile.java      # Handles single-target, splash, and slow projectiles
│   │   └── towers/
│   │       ├── CannonTower.java
│   │       ├── SniperTower.java
│   │       ├── SlowTower.java
│   │       ├── MageTower.java   # Chain lightning; use createChainProjectiles()
│   │       └── TowerFactory.java
│   ├── ui/
│   │   ├── MainMenu.java        # Difficulty selection and leaderboard
│   │   ├── SidePanel.java       # Tower shop, stats HUD, upgrade/sell buttons
│   │   ├── GamePanel.java       # Main game loop and rendering
│   │   └── GameOverScreen.java  # Victory/defeat screen with score saving
│   └── util/
│       ├── GameMap.java         # Grid, path definition, tree placement, rendering
│       ├── WaveManager.java     # Wave spawning logic (waves 1–15 + procedural)
│       ├── FileManager.java     # Score saving/loading via scores.txt
│       └── Difficulty.java      # Enum: EASY, MEDIUM, HARD with stat multipliers
```

---

## OOP Concepts Demonstrated

- **Inheritance** — `Entity` → `Tower` → `CannonTower` / `SniperTower` / `SlowTower` / `MageTower`
- **Abstract Classes** — `Entity` and `Tower` define abstract `update()`, `draw()`, `createProjectile()`, etc.
- **Polymorphism** — towers and enemies are stored as `List<Tower>` / `List<Enemy>` and called through shared interfaces
- **Encapsulation** — all fields are `private`/`protected` with getters; game state is managed through callbacks
- **Factory Pattern** — `TowerFactory` centralizes tower creation and metadata
- **File I/O** — `FileManager` reads and writes `scores.txt` for persistent high scores

---

## How to Run

1. Ensure you have **JDK 17+** installed (project uses switch expressions and records)
2. Compile from the `src/` directory:
   ```
   javac -d out $(find src -name "*.java")
   ```
3. Run:
   ```
   java -cp out game.ui.MainMenu
   ```
   *(adjust the main class name to match your entry point)*

---

## Notes

- The **Mage Tower** uses a special `createChainProjectiles(List<Enemy>)` method instead of `createProjectile()`. In `GamePanel`, check `tower instanceof MageTower` before firing and call the chain method accordingly.
- Scores are saved to `scores.txt` in the working directory automatically on the game over screen.
- The map is a fixed 18×13 tile grid (864×624 px). Tower placement is blocked on path tiles and tree tiles.
