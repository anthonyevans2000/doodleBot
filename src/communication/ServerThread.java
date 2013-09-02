package communication;
import dataTypes.CurveVelocity;
import java.io.*;
import java.net.*;
import main.Main;



public class ServerThread extends Thread {
	
	//Server Variables
	//ServerSocket DataServer;
	public DatagramSocket _socket;
	protected BufferedReader in = null;
	//public DataInputStream is = null;
	private boolean terminateRequested = false;
	
	
	public ServerThread() throws IOException {
		this("ServerThread",6666);
    }
	
    public ServerThread(String name, int portNumber) throws IOException {
        super(name);
        _socket = new DatagramSocket(portNumber);
        //Should run a check on the integrity of the data to be sent here
	}
    
    public void initialiseConnection() throws IOException{

        
    }
    

    /**
     * This function will serve velocity instructions upon receipt of a request packet.
     * Currently will remain in the loop until out of instructions. Probably should 
     * separate out the thread so more processing can take place in the background
     * @param profile Velocity profile to transmit
     * @param port The transmitting port
     * @throws IOException 
     */
    public void serveInstruction(CurveVelocity profile, int port) throws IOException {

        /*HEADER
         * "H"
         * "I"
         * X clkwise boundary (short)
         * X Cclockwise (short)
         * Y clkwise boundary (short)
         * Y Cclockwise (short)
         * 
         */
        
        /*
         * Data Transfer
         * Requested command number (from zero) (short)
         *  If -1 -> transfer complete.
         */
        
        byte[] buf = new byte[Main.packetBufferLength];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        
        _socket.receive(packet);
        
        String decoded = new String(packet.getData(), "UTF-8");
        System.out.println(decoded);
        
        //Send header
        DoodlePacket header = new DoodlePacket();
        header.header((short)50, (short)100, (short)1000, (short)1000, (short)profile._xVel.size(), (short)60, packet.getAddress(), Main.plcPortNumber);
        _socket.send(header._packet);
        System.out.println("Sent header");
        
        //Wait for packet before sending another
        _socket.receive(packet);
        
        //Send move instruction
        DoodlePacket moveInstruction = new DoodlePacket();
        moveInstruction.instructionConstructor((short)0, profile._xInitial, profile._yInitial);
        _socket.send(moveInstruction._packet);
     
        System.out.println("Entering send-loop");
        int i = 0;
        while(i< profile._xVel.size()) {
            //need to do sanity checking and logic on recieved packet    
            _socket.receive(packet);
            
            System.out.println(i);
            
            DoodlePacket instruction = new DoodlePacket();
            //TODO: This should work for our purposes, but maybe set the IP address in the initialisation step
            instruction.instruction((short)1, profile._xVel.get(i), profile._yVel.get(i), packet.getAddress(), port);
            _socket.send(instruction._packet);
            i++;
        }
        
        
    }

    
}