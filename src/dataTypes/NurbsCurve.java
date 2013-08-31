package dataTypes;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class NurbsCurve {
	
	public ArrayList<Point2D.Double> controlPoints;
	public ArrayList<Double> knots;
	
	public NurbsCurve(ArrayList<Point2D.Double> cp, ArrayList<Double> k)
        {
            controlPoints = cp;
            knots = k;
        }

        public ArrayList<Point2D> Convert2QuadraticCurve(){
            ArrayList<Point2D> list = new ArrayList<Point2D>();
            list.add(controlPoints.get(0));
            
            list.add(controlPoints.get(controlPoints.size()-1));
           return list;
        }
}
