package communication;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import dataTypes.NurbsCurve;


public class ServerThread extends Thread {
	//Data Variables
	public ArrayList<NurbsCurve> data;
	
	//Server Variables
	//ServerSocket DataServer;
	protected DatagramSocket socket;
	protected BufferedReader in = null;
	//public DataInputStream is = null;
	
	
	public ServerThread() throws IOException {
		this("ServerThread",6666);
    }
    public ServerThread(String name, int portNumber) throws IOException {
        super(name);
        socket = new DatagramSocket(portNumber);
		  try {
		       // DataServer = new ServerSocket(portNumber);
			   // clientSocket = DataServer.accept();
			  	serverSocket = new Socket("hostname", 25);
			  	os = new DataOutputStream(serverSocket.getOutputStream());
			  	is = new DataInputStream(serverSocket.getInputStream());
		  		} catch (UnknownHostException e) {
		        	System.err.println("Don't know about host: hostname");
		        }catch (IOException e) {
		        	System.err.println("Couldn't get I/O for the connection to: hostname");
		     	}
	}
	
	public static void main(String [ ] args)
	{
		System.out.println("HELLO WORLD!");
		
		Server newServer = new Server(8000);
		
		// If everything has been initialized then we want to write some data
		// to the socket we have opened a connection to on port 25
		    if (newServer != null && newServer.os != null && newServer.is != null) {
		            try {
		// The capital string before each colon has a special meaning to SMTP
		// you may want to read the SMTP specification, RFC1822/3
		        newServer.os.writeBytes("HELO\n");    
		        newServer.os.writeBytes("MAIL From: k3is@fundy.csd.unbsj.ca\n");
		        newServer.os.writeBytes("RCPT To: k3is@fundy.csd.unbsj.ca\n");
		        newServer.os.writeBytes("DATA\n");
		        newServer.os.writeBytes("From: k3is@fundy.csd.unbsj.ca\n");
		        newServer.os.writeBytes("Subject: testing\n");
		        newServer.os.writeBytes("Hi there\n"); // message body
		        newServer.os.writeBytes("\n.\n");
		        newServer.os.writeBytes("QUIT");
		// keep on reading from/to the socket till we receive the "Ok" from SMTP,
		// once we received that then we want to break.
		                String responseLine;
		                while ((responseLine = is.readLine()) != null) {
		                    System.out.println("Server: " + responseLine);
		                    if (responseLine.indexOf("Ok") != -1) {
		                      break;
		                    }
		                }
	    // clean up:
	    // close the output stream
	    // close the input stream
	    // close the socket
	             os.close();
	                     is.close();
	                     smtpSocket.close();   
	                 } catch (UnknownHostException e) {
	                     System.err.println("Trying to connect to unknown host: " + e);
	                 } catch (IOException e) {
	                     System.err.println("IOException:  " + e);
	                 }
	             }
	         }   
	}

}
