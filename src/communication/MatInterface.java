/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import dataTypes.CurveVelocity;
import dataTypes.NurbsCurve;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Main;
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
public class MatInterface implements Runnable{
        
    public final String matAddress = "/home/anthony/.MATLAB/bin/matlab"; //Where your matlab binary file is
    public final String codeAddress = "\'/home/anthony/Dropbox/DoodleBot/Design/Modules/3 Path Generation - PC/code/ControlToys\'";
    
    public MatlabProxy _proxy;
    
    public ArrayList<NurbsCurve> _toProcess = new ArrayList<NurbsCurve>();

    
    public MatInterface() throws IOException, MatlabConnectionException, MatlabInvocationException {
        
        MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
                                        .setMatlabLocation(matAddress)
                                        //.setHidden(true)
                                        .build();
        
        MatlabProxyFactory factory = new MatlabProxyFactory(options);

        _proxy = factory.getProxy();
        _proxy.eval("cd(" + codeAddress + ")");
        
    }
    
    @Override
    public void run() {
        try {
            while (!Main.readyToQuit) { 
                
                Main._matlabThread.sleep(500);
                
                if(Main.curveReset) {
                    System.out.println("Resetting Matlab queue");
                    _toProcess.clear();
                    Main.curveReset = false;
                }

                
                if(_toProcess.size() < 1) {
                        continue;
                    }else {

                            //Process curve, then add it to the server queue.
                            System.out.println("Processing");
                            System.out.flush();
                            CurveVelocity completed = processNURBS(_toProcess.get(0), Main.sPoints, Main.timeStepDouble);
                            if(!Main.curveReset) Main._server._profiles.add(completed);
                            _toProcess.remove(0);
                            System.out.println("Processed Curve - " + _toProcess.size() + " remain");
                    }
            }
            } catch (IOException ex) {
                        Logger.getLogger(MatInterface.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (MatlabConnectionException ex) {
                        Logger.getLogger(MatInterface.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (MatlabInvocationException ex) {
                        Logger.getLogger(MatInterface.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
            Logger.getLogger(MatInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            quit();
        } catch (MatlabInvocationException ex) {
            Logger.getLogger(MatInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public CurveVelocity processNURBS(NurbsCurve curve, int sPoints, double timeStep) throws IOException, MatlabConnectionException, MatlabInvocationException {
        
        MatlabTypeConverter processor = new MatlabTypeConverter(_proxy);
        
        processor.setNumericArray("cp", new MatlabNumericArray(curve.controlpointArray(), null));
        processor.setNumericArray("knots", new MatlabNumericArray(curve.knotArray(), null));
        
        _proxy.eval("[xVel yVel] = nurbsSolver(cp',knots," + sPoints +"," + timeStep + ", " + Main.maxTorqueX +", " + Main.maxTorqueY + ");");
       
        double[][] xVel = processor.getNumericArray("xVel").getRealArray2D();
        double[][] yVel = processor.getNumericArray("yVel").getRealArray2D();
        
        short[][] xVelInt = new short[1][xVel[0].length];
        short[][] yVelInt = new short[1][yVel[0].length];
        
        for( int i = 0; i < xVel[0].length; i++) {
            
            xVel[0][i] = 50*xVel[0][i];
            yVel[0][i] = 50*yVel[0][i];
            
            xVelInt[0][i] = (short) xVel[0][i];
            yVelInt[0][i] = (short) yVel[0][i];
        } 
        
        CurveVelocity ans = new CurveVelocity(xVelInt,yVelInt, curve.controlX.get(0), curve.controlY.get(0));
        
        return ans;
    }
    
    public void quit() throws MatlabInvocationException {
        //Disconnect the proxy from MATLAB
        _proxy.exit();
        _proxy.disconnect();
    }
    
}
