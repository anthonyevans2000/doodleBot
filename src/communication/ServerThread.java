package communication;
import java.io.*;
import java.net.*;



public class ServerThread extends Thread {
	
	//Server Variables
	//ServerSocket DataServer;
	protected DatagramSocket socket;
	protected BufferedReader in = null;
	//public DataInputStream is = null;
	private boolean terminateRequested = false;
	
	
	public ServerThread() throws IOException {
		this("ServerThread",6666);
    }
	
    public ServerThread(String name, int portNumber) throws IOException {
        super(name);
        socket = new DatagramSocket(portNumber);
        //Should run a check on the integrity of the data to be sent here
	}
    
    public void run() {

        while (!terminateRequested) {
            try {
                byte[] buf = new byte[256];

                int _PORT = 6666;
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                
                //Print received data
                //TODO: implement logic for switching returned message based on received data
                String decoded = new String(packet.getData(), "UTF-8");
                System.out.println(decoded);
                
                byte[] data = headerConstructor(1000,11,2);
 
		// send the response to the client at "address" and "port"
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(data, data.length, address, _PORT);
                socket.send(packet);
                
                int[][] instructionArray = 
                        {
                            {0,     100,     100       },
                            {1,     0,      -1000   },
                            {1,     1000,   0       },
                            {0,     1200,   0       },
                            {1,     0,      -1000   },
                            {1,     1000,   0       },
                            {1,     0,      1000    },
                            {1,     -1000,  0       },
                            {0,     2400,   0       },
                            {1,     0,      -1000   },
                            {1,     1000,   0       }
                        };
                
                int i = 0;
                while(i<11){
        
                    socket.receive(packet);
                    
                    data = instructionConstructor(instructionArray[i][0],instructionArray[i][1],instructionArray[i][2]);
                    address = packet.getAddress();
                    port = packet.getPort();
                    packet = new DatagramPacket(data, data.length, address, _PORT);
                    socket.send(packet);
                    i++;
                }
                
                
                
                
            } catch (IOException e) {
                e.printStackTrace();
                terminateRequested = true;
            }
        }
        socket.close();
    }
    
    

    
}