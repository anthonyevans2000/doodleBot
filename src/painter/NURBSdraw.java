package painter;

import dataTypes.CurveVelocity;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import main.Main;
import static main.Main._matlab;
import static main.Main.drawCurve;
import static main.Main.plcPortNumber;
import static main.Main.sPoints;
import static main.Main.timeStep;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;


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
        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "onC");

        am.put("onEnter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Enter pressed
                try {
                    CurveVelocity profile = _matlab.processNURBS(drawCurve, sPoints, timeStep);
                    Main._server.serveInstruction(profile, plcPortNumber);
                
                
                } catch (MatlabConnectionException ex) {
                    Logger.getLogger(MyPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MatlabInvocationException ex) {
                    Logger.getLogger(MyPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MyPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
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
        
        GeneralPath path = new GeneralPath();
        
        
        path.moveTo (curve.controlX.get(0), curve.controlY.get(0));
        
        path.curveTo(   curve.controlX.get(1),
                        curve.controlY.get(1),
                        curve.controlX.get(2),
                        curve.controlY.get(2),
                        curve.controlX.get(3),
                        curve.controlY.get(3));
        
        g.draw(path);
        
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
        paintNURBS(g2,Main.drawCurve);
    }  
}

