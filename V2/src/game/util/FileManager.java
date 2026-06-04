package game.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles all File I/O for the game:
 *   - High score leaderboard (scores.txt)
 *   - Last game stats
 * Demonstrates File I/O requirement.
 */
public class FileManager {
    private static final String SCORES_FILE = "scores.txt";
    private static final int    MAX_ENTRIES = 10;
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public record ScoreEntry(String name, int score, int wave, String date)
            implements Comparable<ScoreEntry> {
        @Override public int compareTo(ScoreEntry o) {
            return Integer.compare(o.score, this.score); // descending
        }
    }

    // ── Save a new score ────────────────────────────────────────
    public static void saveScore(String playerName, int score, int wave) {
        List<ScoreEntry> entries = loadScores();
        String date = LocalDateTime.now().format(DATE_FMT);
        entries.add(new ScoreEntry(playerName, score, wave, date));
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) entries = entries.subList(0, MAX_ENTRIES);

        try (PrintWriter pw = new PrintWriter(new FileWriter(SCORES_FILE))) {
            for (ScoreEntry e : entries) {
                pw.printf("%s|%d|%d|%s%n", e.name(), e.score(), e.wave(), e.date());
            }
        } catch (IOException ex) {
            System.err.println("Could not save scores: " + ex.getMessage());
        }
    }

    // ── Load leaderboard ────────────────────────────────────────
    public static List<ScoreEntry> loadScores() {
        List<ScoreEntry> entries = new ArrayList<>();
        File f = new File(SCORES_FILE);
        if (!f.exists()) return entries;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    try {
                        entries.add(new ScoreEntry(
                                parts[0],
                                Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim()),
                                parts[3]));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException ex) {
            System.err.println("Could not load scores: " + ex.getMessage());
        }

        Collections.sort(entries);
        return entries;
    }

    // ── Check if a score qualifies for the leaderboard ──────────
    public static boolean isHighScore(int score) {
        List<ScoreEntry> entries = loadScores();
        if (entries.size() < MAX_ENTRIES) return true;
        return score > entries.get(entries.size() - 1).score();
    }

    public static int getRank(int score) {
        List<ScoreEntry> entries = loadScores();
        int rank = 1;
        for (ScoreEntry e : entries) {
            if (score < e.score()) rank++;
        }
        return rank;
    }
}
