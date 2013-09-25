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
        
        public void calculateKnots() {
            int nKnots = controlX.size() -2;
            knots.add(0.d);
            knots.add(0.d);
            knots.add(0.d);
            for(int i = 1; i < nKnots; i++)
            {
                knots.add(1.0*i/nKnots);
            }
            knots.add(1.d);
            knots.add(1.d);
            knots.add(1.d);
        }
        
        public static NurbsCurve convert2PrintCoords(double[][] curve, int canvasXDim, int canvasYDim) {
            NurbsCurve ans = new NurbsCurve();
            for(int i = 0; i < curve.length; i++) {
                ans.controlX.add(i, (short)((curve[i][0] - canvasXDim/2)*Main.xDim/canvasXDim));
                ans.controlY.add(i, (short)((curve[i][1]  - canvasYDim/2)*Main.yDim/canvasYDim));
            }
            ans.calculateKnots();
            return ans; 
        }
}
