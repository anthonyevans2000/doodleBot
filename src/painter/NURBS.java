package painter;

import dataTypes.NurbsCurve;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.*;
import static javax.swing.JComponent.WHEN_FOCUSED;
import javax.swing.event.*;
import javax.swing.table.*;
import main.Main;

public class NURBS
{
    NURBSPanel nurbsPanel;
    JFrame f;
    //JPanel east;
   
    public NURBS()
    {
       // east = new JPanel(new GridLayout(0,1));
        f = new JFrame();
        f.setJMenuBar(getMenuBar());
        //f.add(east, "East");
        setGUI();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(Main.canvasX,Main.canvasY);
        f.setLocation(0,0);
        f.setVisible(true);
    }
 
    private void setGUI()
    {
        //east.removeAll();
        if(nurbsPanel != null)
            f.remove(nurbsPanel);
        nurbsPanel = new NURBSPanel();
        PointManager pointManager = new PointManager(nurbsPanel);
        nurbsPanel.addMouseListener(pointManager);
        nurbsPanel.addMouseMotionListener(pointManager);
        KnotDisplay knotDisplay = new KnotDisplay(nurbsPanel);
        WeightsPanel weightsPanel = new WeightsPanel(nurbsPanel);
        //east.add(knotDisplay);
        //east.add(weightsPanel);
        //east.revalidate();
        f.add(nurbsPanel);
        f.validate();
        nurbsPanel.firstTime = true;
    }
 
    private JMenuBar getMenuBar()
    {
        JMenu menu = new JMenu("DoodleBot");
        ActionListener l = new ActionListener()
        {
             
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JMenuItem item = (JMenuItem)e.getSource();
                setGUI();
            }
        };
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        return menuBar;
    }
 
    public void main(String[] args)
    {
        new NURBS();
    }
}
 
class NURBSPanel extends JPanel
{
        ArrayList<GeneralPath> completeCurves = new ArrayList<GeneralPath>();
        ArrayList<GeneralPath> printedCurves = new ArrayList<GeneralPath>();

        ArrayList<double[][]> toPrint = new ArrayList<double[][]>();

        double[][] points = new double[0][];
        double[] knots;
        GeneralPath curve;
        int n,                  // points.length - 1    set in makeCurve
            m,                  // knots.length - 1
            p;                  // degree = m - n - 1
        NumberFormat nf;
        boolean firstTime;
        final int
            PAD    = 30,
            TICK   = 10,
            MARGIN = 2,
            X_MAX  = 15,
            Y_MAX  = 10;


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

            InputMap im = getInputMap(WHEN_FOCUSED);
            ActionMap am = getActionMap();

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "onEnter");
            am.put("onEnter", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Enter pressed
                    completeCurves.add(curve);
                    curve = new GeneralPath();
                    toPrint.add(points);
                    points = new double[0][];
                    firstTime = true;
                    repaint();
             }});            
            
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "onPrint");
            am.put("onPrint", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Enter pressed
                    printedCurves.addAll(completeCurves);
                    for(int i = 0; i < toPrint.size(); i++)
                    {
                        Main._matlab._toProcess.add(NurbsCurve.convert2PrintCoords(toPrint.get(i),X_MAX,Y_MAX));
                    }
                    toPrint.clear();
                    completeCurves.clear();
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
            if(firstTime)
                makeCurve();
            g2.setPaint(Color.green.darker());
            for(int i = 0; i< completeCurves.size(); i++)
            {
                g2.draw(completeCurves.get(i));
            }
            g2.setPaint(Color.red.darker());
            for(int i = 0; i< printedCurves.size(); i++)
            {
                g2.draw(printedCurves.get(i));
            }
            g2.setPaint(Color.blue);
            g2.draw(curve);
                //drawKnots(g2);
            drawPoints(g2);
        }

        private void makeCurve()
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



