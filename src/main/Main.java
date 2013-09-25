/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import communication.MatInterface;
import communication.ServerThread;
import dataTypes.NurbsCurve;
import java.io.IOException;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import painter.NURBS;

/**
 *
 * @author
 */
public class Main {
    
    public static final int computerPortNumber = 6665;
    public static final int plcPortNumber = 6666;
    public static final int packetBufferLength = 10;
    
    public static final int socketTimeout = 100;
    
    public static final int sPoints = 100;
    public static final short timeStep = 60;
    public static final double timeStepDouble = timeStep*1.0/1000;
    
    
    public static final short maxAccelX = 2;
    public static final short maxAccelY = 1;
    public static final short maxVelX = 1000;
    public static final short maxVelY = 1000;
    
    public static final double maxTorqueX = 950*maxAccelX;
    public static final double maxTorqueY = 1000*maxAccelY;
   

    
    public static NurbsCurve drawCurve = new NurbsCurve();
    
    public static boolean readyToDraw = false;
    public static boolean readyToQuit = false;
    public static boolean commsReset = false;
    public static boolean curveReset = false;
    public static boolean processImage = false;
    
    public static final double xClkWiseLim = 4277;
    public static final double xAClkWiseLim = 4277;
    public static final double yClkWiseLim = 1676;
    public static final double yAClkWiseLim = 1676; 
    
    public static final int canvasX = 1370;
    public static final int canvasY = 750;
    
    public static final double xDim = xClkWiseLim + xAClkWiseLim;
    public static final double yDim = yClkWiseLim + yAClkWiseLim;   
            
    public static MatInterface _matlab;
    public static ServerThread _server;
    
    public static Thread _matlabThread;
    
    public static void main(String[] args) throws IOException, MatlabConnectionException, MatlabInvocationException {
        
        _server = new ServerThread("master",computerPortNumber);
        _matlab = new MatInterface();
        
        _matlabThread = new Thread(_matlab);
        _matlabThread.start();
        new Thread(_server).start();
        
         
        new NURBS();
         
        

    }
    
    private void quit() throws MatlabInvocationException {
        _matlab.quit();
        _server._socket.close();
        
    }
            
    
}
