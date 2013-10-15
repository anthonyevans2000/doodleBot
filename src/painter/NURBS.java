package painter;

import dataTypes.NurbsCurve;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import main.Main;

public class NURBS
{
    public NURBSPanel nurbsPanel;
    JFrame f;
    JPanel north;
   
    public NURBS() throws IOException
    {
        north = new JPanel(new GridLayout(0,1));
        f = new JFrame();
        f.setJMenuBar(getMenuBar());
        f.add(north, "North");
        setGUI();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(Main.canvasX,Main.canvasY);
        f.setLocation(0,0);
        f.setVisible(true);
    }
 
    private void setGUI() throws IOException
    {
        north.removeAll();
        if(nurbsPanel != null)
            f.remove(nurbsPanel);
        nurbsPanel = new NURBSPanel();
        PointManager pointManager = new PointManager(nurbsPanel);
        nurbsPanel.addMouseListener(pointManager);
        nurbsPanel.addMouseMotionListener(pointManager);
        SettingsDisplay settingsDisplay = new SettingsDisplay(nurbsPanel);
        north.add(settingsDisplay);
        north.revalidate();
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
                try {
                    setGUI();
                } catch (IOException ex) {
                    Logger.getLogger(NURBS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        return menuBar;
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
        
        if(nurbsPanel.isControlDown)
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
                    if(nurbsPanel.isShiftDown)
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

class SettingsDisplay extends JPanel
{
    NURBSPanel nurbsPanel;
    JTable table;
    JSlider slider;
    double scale;   
    boolean valueIsAdjusting;
    int selectedIndex;
    JFileChooser fc = new JFileChooser();
    //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

    public SettingsDisplay(NURBSPanel np) throws IOException
    {
        nurbsPanel = np;
        valueIsAdjusting = false;
        selectedIndex = 0;
        setBorder(BorderFactory.createLineBorder(Color.blue));
        setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        //add(getTable(), "West");
        
        Box vbox0 = Box.createVerticalBox();
        add(vbox0);
        Box vbox1 = Box.createHorizontalBox();
        vbox0.add(vbox1);
        vbox1.add(getXTorqueSlider(),"East");
        vbox1.add(Box.createRigidArea(new Dimension(15,0)));
        vbox1.add(getYTorqueSlider(),"West");
        vbox1.add(Box.createRigidArea(new Dimension(15,0)));
        vbox1.add(getSSlider());
        Box titlebox = Box.createHorizontalBox();
        vbox0.add(titlebox,"South");
        JLabel left = new JLabel("X Force");
        titlebox.add(left);
        titlebox.add(Box.createRigidArea(new Dimension(15,0)));
        JLabel mid = new JLabel("Y Force", JLabel.CENTER);
        titlebox.add(mid);
        titlebox.add(Box.createRigidArea(new Dimension(15,0)));
        JLabel right = new JLabel("Optimisation", JLabel.RIGHT);
        right.setAlignmentX(JLabel.RIGHT);
        titlebox.add(right);
        Box vbox2 = Box.createVerticalBox();
        add(vbox2);
        BufferedImage myPicture = ImageIO.read(new File(Main.bannerPath));
        JLabel banner = new JLabel(new ImageIcon(myPicture));
        banner.setAlignmentX(JLabel.LEFT);
        vbox2.add(banner,"North");
        JPanel radio = getRadioPanel();
        radio.setAlignmentX(JLabel.LEFT);
        vbox2.add(radio,"South");
    }

    private void changeSSelection()
    {
        // set slider values
        int min = (int)(1);
        int max = (int)(1000);
        boolean enabled = true;
        slider.setEnabled(enabled);
        int value = (int)(Main.sPoints);
        valueIsAdjusting = true;
        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setValue(value);
        valueIsAdjusting = false;
    }

      private JSlider getSSlider()
    {

        int min = (int)(15);
        int max = (int)(999);
        int value = (int)(Main.sPoints);
        slider = new JSlider(JSlider.VERTICAL, min, max, value);
        slider.setEnabled(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(200);
        slider.setLabelTable(slider.createStandardLabels(200,200));
        slider.setPaintLabels(true);
        slider.addChangeListener(new SSliderListener());
        return slider;
    }
    class SSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                Main.sPoints = (int)source.getValue();
            }    
        }
    }
    
        private void changeXTorqueSelection()
    {
        // set slider values
        int min = (int)(1);
        int max = (int)(3000);
        boolean enabled = true;
        slider.setEnabled(enabled);
        int value = (int)(Main.maxTorqueX);
        valueIsAdjusting = true;
        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setValue(value);
        valueIsAdjusting = false;
    }

      private JSlider getXTorqueSlider()
    {
        int min = (int)(1);
        int max = (int)(3000);
        int value = (int)(Main.maxTorqueX);
        slider = new JSlider(JSlider.VERTICAL, min, max, value);
        slider.setEnabled(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(1000);
        slider.setLabelTable(slider.createStandardLabels(1000,1000));
        slider.setPaintLabels(true);
        slider.addChangeListener(new XTorqueSliderListener());
        return slider;
    }
    class XTorqueSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                Main.maxTorqueX = (int)source.getValue();
            }    
        }
    }

