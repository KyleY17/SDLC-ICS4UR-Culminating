package game.util;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the map grid, the enemy path, obstacles (trees), and rendering.
 */
public class GameMap {
    public static final int TILE   = 48;
    public static final int COLS   = 18;
    public static final int ROWS   = 13;
    public static final int WIDTH  = COLS * TILE;   // 864
    public static final int HEIGHT = ROWS * TILE;   // 624

    private final boolean[][] pathGrid = new boolean[ROWS][COLS];
    private final boolean[][] occupied = new boolean[ROWS][COLS];
    private final boolean[][] treeTile = new boolean[ROWS][COLS]; // visual obstruction

    private final List<Point2D.Double> waypoints = new ArrayList<>();

    // Fixed tree positions (col, row) — not on path, not adjacent to path
    private static final int[][] TREE_POSITIONS = {
        {1,0},{2,0},{4,0},{8,0},{9,0},{13,0},{15,0},{16,0},
        {0,3},{0,4},{1,3},{0,6},{1,6},{2,6},{0,9},{0,10},
        {4,3},{5,3},{4,4},{6,3},
        {9,3},{10,3},{9,4},
        {13,3},{14,3},{15,3},
        {16,3},{17,3},{16,4},{17,4},
        {2,7},{3,7},{2,8},{15,7},{16,7},{17,7},{16,6},{17,6},
        {4,6},{5,6},{4,7},{5,7},
        {8,7},{9,7},{8,8},{9,8},
        {13,7},{13,6},{12,7},
        {3,12},{4,12},{6,12},{7,12},{8,12},{12,12},{13,12},{15,12},{16,12},{17,12},
        {0,12},{1,12},{17,0},{17,1},{17,2}
    };

    private static final Color COL_GRASS     = new Color(72, 138, 56);
    private static final Color COL_GRASS_ALT = new Color(65, 128, 50);
    private static final Color COL_PATH      = new Color(190, 162, 108);
    private static final Color COL_PATH_DARK = new Color(170, 142,  90);
    private static final Color COL_PATH_EDGE = new Color(145, 115,  72);

    public GameMap() {
        buildPath();
        markTrees();
    }

    private void buildPath() {
        int[][] grid = {
            {0,  2}, {3,  2}, {3,  5}, {7,  5}, {7,  1},
            {11, 1}, {11, 5}, {14, 5}, {14, 9}, {10, 9},
            {10, 11},{5,  11},{5,  8}, {1,  8}, {1,  11},
            {17, 11}
        };
        for (int[] wp : grid) {
            waypoints.add(new Point2D.Double(wp[0]*TILE + TILE/2.0, wp[1]*TILE + TILE/2.0));
        }
        for (int i = 0; i+1 < waypoints.size(); i++) {
            Point2D.Double a = waypoints.get(i), b = waypoints.get(i+1);
            int ac = (int)(a.x/TILE), ar = (int)(a.y/TILE);
            int bc = (int)(b.x/TILE), br = (int)(b.y/TILE);
            int dc = Integer.compare(bc,ac), dr = Integer.compare(br,ar);
            int c = ac, r = ar;
            markPath(r,c);
            while (c!=bc||r!=br) { if(c!=bc)c+=dc; else r+=dr; markPath(r,c); }
        }
    }

    private void markPath(int row, int col) {
        if (row>=0&&row<ROWS&&col>=0&&col<COLS) pathGrid[row][col]=true;
    }

    private void markTrees() {
        for (int[] t : TREE_POSITIONS) {
            int c = t[0], r = t[1];
            if (r>=0&&r<ROWS&&c>=0&&c<COLS && !pathGrid[r][c]) {
                treeTile[r][c] = true;
                occupied[r][c] = true; // trees block placement
            }
        }
    }

