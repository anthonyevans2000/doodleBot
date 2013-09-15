/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataTypes;

import java.util.ArrayList;

/**
 *
 * @author anthony
 */
public class CurveVelocity {
    public ArrayList<Short> _xVel;
    public ArrayList<Short> _yVel;
    public short _xInitial;
    public short _yInitial;
  
    public CurveVelocity(){
        _xVel = new ArrayList<Short>();
        _yVel = new ArrayList<Short>();
    }
    public CurveVelocity(short[][] xVel, short[][]yVel, short xInit, short yInit) {
        _xVel = new ArrayList<Short>();
        _yVel = new ArrayList<Short>();
        
        _xInitial = xInit;
        _yInitial = yInit;
        
        for(int i = 0; i<xVel[0].length; i++) {
        _xVel.add(xVel[0][i]);
        _yVel.add(yVel[0][i]);
        }
    }
    
    public CurveVelocity circularCurveTest() {
        CurveVelocity ans = new CurveVelocity();
        ans._xInitial = 0;
        ans._yInitial = 0;
        for(int i = 0; i < 50; i++) {
            ans._xVel.add((short)(-1000*Math.sin(i*2*Math.PI/50)));
            ans._yVel.add((short)(1000*Math.cos(i*2*Math.PI/50)));
        }
        return ans;
    }
    
}
