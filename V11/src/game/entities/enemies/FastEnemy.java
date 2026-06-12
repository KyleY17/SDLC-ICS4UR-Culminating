package game.entities.enemies;
import game.entities.Enemy;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

// This enemy is very fast but doesn't have much health
public class FastEnemy extends Enemy {
    // Rotation angle for spinning animation
    private double angle = 0;
    
    // Create a fast enemy
    // hpMult: multiply health by this (affected by difficulty)
    // spdMult: multiply speed by this (affected by difficulty)
    public FastEnemy(List<Point2D.Double> path, double hpMult, double spdMult) {
        // Create enemy with: size=20, health=30, speed=2.4 (2x faster than basic), reward=$60, damage=1 life
        super(0,0,20, 30*hpMult, 2.4, 60,1, path,
              new Color(255,160,20), new Color(200,100,0), "Fast", spdMult);
    }
    
    // Spin the enemy as it moves (for visual effect)
    @Override public void update() { 
        super.update(); 
        angle += 5; // Rotate a bit each frame
    }
    
    // Draw the enemy as a spinning orange triangle
    @Override protected void drawBody(Graphics2D g, Color color) {
        int cx=(int)(x+width/2), cy=(int)(y+height/2);
        Graphics2D r=(Graphics2D)g.create();
        // Rotate the triangle around its center
        r.rotate(Math.toRadians(angle),cx,cy);
        // Define the triangle points
        int[] xp={cx,cx-10,cx+10}, yp={cy-12,cy+8,cy+8};
        // Draw filled triangle
        r.setColor(color); 
        r.fillPolygon(xp,yp,3);
        // Draw triangle outline
        r.setColor(secondaryColor); 
        r.setStroke(new BasicStroke(1.5f)); 
        r.drawPolygon(xp,yp,3);
        r.dispose();
    }
}