    public void draw(Graphics2D g2d) {
        // Grass
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (!pathGrid[r][c]) {
                    g2d.setColor((r+c)%2==0 ? COL_GRASS : COL_GRASS_ALT);
                    g2d.fillRect(c*TILE, r*TILE, TILE, TILE);
                }

        // Path
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (pathGrid[r][c]) {
                    g2d.setColor(COL_PATH);
                    g2d.fillRect(c*TILE, r*TILE, TILE, TILE);
                    g2d.setColor(COL_PATH_DARK);
                    g2d.drawLine(c*TILE+4, r*TILE+4, c*TILE+TILE-5, r*TILE+4);
                    g2d.drawLine(c*TILE+4, r*TILE+TILE-5, c*TILE+TILE-5, r*TILE+TILE-5);
                }

        // Path edges
        g2d.setColor(COL_PATH_EDGE);
        g2d.setStroke(new BasicStroke(1f));
        for (int r=0;r<ROWS;r++) for (int c=0;c<COLS;c++) if (pathGrid[r][c]) {
            if (r>0      &&!pathGrid[r-1][c]) g2d.drawLine(c*TILE,r*TILE,c*TILE+TILE,r*TILE);
            if (r<ROWS-1 &&!pathGrid[r+1][c]) g2d.drawLine(c*TILE,r*TILE+TILE,c*TILE+TILE,r*TILE+TILE);
            if (c>0      &&!pathGrid[r][c-1]) g2d.drawLine(c*TILE,r*TILE,c*TILE,r*TILE+TILE);
            if (c<COLS-1 &&!pathGrid[r][c+1]) g2d.drawLine(c*TILE+TILE,r*TILE,c*TILE+TILE,r*TILE+TILE);
        }

        // Grid overlay
        g2d.setColor(new Color(0,0,0,15));
        g2d.setStroke(new BasicStroke(0.5f));
        for (int r=0;r<=ROWS;r++) g2d.drawLine(0,r*TILE,WIDTH,r*TILE);
        for (int c=0;c<=COLS;c++) g2d.drawLine(c*TILE,0,c*TILE,HEIGHT);

        // Trees (drawn on top)
        for (int r=0;r<ROWS;r++)
            for (int c=0;c<COLS;c++)
                if (treeTile[r][c]) drawTree(g2d, c*TILE, r*TILE);
    }

    private void drawTree(Graphics2D g2d, int tx, int ty) {
        // Shadow
        g2d.setColor(new Color(0,0,0,40));
        g2d.fillOval(tx+6, ty+28, 36, 16);
        // Trunk
        g2d.setColor(new Color(101, 67, 33));
        g2d.fillRoundRect(tx+17, ty+28, 14, 16, 4, 4);
        // Canopy layers (back to front)
        g2d.setColor(new Color(34, 100, 34));
        g2d.fillOval(tx+2, ty+16, 44, 28);
        g2d.setColor(new Color(40, 120, 40));
        g2d.fillOval(tx+6, ty+8, 36, 28);
        g2d.setColor(new Color(56, 148, 56));
        g2d.fillOval(tx+10, ty+2, 28, 24);
        // Highlight
        g2d.setColor(new Color(100, 200, 80, 80));
        g2d.fillOval(tx+14, tx+4, 12, 8);
    }

    /** Snap pixel coord to top-left of tile */
    public static Point snapToGrid(int px, int py) {
        return new Point((px/TILE)*TILE, (py/TILE)*TILE);
    }

    /** Centre of tile given pixel coord */
    public static Point tileCentre(int px, int py) {
        return new Point((px/TILE)*TILE + TILE/2, (py/TILE)*TILE + TILE/2);
    }

    public static int toCol(int px) { return px/TILE; }
    public static int toRow(int py) { return py/TILE; }

    public boolean canPlace(int col, int row) {
        if (row<0||row>=ROWS||col<0||col>=COLS) return false;
        return !pathGrid[row][col] && !occupied[row][col];
    }

    public void setOccupied(int col, int row, boolean val) {
        if (row>=0&&row<ROWS&&col>=0&&col<COLS) occupied[row][col]=val;
    }

    public boolean isPathTile(int col, int row) {
        if (row<0||row>=ROWS||col<0||col>=COLS) return false;
        return pathGrid[row][col];
    }

    public boolean isTreeTile(int col, int row) {
        if (row<0||row>=ROWS||col<0||col>=COLS) return false;
        return treeTile[row][col];
    }

    public List<Point2D.Double> getWaypoints() { return waypoints; }
}
