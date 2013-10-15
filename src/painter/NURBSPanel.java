/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package painter;

import dataTypes.NurbsCurve;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import static javax.swing.JComponent.WHEN_FOCUSED;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import main.Main;

/**
 *
 * @author anthony
 */
public class NURBSPanel extends JPanel
{
        ArrayList<GeneralPath> completeCurves = new ArrayList<GeneralPath>();
        ArrayList<GeneralPath> printedCurves = new ArrayList<GeneralPath>();

        public ArrayList<double[][]> toPrint = new ArrayList<double[][]>();

        double[][] points = new double[0][];
        double[] knots;
        GeneralPath curve;
        int n,                  // points.length - 1    set in makeCurve
            m,                  // knots.length - 1
            p;                  // degree = m - n - 1
        NumberFormat nf;
        boolean firstTime;
        boolean isControlDown = true;
        boolean isShiftDown  = false;
        final int
            PAD    = 30,
            TICK   = 10,
            MARGIN = 2,
            X_MAX  = Main.X_MAX,
            Y_MAX  = Main.Y_MAX;


        public NURBSPanel()
        {
            curve = new GeneralPath();
            nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(1);
            addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentResized(ComponentEvent e)
                {
                    if(!firstTime)
                    {
                        firstTime = true;
                        repaint();
                    }
                }
            });

            InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = getActionMap();

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "onEnter");
            am.put("onEnter", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Enter pressed
                    if(points.length > 0)
                    {
                        toPrint.add(points);
                    }
                    points = new double[0][];
                    firstTime = true;
                    repaint();
             }});            
            
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "onPrint");
            am.put("onPrint", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Enter pressed
                    double[][] backup = points;
                    for(int i = 0; i < toPrint.size(); i++)
                    {
                        Main._matlab._toProcess.add(NurbsCurve.convert2PrintCoords(toPrint.get(i)));
                        points = toPrint.get(i);
                        makeCurve();
                        printedCurves.add((GeneralPath)curve.clone());
                    }
                    points = backup;
                    makeCurve();
                    toPrint.clear();
                    repaint();
             }});            
            
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE , 0), "onBackspace");
            am.put("onBackspace", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Enter pressed
                    points = new double[0][];
                    firstTime = true;
                    repaint();
             }});
            
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C , 0), "onClear");
            am.put("onClear", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Enter pressed
                    points = new double[0][];
                    toPrint.clear();
                    firstTime = true;
                    repaint();
             }});
                        
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "onReset");
            am.put("onReset", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Enter pressed
                    points = new double[0][];
                    toPrint.clear();
                    printedCurves.clear();
                    Main.commsReset = true;
                    Main.curveReset = true;
                    firstTime = true;
                    repaint();
             }});
                        
            
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "onImage");
            am.put("onImage", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Enter pressed
                    Main.processImage = true;
             }});

        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            drawAxes(g2);
            drawConvexPolyline(g2);
            
            g2.setPaint(Color.green.darker());
            g2.setStroke(new BasicStroke(2));
            double[][] backup = points;
            for(int i = 0; i< toPrint.size(); i++)
            {
                points = toPrint.get(i);
                makeCurve();
                g2.draw(curve);
            }
            points = backup;
            makeCurve();
            g2.setPaint(Color.red.darker());
            for(int i = 0; i< printedCurves.size(); i++)
            {
                g2.draw(printedCurves.get(i));
            }
            g2.setPaint(Color.blue);
            g2.draw(curve);
            
            drawPoints(g2);
        }

        public void makeCurve()
        {
            curve.reset();
            final double STEPS = 20.0;
            boolean firstValue = true;
            n = points.length - 1;
            if(n>1)
            {
                knots = new double[n+4];
                knots[0] = 0;
                knots[1] = 0;
                knots[2] = 0;
                knots[n+1] = 1;        
                knots[n+2] = 1;
                knots[n+3] = 1;
                for(int i = 2; i< n; i++)
                {
                    knots[i+1] = 1.0*i/(n+1); 
                }
            }
            else if(n == 1)
            {
                knots = new double[4];
                knots[0] = 0;
                knots[1] = 0;
                knots[2] = 1;
                knots[3] = 1;
            }
            else
            {
                knots = new double[3];
                knots[0] = 0;
                knots[1] = 0;
                knots[2] = 0;
            }
            m = knots.length - 1;
            p = m - n - 1;
            // curve domain is [u[p], u[m-p]]
            // so curve is defined on non-zero intervals from u[p] up to u[m-p]
            for(int j = p; j < m-p; j++)
            {
                double spanInterval = knots[j+1] - knots[j];
                if(spanInterval == 0)       // no interval -> no curve
                    continue;
                double dt = spanInterval / STEPS;
                double u;
                for(int k = 0; k <= STEPS; k++)
                {
                    u = knots[j] + k * dt;
                    //System.out.println("u = " + nf.format(u));
                    Point2D.Double pv = getValue(u, j);
                    if(firstValue)
                    {
                        curve.moveTo((float)pv.x, (float)pv.y);
                        firstValue = false;
                    }
                    else
                        curve.lineTo((float)pv.x, (float)pv.y);
                }
            }
            firstTime = false;
        }

        private double[] getBasisValues(double u, int k, int p)
        {
            double[] N = new double[points.length];
            for(int j = 0; j < N.length; j++)
                N[j] = 0.0;
            // u is in interval [u[k], u[k+1]]
            // start with 0-degree coefficient, guaranteed to be non-zero
            N[k] = 1.0;
            // and triangulate toward coefficients of degree p
            for(int d = 1; d <= p; d++)
            {
                N[k-d] = ((knots[k+1] - u) / (knots[k+1] - knots[k-d+1])) * N[k-d+1];
                for(int i = k-d+1; i <= k-1; i++)
                    N[i] = ((u - knots[i]) / (knots[i+d] - knots[i])) * N[i] + ((knots[i+d+1] - u) / (knots[i+d+1] - knots[i+1])) * N[i+1];
                    N[k] = ((u - knots[k]) / (knots[k+d] - knots[k])) * N[k];
            }

            return N;
        }



    private Point2D.Double getValue(double u, int k)
    {
        double[] N = getBasisValues(u, k, p);
        // the curve over knot interval [u[k], u[k+1]] has at most p+1
        // non-zero coefficients: N[k-p][p], N[k-p+1][p], ... N[k][p]
        double x = 0.0, y = 0.0, w = 0.0;
        for(int j = k-p; j <= k; j++)
        {
            if(j < 0)
                continue;
            x += N[j] * points[j][0] * points[j][2];
            y += N[j] * points[j][1] * points[j][2];
            w += N[j] * points[j][2];
        }   
        return modelToView(x/w, y/w);
    }


    private void drawConvexPolyline(Graphics2D g2) {
        g2.setPaint(new Color(240,180,180));
        double lastX = 0, lastY = 0;
        Point2D.Double pv;

        for(int j = 0; j < points.length; j++)
        {
            pv = modelToView(points[j][0], points[j][1]);
            if(j > 0)
                g2.draw(new Line2D.Double(pv.x, pv.y, lastX, lastY));
            lastX = pv.x;
            lastY = pv.y;
        }
    }



    private void drawPoints(Graphics2D g2)
    {
        g2.setPaint(Color.red);
        for(int j = 0; j < points.length; j++)
        {
            Point2D.Double pv = modelToView(points[j][0], points[j][1]);
            g2.fill(new Ellipse2D.Double(pv.x - 5, pv.y - 5, 10, 10));
        }

    }



    public Point2D.Double modelToView(double x, double y)
    {
        double h = getHeight();
        Point2D.Double pView = new Point2D.Double();
        pView.x = PAD + x * (getWidth() - 2*PAD) / X_MAX;
        pView.y = h - PAD - (y * (h - 2*PAD) / Y_MAX);
        return pView;
    }



    private Point2D.Double viewToModel(double x, double y)
    {
        double w = getWidth();
        double h = getHeight();
        Point2D.Double pModel = new Point2D.Double();
        pModel.x = (x - PAD) * X_MAX / (w - 2*PAD);
        pModel.y = (h - PAD - y) * Y_MAX / (h - 2*PAD);
        return pModel;
    }



    private void drawAxes(Graphics2D g2)
    {   
        Font font = new Font("lucida sans", Font.PLAIN, 14);
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        double w = getWidth();
        double h = getHeight();
        double xInc = (w - 2*PAD) / X_MAX;
        double yInc = (h - 2*PAD) / Y_MAX;
        double x1 = PAD, y1 = PAD, x2 = w-PAD, y2 = h-PAD;
        g2.setPaint(new Color(200,220,220));
        // grid lines - vertical lines across
        for(int j = 0; j <= X_MAX; j++)
        {
            g2.draw(new Line2D.Double(x1, y1, x1, y2));
            x1 += xInc;
        }
        // horizontal lines down
        x1 = PAD;
        for(int j = 0; j < Y_MAX; j++)
        {
            g2.draw(new Line2D.Double(x1, y1, x2, y1));
            y1 += yInc;
        }
        g2.setPaint(Color.black);
        // ordinate
        g2.draw(new Line2D.Double(PAD, PAD, PAD, h-PAD));
        // tic marks
        x1 = PAD - TICK; y1 = PAD; x2 = PAD;
        for(int j = 0; j <= Y_MAX; j++)
        {
            g2.draw(new Line2D.Double(x1, y1, x2, y1));
            y1 += yInc;
        }

        // labels
        for(int j = 0; j <= Y_MAX; j++)
        {
            String s = String.valueOf(Y_MAX - j);
            float width = (float)font.getStringBounds(s,frc).getWidth();
            float height = font.getLineMetrics(s, frc).getAscent();
            float sx = PAD - TICK - MARGIN - width;
            float sy = (float)(PAD + j * yInc + height/2);
            g2.drawString(s, sx, sy);
        }

        // abcissa
        g2.draw(new Line2D.Double(PAD, h-PAD, w-PAD, h-PAD));
        // tic marks
        x1 = PAD; y1 = h-PAD; y2 = h-PAD+TICK;
        for(int j = 0; j <= X_MAX; j++)
        {
            g2.draw(new Line2D.Double(x1, y1, x1, y2));
            x1 += xInc;
        }

        // labels
        for(int j = 0; j <= X_MAX; j++)
        {
            String s = String.valueOf(j);
            float width = (float)font.getStringBounds(s,frc).getWidth();
            float height = font.getLineMetrics(s, frc).getAscent();
            float sx = (float)(PAD + j * xInc - width/2);
            float sy = (float)(h - PAD + TICK + MARGIN + height);
            g2.drawString(s, sx, sy);
        }
    }

    public void setPoint(int index, double x, double y)
    {
        Point2D.Double pModel = viewToModel(x, y);
        points[index][0] = pModel.x;
        points[index][1] = pModel.y;
        firstTime = true;
        repaint();
    }
    
    public void removePoint(int index)
    {
        double[][] newPoints;
        newPoints = new double[points.length-1][3];
        int j = 0;
        for(int i = 0; i < points.length; i++)
        {
            if(i == index)
            {
                continue;
            }
            newPoints[j][0] = points[i][0];
            newPoints[j][1] = points[i][1];
            newPoints[j][2] = points[i][2];
            j++;
        }
        points = newPoints;
        firstTime = true;
        repaint();
    }

    public void addPoint(double x, double y)
    {
        double[][] newPoints;
        newPoints = new double[points.length+1][3];

        for(int i = 0; i < points.length; i++)
        {
            newPoints[i][0] = points[i][0];
            newPoints[i][1] = points[i][1];
            newPoints[i][2] = points[i][2];

        }
        Point2D.Double pModel = viewToModel(x, y);
        newPoints[points.length][0] = pModel.x;
        newPoints[points.length][1] = pModel.y;
        newPoints[points.length][2] = 1.0;
        points = newPoints;
        firstTime = true;
        repaint();
    }

}