package game;

import game.ui.*;
import game.util.Difficulty;
import game.util.GameMap;
import java.awt.*;
import javax.swing.*;

// This is the starting point of the game.
// It creates the game window and shows the menu, game, or game over screen.
public class Main {
    // The window that holds the entire game
    private static JFrame frame;

    // This runs when the game starts
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::launch);
    }

    // This creates the game window
    private static void launch() {
        // Make a window with a title
        frame = new JFrame("Comp Sci Tower Defense");
        // Close the game when you click the X button
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Don't let the player resize the window
        frame.setResizable(false);
        // Set the background color to brown
        frame.getContentPane().setBackground(new Color(45,30,12));
        // Show the main menu first
        showMainMenu();
        frame.pack();
        // Put the window in the middle of the screen
        frame.setLocationRelativeTo(null);
        // Make the window visible
        frame.setVisible(true);
    }

    // This shows the main menu where you pick the difficulty
    static void showMainMenu() {
        // Create the menu screen
        MainMenu menu = new MainMenu();
        // Make it the same size as the game area
        menu.setPreferredSize(new Dimension(GameMap.WIDTH + SidePanel.PANEL_WIDTH, GameMap.HEIGHT));
        // When the player picks a difficulty, start the game
        menu.setOnStartGame(Main::showGame);
        // Clear the window and put the menu in it
        frame.getContentPane().removeAll();
        frame.getContentPane().add(menu);
        // Update what the window shows
        frame.pack(); frame.revalidate(); frame.repaint();
    }

    // This starts the actual game
    static void showGame(Difficulty difficulty) {
        // Create the side panel (where tower buttons are)
        SidePanel side = new SidePanel();
        // Create the main game screen
        GamePanel game = new GamePanel(side, difficulty);

        // If the player loses, show the game over screen
        game.setOnGameOver(() -> SwingUtilities.invokeLater(() ->
                showEndScreen(false, game.getScore(), game.getWave(), game.getLives())));
        // If the player wins all waves, show the victory screen
        game.setOnVictory(() -> SwingUtilities.invokeLater(() ->
                showEndScreen(true, game.getScore(), game.getWave(), game.getLives())));

        // Put the game and side panel together
        JPanel wrapper = new JPanel(new BorderLayout(0,0));
        wrapper.setBackground(new Color(45,30,12));
        // Game goes in the middle
        wrapper.add(game, BorderLayout.CENTER);
        // Side panel goes on the right
        wrapper.add(side, BorderLayout.EAST);

        // Clear the window and put both panels in it
        frame.getContentPane().removeAll();
        frame.getContentPane().add(wrapper);
        // Update what the window shows
        frame.pack(); frame.revalidate(); frame.repaint();
        // Make the game listen for keyboard inputs
        SwingUtilities.invokeLater(game::requestFocus);
    }

    // This shows the game over screen (win or lose)
    static void showEndScreen(boolean victory, int score, int wave, int lives) {
        // Create the game over screen
        GameOverScreen end = new GameOverScreen(victory, score, wave, lives);
        // Make it the same size as the game area
        end.setPreferredSize(new Dimension(GameMap.WIDTH + SidePanel.PANEL_WIDTH, GameMap.HEIGHT));
        // If the player wants to play again, go back to the main menu
        end.setOnPlayAgain(() -> showMainMenu());
        // If the player wants to go to the main menu, do that
        end.setOnMainMenu(Main::showMainMenu);
        // Clear the window and put the game over screen in it
        frame.getContentPane().removeAll();
        frame.getContentPane().add(end);
        // Update what the window shows
        frame.pack(); frame.revalidate(); frame.repaint();
    }
}