class PointManager extends MouseInputAdapter
    {
        NURBSPanel nurbsPanel;
        double[][] points;
        Point2D.Double offset;
        int selectedIndex;
        boolean dragging;

    public PointManager(NURBSPanel np)
    {
        nurbsPanel = np;
        points = nurbsPanel.points;
        offset = new Point2D.Double();
        dragging = false;
    }

        @Override
    public void mousePressed(MouseEvent e)
    {
        Point p = e.getPoint();
        Point2D.Double pView;
        if(e.isControlDown())
        {
            nurbsPanel.addPoint(e.getX(), e.getY());
            points = nurbsPanel.points;
        }
        else
        {
            for(int j = 0; j < points.length; j++)
            {
                pView = nurbsPanel.modelToView(points[j][0], points[j][1]);
                if(p.distance(pView) < 10)
                {
                    selectedIndex = j;
                    if(e.isShiftDown())
                    {
                        nurbsPanel.removePoint(selectedIndex);
                        points = nurbsPanel.points;
                    }
                    else
                    {
                        offset.x = p.x - pView.x;
                        offset.y = p.y - pView.y;
                        dragging = true;
                        break;
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        dragging = false;
    }

    public void mouseDragged(MouseEvent e)
    {
        if(dragging)
        {
            double x = e.getX() - offset.x;
            double y = e.getY() - offset.y;
            nurbsPanel.setPoint(selectedIndex, x, y);
        }
    }
}

class KnotDisplay extends JPanel
{
    NURBSPanel nurbsPanel;
    JTable table;
    JSlider slider;
    double scale;   
    boolean valueIsAdjusting;
    int selectedIndex;

    public KnotDisplay(NURBSPanel np)
    {
        nurbsPanel = np;
        valueIsAdjusting = false;
        selectedIndex = 0;
        setBorder(BorderFactory.createTitledBorder("knots"));
        setLayout(new BorderLayout());
        add(getTable(), "North");
        add(getSlider());
        add(getRadioPanel(), "South");
    }

    private void changeSelection()
    {
        // reset slider values
        double lo = nurbsPanel.knots[selectedIndex-1];
        double hi = nurbsPanel.knots[selectedIndex+1];
        scale = Math.rint((hi - lo) * 1000);
        int min = (int)(lo * scale);
        int max = (int)(hi * scale);
        boolean enabled = true;
        if(max - min == 0) enabled = false;
        slider.setEnabled(enabled);
        int value = (int)(nurbsPanel.knots[selectedIndex] * scale);
        valueIsAdjusting = true;
        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setValue(value);
        valueIsAdjusting = false;
    }



    private JTable getTable()
    {
    String[] headers = new String[3];
    Object[][]data = new Object[1][3];
    for(int col = 0; col < data[0].length; col++)
    {
        headers[col] = "";  
        data[0][col] = String.valueOf(1);   
    }
    table = new JTable(new DefaultTableModel(data, headers));
    TableCellRenderer renderer = table.getDefaultRenderer(String.class);
    ((JLabel)renderer).setHorizontalAlignment(JLabel.CENTER);
    table.setEnabled(false);
    return table;
}

    private JSlider getSlider()
    {
        double lo = 1;
        double hi = 100;
        scale = Math.rint((hi - lo) * 1000);
        int min = (int)(lo * scale);
        int max = (int)(hi * scale);
        int value = (int)(5 * scale);
        slider = new JSlider(JSlider.HORIZONTAL, min, max, value);
        if(max - min == 0)
        slider.setEnabled(false);
        return slider;
    }

    private JPanel getRadioPanel()
    {
        final JRadioButton[] buttons = new JRadioButton[4];
        ButtonGroup group = new ButtonGroup();
        ActionListener l = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JRadioButton radio = (JRadioButton)e.getSource();
                int index = -1;
                for(int j = 0; j < buttons.length; j++)
                    if(radio == buttons[j])
                    {
                        selectedIndex = j;
                        break;
                    }
                changeSelection();
            }
        };
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();  
        gbc.weightx = 1.0;
        for(int j = 0; j < 4; j++)
        {
            buttons[j] = new JRadioButton();
            group.add(buttons[j]);
            buttons[j].addActionListener(l);
            panel.add(buttons[j], gbc);
        }
        buttons[0].setEnabled(false);
        buttons[buttons.length-1].setEnabled(false);
        buttons[selectedIndex].setSelected(true);
        return panel;
    }
}

class WeightsPanel extends JPanel
{
    NURBSPanel nurbsPanel;  
    JTable table;
    JSlider slider;
    double scale;
    boolean valueIsAdjusting;
    int selectedIndex;

    public WeightsPanel(NURBSPanel np)
    {
        nurbsPanel = np;
        valueIsAdjusting = false;
        selectedIndex = 2;
        setBorder(BorderFactory.createTitledBorder("weights"));
        setLayout(new BorderLayout());
        add(getTable(), "North");
        add(getSlider());
        add(getRadioPanel(), "South");
    }
    
    private void changeSelection()
    {
        // reset slider value   
        double weight = nurbsPanel.points[selectedIndex][2];
        setScale(weight);
        int value = (int)(weight * scale);
        valueIsAdjusting = true;
        slider.setValue(value);
        valueIsAdjusting = false;
    }

    private void setScale(double weight)
    {
        if(weight < 1.0) scale = slider.getMaximum()/5;
        else scale = 2.0;
    }
    
    private JTable getTable()
    {
        String[] headers = new String[nurbsPanel.points.length];
        Object[][]data = new Object[1][nurbsPanel.points.length];
        for(int col = 0; col < data[0].length; col++)
        {
            headers[col] = "";
            data[0][col] = String.valueOf(nurbsPanel.points[col][2]);
        }
        table = new JTable(new DefaultTableModel(data, headers));
        TableCellRenderer renderer = table.getDefaultRenderer(String.class);
        ((JLabel)renderer).setHorizontalAlignment(JLabel.CENTER);
        table.setEnabled(false);
        return table;
    }

    private JSlider getSlider()
    {
        double weight = 4;
        setScale(weight);
        int value = (int)(weight * scale);
        slider = new JSlider(JSlider.HORIZONTAL, 0, 50, value);
        slider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                if(!valueIsAdjusting)
                {
                    double value = slider.getValue() / scale;
                }
            }
        });

        return slider;
    }

    private JPanel getRadioPanel()
    {
        final JRadioButton[] buttons = new JRadioButton[nurbsPanel.points.length];
        ButtonGroup group = new ButtonGroup();
        ActionListener l = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JRadioButton radio = (JRadioButton)e.getSource();
                int index = -1;
                for(int j = 0; j < buttons.length; j++)
                    if(radio == buttons[j])
                    {
                        selectedIndex = j;
                        break;
                    }
                changeSelection();
            }
        };
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        return panel;
    }
}
