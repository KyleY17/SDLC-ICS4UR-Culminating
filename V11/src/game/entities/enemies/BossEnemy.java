package game.entities.enemies;
import game.entities.Enemy;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

// This is the BOSS enemy - super strong and super dangerous!
public class BossEnemy extends Enemy {
    // Pulsing animation value
    private double pulse=0;
    
    // Create a boss enemy
    // hpMult: multiply health by this (affected by difficulty)
    // spdMult: multiply speed by this (affected by difficulty)
    public BossEnemy(List<Point2D.Double> path, double hpMult, double spdMult) {
        // Create enemy with: size=42, health=1000, speed=0.4 (very slow), reward=$500, damage=5 lives!!!
        super(0,0,42, 1000*hpMult, 0.4, 500,5, path,
              new Color(220,40,40), new Color(140,10,10), "Boss", spdMult);
    }
    
    // Make the boss pulse as it moves (animated effect)
    @Override public void update() { 
        super.update(); 
        pulse+=0.08; // Increment pulse animation
    }
    
    // Draw the boss as a red star with a glowing aura
    @Override protected void drawBody(Graphics2D g, Color color) {
        int cx=(int)(x+width/2), cy=(int)(y+height/2);
        int outerR=width/2, innerR=outerR/2, pts=8;
        
        // Draw the glowing aura (pulsing effect)
        float ga=(float)(0.3+0.2*Math.sin(pulse));
        g.setColor(new Color(220,40,40,(int)(ga*255)));
        g.fillOval(cx-outerR-6,cy-outerR-6,(outerR+6)*2,(outerR+6)*2);
        
        // Draw the star shape (8-pointed star)
        int[] sx=new int[pts*2], sy=new int[pts*2];
        for(int i=0;i<pts*2;i++){
            double a=Math.PI/pts*i-Math.PI/2; 
            int r=(i%2==0)?outerR:innerR;
            sx[i]=(int)(cx+r*Math.cos(a)); 
            sy[i]=(int)(cy+r*Math.sin(a));
        }
        g.setColor(color); 
        g.fillPolygon(sx,sy,pts*2);
        // Draw the outline
        g.setColor(secondaryColor); 
        g.setStroke(new BasicStroke(2f)); 
        g.drawPolygon(sx,sy,pts*2);
        
        // Draw the BOSS label
        g.setColor(Color.WHITE); 
        g.setFont(new Font("Arial",Font.BOLD,8));
        FontMetrics fm=g.getFontMetrics();
        g.drawString("BOSS",cx-fm.stringWidth("BOSS")/2,cy+3);
    }
}
