package game.entities.enemies;
import game.entities.Enemy;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class BasicEnemy extends Enemy {
    public BasicEnemy(List<Point2D.Double> path, double hpMult, double spdMult) {
        super(0,0,24, 80*hpMult, 1.2, 10,1, path,
              new Color(60,180,75), new Color(30,120,45), "Basic", spdMult);
    }
    @Override protected void drawBody(Graphics2D g, Color color) {
        g.setColor(color); g.fillOval((int)x,(int)y,width,height);
        g.setColor(secondaryColor); g.setStroke(new BasicStroke(2f));
        g.drawOval((int)x,(int)y,width,height);
        g.setColor(Color.WHITE);
        g.fillOval((int)x+5,(int)y+7,5,5); g.fillOval((int)x+14,(int)y+7,5,5);
        g.setColor(Color.BLACK);
        g.fillOval((int)x+7,(int)y+8,3,3); g.fillOval((int)x+16,(int)y+8,3,3);
    }
}
