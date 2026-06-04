package game;

import game.ui.*;
import game.util.Difficulty;
import game.util.GameMap;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::launch);
    }

    private static void launch() {
        frame = new JFrame("Tower Defense");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().setBackground(new Color(45,30,12));
        showMainMenu();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static void showMainMenu() {
        MainMenu menu = new MainMenu();
        menu.setPreferredSize(new Dimension(GameMap.WIDTH + SidePanel.PANEL_WIDTH, GameMap.HEIGHT));
        menu.setOnStartGame(Main::showGame);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(menu);
        frame.pack(); frame.revalidate(); frame.repaint();
    }

    static void showGame(Difficulty difficulty) {
        SidePanel side = new SidePanel();
        GamePanel game = new GamePanel(side, difficulty);

        game.setOnGameOver(() -> SwingUtilities.invokeLater(() ->
                showEndScreen(false, game.getScore(), game.getWave(), game.getLives())));
        game.setOnVictory(() -> SwingUtilities.invokeLater(() ->
                showEndScreen(true, game.getScore(), game.getWave(), game.getLives())));

        JPanel wrapper = new JPanel(new BorderLayout(0,0));
        wrapper.setBackground(new Color(45,30,12));
        wrapper.add(game, BorderLayout.CENTER);
        wrapper.add(side, BorderLayout.EAST);

        frame.getContentPane().removeAll();
        frame.getContentPane().add(wrapper);
        frame.pack(); frame.revalidate(); frame.repaint();
        SwingUtilities.invokeLater(game::requestFocus);
    }

    static void showEndScreen(boolean victory, int score, int wave, int lives) {
        GameOverScreen end = new GameOverScreen(victory, score, wave, lives);
        end.setPreferredSize(new Dimension(GameMap.WIDTH + SidePanel.PANEL_WIDTH, GameMap.HEIGHT));
        end.setOnPlayAgain(() -> showMainMenu());
        end.setOnMainMenu(Main::showMainMenu);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(end);
        frame.pack(); frame.revalidate(); frame.repaint();
    }
}
