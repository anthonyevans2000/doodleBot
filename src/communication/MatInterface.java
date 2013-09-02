/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import dataTypes.CurveVelocity;
import dataTypes.NurbsCurve;
import java.io.IOException;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;

/**
 *
 * @author anthony
 */
public class MatInterface {
    
    public static final String matAddress = "/home/anthony/.MATLAB/bin/matlab";
    public static final String codeAddress = "\'/home/anthony/Dropbox/DoodleBot/Design/Modules/3 Path Generation - PC/code/ControlToys\'";
    public MatlabProxy _proxy;
    
    public MatInterface() throws IOException, MatlabConnectionException, MatlabInvocationException {
        
        MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
                                        .setMatlabLocation(matAddress)
                                        //.setHidden(true)
                                        .build();
        
        MatlabProxyFactory factory = new MatlabProxyFactory(options);

        _proxy = factory.getProxy();
        _proxy.eval("cd(" + codeAddress + ")");
        
    }
    
    public CurveVelocity processNURBS(NurbsCurve curve, int sPoints, double timeStep) throws IOException, MatlabConnectionException, MatlabInvocationException {
        
        MatlabTypeConverter processor = new MatlabTypeConverter(_proxy);
        
        processor.setNumericArray("cp", new MatlabNumericArray(curve.controlpointArray(), null));
        processor.setNumericArray("knots", new MatlabNumericArray(curve.knotArray(), null));
        
        _proxy.eval("[xVel yVel] = nurbsSolver(cp',knots," + sPoints +"," + timeStep + ");");
       
        double[][] xVel = processor.getNumericArray("xVel").getRealArray2D();
        double[][] yVel = processor.getNumericArray("yVel").getRealArray2D();
        
        short[][] xVelInt = new short[1][xVel[0].length];
        short[][] yVelInt = new short[1][yVel[0].length];
        
        for( int i = 0; i < xVel[0].length; i++) {
            
            xVel[0][i] = 1000*xVel[0][i];
            yVel[0][i] = 1000*yVel[0][i];
            
            xVelInt[0][i] = (short) xVel[0][i];
            yVelInt[0][i] = (short) yVel[0][i];
            System.out.println(xVelInt[0][i]);
        } 
        
        CurveVelocity ans = new CurveVelocity(xVelInt,yVelInt, curve.controlX.get(0), curve.controlY.get(0));
        
        return ans;
    }
    
    public void quit() {
        //Disconnect the proxy from MATLAB
        _proxy.disconnect();
    }
    
}
