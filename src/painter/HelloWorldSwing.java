package painter;

import dataTypes.NurbsCurve;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

public class HelloWorldSwing{
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(); 
            }
        });
    }

    private static void createAndShowGUI() {
        System.out.println("Created GUI on EDT? "+
        SwingUtilities.isEventDispatchThread());
        JFrame f = new JFrame("Swing Paint Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        f.add(new MyPanel());
        f.pack();
        f.setVisible(true);
    } 
}

class MyPanel extends JPanel {

    private int squareX = 50;
    private int squareY = 50;
    private int squareW = 20;
    private int squareH = 20;
    
    NurbsCurve curve = makeNURBS();

    public MyPanel() {

        setBorder(BorderFactory.createLineBorder(Color.black));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                modNURBS(e.getX(),e.getY());
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                modNURBS(e.getX(),e.getY());
            }
        });
        
    }
    
    private void modNURBS(int x, int y)
    {
        int closest = -1;
        double distance = Double.POSITIVE_INFINITY;
        Point2D.Double click = new Point2D.Double((double)x,(double)y);
        Point2D.Double current;
        Iterator it = curve.controlPoints.iterator();
        while(it.hasNext()) {
            current = (Point2D.Double) it.next();
            if(current.distance(click) < distance) {
                closest = curve.controlPoints.indexOf(current);
                distance = current.distance(click);
            }
        }
        
        curve.controlPoints.set(closest, click);
        
        repaint();
    }
    
    private NurbsCurve makeNURBS() {
        
        ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
        points.add(new Point2D.Double(10,10));
        points.add(new Point2D.Double(80,80));
        points.add(new Point2D.Double(180,10));
        points.add(new Point2D.Double(10,100));
        
        ArrayList<Double> knots = new ArrayList<Double>();
        knots.add(0,0.d);
        knots.add(1,0.d);
        knots.add(2,0.d);
        knots.add(3,0.5);
        knots.add(4,1.d);
        knots.add(5,1.d);
        knots.add(6,1.d);
       
        curve = new NurbsCurve(points, knots);
        
        return curve;
    }
    
    private void moveSquare(int x, int y) {
        int OFFSET = 1;
        if ((squareX!=x) || (squareY!=y)) {
            repaint(squareX,squareY,squareW+OFFSET,squareH+OFFSET);
            squareX=x;
            squareY=y;
            repaint(squareX,squareY,squareW+OFFSET,squareH+OFFSET);
        } 
    }
    
    
    private void paintNURBS(Graphics2D g, NurbsCurve curve)
    {
        
        GeneralPath path = new GeneralPath();
        
        
        path.moveTo (curve.controlPoints.get(0).getX(), curve.controlPoints.get(0).getY());
        
        path.curveTo(   curve.controlPoints.get(1).getX(),
                        curve.controlPoints.get(1).getY(),
                        curve.controlPoints.get(2).getX(),
                        curve.controlPoints.get(2).getY(),
                        curve.controlPoints.get(3).getX(),
                        curve.controlPoints.get(3).getY());
        
        g.draw(path);
        
        g.setColor(Color.RED);
                
        g.drawRect( (int)curve.controlPoints.get(0).getX(),
                    (int)curve.controlPoints.get(0).getY(), 
                    2, 2);
        g.drawRect( (int)curve.controlPoints.get(1).getX(),
                    (int)curve.controlPoints.get(1).getY(), 
                    2, 2);
        g.drawRect( (int)curve.controlPoints.get(2).getX(),
                    (int)curve.controlPoints.get(2).getY(), 
                    2, 2);
        g.drawRect( (int)curve.controlPoints.get(3).getX(),
                    (int)curve.controlPoints.get(3).getY(), 
                    2, 2);
    
    };
    

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600,600);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        setBackground(Color.WHITE);
        
//        g.drawString("This is my custom Panel!",10,20);
//        g.setColor(Color.RED);
//        g.fillRect(squareX,squareY,squareW,squareH);
//        g.setColor(Color.BLACK);
//        g.drawRect(squareX,squareY,squareW,squareH);
//        g.drawLine(squareX, squareY, squareW+squareX, squareH+squareY);
        paintNURBS(g2,curve);
    }  
}

