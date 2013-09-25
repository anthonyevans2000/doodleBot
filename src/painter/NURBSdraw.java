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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import main.Main;
import static main.Main._matlab;
import static main.Main.drawCurve;


public class NURBSdraw{

    public static void createAndShowGUI() throws IOException {
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

    private ArrayList<NurbsCurve> _drawn = new ArrayList<NurbsCurve>();
    private int squareX = 50;
    private int squareY = 50;
    private int squareW = 20;
    private int squareH = 20;

    public MyPanel() throws IOException {

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
        
        InputMap im = getInputMap(WHEN_FOCUSED);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "onEnter");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "onQuit");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "onCurveReset");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "onImageProcess");
        
            am.put("onEnter", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Enter pressed
                    _drawn.add(Main.drawCurve.cloneCurve());

                    Main._matlab._toProcess.add(drawCurve.convert2PrintCoords());
                }});
        
            am.put("onCurveReset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Reset (r) pressed
               Main.curveReset = true;
               Main.commsReset = true;
               _drawn.clear();
               
               repaint();
            }
            });
            
            am.put("onImageProcess", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Image (i) pressed
               Main.processImage = true;
            }
            });
            
            
            am.put("onQuit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Reset (r) pressed
               Main.readyToQuit = true;
               System.exit(0);
            }
            });        
        
        
    }
    
    private void modNURBS(int x, int y)
    {
        int closest = -1;
        double distance = Double.POSITIVE_INFINITY;
        double currentDistance;
        
        for(int i = 0; i < Main.drawCurve.controlX.size(); i++) {
            currentDistance = pointDistance(x,y,Main.drawCurve.controlX.get(i),Main.drawCurve.controlY.get(i));
            
            if( currentDistance < distance) {
                closest = i;
                distance = currentDistance;
            }
        }
        Main.drawCurve.controlX.set(closest, (short)x);
        Main.drawCurve.controlY.set(closest, (short)y);
        
        repaint();
    }
    
    private double pointDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
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

        //Draw previous
        for(int i = 0; i < _drawn.size() ; i++) {
            drawPath(g , _drawn.get(i));
        }
        
        
        //Draw current

        drawPath(g,curve);
        g.setColor(Color.RED);
                
        g.drawRect( curve.controlX.get(0),
                    curve.controlY.get(0), 
                    2, 2);
        g.drawRect( curve.controlX.get(1),
                    curve.controlY.get(1), 
                    2, 2);
        g.drawRect( curve.controlX.get(2),
                    curve.controlY.get(2), 
                    2, 2);
        g.drawRect( curve.controlX.get(3),
                    curve.controlY.get(3), 
                    2, 2);
    
    };
    

    void drawPath(Graphics2D g, NurbsCurve curve) {
        GeneralPath path = new GeneralPath();
        path.moveTo (curve.controlX.get(0), curve.controlY.get(0));
        
        path.curveTo(   curve.controlX.get(1),
                        curve.controlY.get(1),
                        curve.controlX.get(2),
                        curve.controlY.get(2),
                        curve.controlX.get(3),
                        curve.controlY.get(3));
        
        g.draw(path);
        
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int)Main.canvasXDim,(int)Main.canvasYDim);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        setBackground(Color.WHITE);

        paintNURBS(g2,Main.drawCurve);
    }  
}

