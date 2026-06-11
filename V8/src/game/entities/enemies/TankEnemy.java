package game.entities.enemies;
import game.entities.Enemy;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

// This enemy is very strong and has lots of health, but it's slow
public class TankEnemy extends Enemy {
    // Create a tank enemy
    public TankEnemy(List<Point2D.Double> path, double hpMult, double spdMult) {
        super(0,0,34, 320*hpMult, 0.65, 30,2, path,
              new Color(140,80,200), new Color(90,40,140), "Tank", spdMult);
    }
    // Draw the enemy as a purple rectangle with a crosshair
    @Override protected void drawBody(Graphics2D g, Color color) {
        // Draw the rounded rectangle
        g.setColor(color); g.fillRoundRect((int)x,(int)y,width,height,6,6);
        g.setColor(secondaryColor); g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect((int)x,(int)y,width,height,6,6);
        // Draw the crosshair lines
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine((int)x+4,(int)y+height/2,(int)x+width-4,(int)y+height/2);
        g.drawLine((int)x+width/2,(int)y+4,(int)x+width/2,(int)y+height-4);
    }
}
