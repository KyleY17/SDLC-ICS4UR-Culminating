package game.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This handles saving and loading game data to files.
 * It keeps a leaderboard of the top 10 scores and when they were achieved.
 */
public class FileManager {
    // The file where scores are saved
    private static final String SCORES_FILE = "scores.txt";
    // Only keep the top 10 scores
    private static final int    MAX_ENTRIES = 10;
    // Format for the date and time
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // This is a record (a simple data container) for storing one high score entry
    public record ScoreEntry(String name, int score, int wave, String date)
            implements Comparable<ScoreEntry> {
        // When comparing scores, higher is better
        @Override public int compareTo(ScoreEntry o) {
            return Integer.compare(o.score, this.score); // descending
        }
    }

    // ── Save a new score ────────────────────────────────────────
    // Save a new high score to the file
    public static void saveScore(String playerName, int score, int wave) {
        // Load all existing scores
        List<ScoreEntry> entries = loadScores();
        // Get the current date and time
        String date = LocalDateTime.now().format(DATE_FMT);
        // Add the new score
        entries.add(new ScoreEntry(playerName, score, wave, date));
        // Sort by score (highest first)
        Collections.sort(entries);
        // Keep only the top 10
        if (entries.size() > MAX_ENTRIES) entries = entries.subList(0, MAX_ENTRIES);

        // Write the scores to the file
        try (PrintWriter pw = new PrintWriter(new FileWriter(SCORES_FILE))) {
            for (ScoreEntry e : entries) {
                // Format: name|score|wave|date
                pw.printf("%s|%d|%d|%s%n", e.name(), e.score(), e.wave(), e.date());
            }
        } catch (IOException ex) {
            System.err.println("Could not save scores: " + ex.getMessage());
        }
    }

    // ── Load leaderboard ────────────────────────────────────────
    // Load the leaderboard from the file
    public static List<ScoreEntry> loadScores() {
        List<ScoreEntry> entries = new ArrayList<>();
        File f = new File(SCORES_FILE);
        // If the file doesn't exist, return an empty list
        if (!f.exists()) return entries;

        // Read the file line by line
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split each line by the pipe character
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    try {
                        // Parse the parts and create a score entry
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

        // Sort by score (highest first)
        Collections.sort(entries);
        return entries;
    }

    // ── Check if a score qualifies for the leaderboard ──────────
    // Check if a score is good enough for the leaderboard
    public static boolean isHighScore(int score) {
        List<ScoreEntry> entries = loadScores();
        // If there are less than 10 scores, any new score qualifies
        if (entries.size() < MAX_ENTRIES) return true;
        // Otherwise, check if this score is higher than the lowest score
        return score > entries.get(entries.size() - 1).score();
    }

    // Get the rank (position) of a score
    public static int getRank(int score) {
        List<ScoreEntry> entries = loadScores();
        int rank = 1;
        // Count how many scores are higher
        for (ScoreEntry e : entries) {
            if (score < e.score()) rank++;
        }
        return rank;
    }
}