    private void changeYTorqueSelection()
    {
        // set slider values
        int min = (int)(1);
        int max = (int)(5000);
        boolean enabled = true;
        slider.setEnabled(enabled);
        int value = (int)(Main.maxTorqueY);
        valueIsAdjusting = true;
        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setValue(value);
        valueIsAdjusting = false;
    }

      private JSlider getYTorqueSlider()
    {
        int min = (int)(1);
        int max = (int)(5000);
        int value = (int)(Main.maxTorqueY);
        slider = new JSlider(JSlider.VERTICAL, min, max, value);
        slider.setEnabled(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(1000);
        slider.setLabelTable(slider.createStandardLabels(1000,1000));
        slider.setPaintLabels(true);
        slider.addChangeListener(new YTorqueSliderListener());
        return slider;
    }
    class YTorqueSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                Main.maxTorqueY = (int)source.getValue();
            }    
        }
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

  

    private JPanel getRadioPanel()
    {
        final JRadioButton[] buttons = new JRadioButton[3];
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
                changeSSelection();
                changeXTorqueSelection();
                changeYTorqueSelection();
            }
        };
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); 
        gbc.weightx = 1.0;
        panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        buttons[0] = new JRadioButton("<HTML>Create <br> Control Points </HTML>");
        group.add(buttons[0]);
        buttons[0].addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
               nurbsPanel.isControlDown = true;
               nurbsPanel.isShiftDown = false;
            }
        });      
        panel.add(buttons[0], gbc);
        
        buttons[1] = new JRadioButton("<HTML>Delete <br> Control Points </HTML>");
        group.add(buttons[1]);
        buttons[1].addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
               nurbsPanel.isControlDown = false;
               nurbsPanel.isShiftDown = true;
            }
        });      
        panel.add(buttons[1], gbc);
                
        buttons[2] = new JRadioButton("<HTML>Move <br> Control Points </HTML>");
        group.add(buttons[2]);
        buttons[2].addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
               nurbsPanel.isControlDown = false;
               nurbsPanel.isShiftDown = false;
            }
        });      
        panel.add(buttons[2], gbc);
        
        JButton b1 = new JButton("<HTML>Reset<br> <center><b>(ESC)</b></centre> </HTML>");
        b1.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                nurbsPanel.points = new double[0][];
                nurbsPanel.toPrint.clear();
                nurbsPanel.printedCurves.clear();
                Main.commsReset = true;
                Main.curveReset = true;
                nurbsPanel.firstTime = true;
                nurbsPanel.repaint();
            }
        });      
        group.add(b1);
        panel.add(b1);
        
        JButton b2 = new JButton("<HTML>Print <br> <center><b>(P)</b></centre> </HTML>");
        b2.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                    double[][] backup = nurbsPanel.points;
                    for(int i = 0; i < nurbsPanel.toPrint.size(); i++)
                    {
                        Main._matlab._toProcess.add(NurbsCurve.convert2PrintCoords(nurbsPanel.toPrint.get(i)));
                        nurbsPanel.points = nurbsPanel.toPrint.get(i);
                        nurbsPanel.makeCurve();
                        nurbsPanel.printedCurves.add((GeneralPath)nurbsPanel.curve.clone());
                    }
                    nurbsPanel.points = backup;
                    nurbsPanel.makeCurve();
                    nurbsPanel.toPrint.clear();
                    nurbsPanel.repaint();
            }
        });      
        group.add(b2);
        panel.add(b2);
        
        JButton b3 = new JButton("<HTML>Clear Canvas<br> <center><b>(C)</b></centre> </HTML>");
        b3.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                    nurbsPanel.points = new double[0][];
                    nurbsPanel.toPrint.clear();
                    nurbsPanel.firstTime = true;
                    nurbsPanel.repaint();
            }
        });      
        group.add(b3);
        panel.add(b3);
        
        JButton b5 = new JButton("<HTML>Finish Current Curve<br> <center><b>(Enter)</b></centre> </HTML>");
        b5.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                    nurbsPanel.toPrint.add(nurbsPanel.points);
                    nurbsPanel.points = new double[0][];
                    nurbsPanel.firstTime = true;
                    nurbsPanel.repaint();
            }
        });      
        group.add(b5);
        panel.add(b5);
        
        

        JButton b6 = new JButton("<HTML>Process Image<br> <center><b>(I)</b></centre> </HTML>");
        b6.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        Main.imagePath = file.getAbsolutePath();
                        Main.imagePath.replaceAll("\\p{Cntrl}", "");
                        Main.processImage = true;
                    }
                    
            }
        });      
        group.add(b6);
        panel.add(b6);
        
        
        buttons[selectedIndex].setSelected(true);
        return panel;
    }
}

