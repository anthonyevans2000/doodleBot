package dataTypes;

import java.util.ArrayList;
import main.Main;

public class NurbsCurve {
	
	public ArrayList<Short> controlX;
        public ArrayList<Short> controlY;
	public ArrayList<Double> knots;
	
        public NurbsCurve() {
            
            controlX = new ArrayList<Short>();
            controlY = new ArrayList<Short>();
            knots = new ArrayList<Double>();
            initialiseNURBS();
        }
        
	public NurbsCurve(ArrayList<Short> x,ArrayList<Short> y, ArrayList<Double> k)
        {
            controlX = x;
            controlY = y;
            
            knots = k;
        }

        
        public double[][] controlpointArray() {
            double[][] cp = new double[2][controlX.size()];
            for(int i = 0; i < controlX.size(); i++) {
                cp[0][i] = (double) controlX.get(i);
                cp[1][i] = (double) controlY.get(i);
            }
            return cp;
        }
        
        public double[][] knotArray() {
            double[][] knot = new double[1][knots.size()];
            for(int i = 0; i < knots.size(); i++) {
                knot[0][i] = knots.get(i);
            }
            return knot;
        }
        
        public final void initialiseNURBS() {
        
        controlX.add((short)10);
        controlY.add((short)10);
        controlX.add((short)80);
        controlY.add((short)80);
        controlX.add((short)180);
        controlY.add((short)100);
        controlX.add((short)10);
        controlY.add((short)100);
        
        knots.add(0,0.d);
        knots.add(1,0.d);
        knots.add(2,0.d);
        knots.add(3,0.5);
        knots.add(4,1.d);
        knots.add(5,1.d);
        knots.add(6,1.d);
        
    }
        
        public void printCurve() {
            for(int i = 0; i < controlX.size(); i++) {
                System.out.println(" " + i );
                System.out.println(controlX.get(i)+ ", " + controlY.get(i));
            }
        }
        
        public NurbsCurve cloneCurve() {
            NurbsCurve ans = new NurbsCurve();
            ans.controlX = (ArrayList<Short>) controlX.clone();
            ans.controlY = (ArrayList<Short>) controlY.clone();
            ans.knots = (ArrayList<Double>) knots.clone();
            return ans;
        }
        
        public NurbsCurve convert2PrintCoords() {
            NurbsCurve ans = new NurbsCurve();
            for(int i = 0; i < controlX.size(); i++) {
                ans.controlX.set(i, (short)((controlX.get(i) - Main.canvasXDim/2)*Main.xConvertMultiplier));
                ans.controlY.set(i, (short)((controlY.get(i) - Main.canvasYDim/2)*Main.yConvertMultiplier));
            }
            ans.knots = (ArrayList<Double>) knots.clone();
            for(int i = 0; i < ans.controlX.size(); i ++) {
                System.out.println("CP X: " + ans.controlX.get(i) + " Y :" + ans.controlY.get(i));
            }
            return ans; 
        }
}
