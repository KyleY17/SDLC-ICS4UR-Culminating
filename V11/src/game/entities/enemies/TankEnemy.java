package game.entities.enemies;
import game.entities.Enemy;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

// This enemy is very strong and has lots of health, but it's slow
public class TankEnemy extends Enemy {
    // Create a tank enemy
    // hpMult: multiply health by this (affected by difficulty)
    // spdMult: multiply speed by this (affected by difficulty)
    public TankEnemy(List<Point2D.Double> path, double hpMult, double spdMult) {
        // Create enemy with: size=34, health=150, speed=0.45 (very slow), reward=$150, damage=2 lives
        super(0,0,34, 150*hpMult, 0.45, 150,2, path,
              new Color(140,80,200), new Color(90,40,140), "Tank", spdMult);
    }
    
    // Draw the enemy as a purple rectangle with a crosshair
    @Override protected void drawBody(Graphics2D g, Color color) {
        // Draw the main body (rounded rectangle)
        g.setColor(color); 
        g.fillRoundRect((int)x,(int)y,width,height,6,6);
        // Draw the outline
        g.setColor(secondaryColor); 
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect((int)x,(int)y,width,height,6,6);
        // Draw the crosshair lines (targeting reticle)
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine((int)x+4,(int)y+height/2,(int)x+width-4,(int)y+height/2);  // horizontal
        g.drawLine((int)x+width/2,(int)y+4,(int)x+width/2,(int)y+height-4);   // vertical
    }
}
