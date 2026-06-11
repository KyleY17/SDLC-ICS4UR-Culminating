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
    public FastEnemy(List<Point2D.Double> path, double hpMult, double spdMult) {
        super(0,0,20, 30*hpMult, 2.4, 15,1, path,
              new Color(255,160,20), new Color(200,100,0), "Fast", spdMult);
    }
    // Spin the enemy as it moves
    @Override public void update() { super.update(); angle += 5; }
    // Draw the enemy as a spinning orange triangle
    @Override protected void drawBody(Graphics2D g, Color color) {
        int cx=(int)(x+width/2), cy=(int)(y+height/2);
        Graphics2D r=(Graphics2D)g.create();
        // Rotate around the center
        r.rotate(Math.toRadians(angle),cx,cy);
        // Triangle points
        int[] xp={cx,cx-10,cx+10}, yp={cy-12,cy+8,cy+8};
        r.setColor(color); r.fillPolygon(xp,yp,3);
        r.setColor(secondaryColor); r.setStroke(new BasicStroke(1.5f)); r.drawPolygon(xp,yp,3);
        r.dispose();
    }
}
