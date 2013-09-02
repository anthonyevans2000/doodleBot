/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import communication.MatInterface;
import communication.ServerThread;
import dataTypes.CurveVelocity;
import dataTypes.NurbsCurve;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import painter.NURBSdraw;

/**
 *
 * @author anthony
 */
public class Main {
    
    public static final int computerPortNumber = 6666;
    public static final int plcPortNumber = 6666;
    public static final int packetBufferLength = 256;
    
    public static final int sPoints = 100;
    public static final double timeStep = 0.06;
    
    public static NurbsCurve drawCurve = new NurbsCurve();
    
    public static boolean readyToDraw = false;
    
    public static MatInterface _matlab;
    public static ServerThread _server;
    
    public static void main(String[] args) throws IOException, MatlabConnectionException, MatlabInvocationException {
        
        _server = new ServerThread("master",computerPortNumber);
        _server.start();
         
        _matlab = new MatInterface();
         
        NURBSdraw.createAndShowGUI();
         
        

    }
    
    private void quit() {
        _matlab.quit();
        _server._socket.close();
        
    }
            
}
