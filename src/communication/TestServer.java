/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import java.io.IOException;


/**
 *
 * @author anthony
 */
public class TestServer {
    
    public static final int computerPortNumber = 6665;
    public static ServerThread _server;
    
    public static void main(String[] args) throws IOException, InterruptedException {
        _server = new ServerThread("master",computerPortNumber);
        new Thread(_server).start();
        
        while(true) {
            if(_server._profiles.isEmpty()) {
                _server._profiles.add(new dataTypes.CurveVelocity());
            }
            Thread.sleep(4000);
        }
        
        
    }
}
