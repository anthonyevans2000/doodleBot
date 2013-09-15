package communication;
import dataTypes.CurveVelocity;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import main.Main;


/**
 * A server that feeds velocity commands to a robot upon a request for them.
 * The server will ignore incoming communications until there is a command to be sent,
 * indicated by the ArrayList '_profiles' containing a value.
 * It will then accept a header request and return the header for the command.
 * It will then proceed to accept instruction requests and serve the corresponding instructions.
 * If a request for a -1 instruction is received, the server will remove itself from the serving
 * loop, delete the instruction it was sending from '_profiles' and begin awaiting header requests again.
 * @author anthony
 */
public class ServerThread implements Runnable {
	
    public DatagramSocket _socket;
    protected BufferedReader in = null;
    public ArrayList<CurveVelocity> _profiles = new ArrayList<CurveVelocity>();


    public ServerThread() throws IOException {
            this("ServerThread",6666);
    }
	
    public ServerThread(String name, int portNumber) throws IOException {
        //super(name);
        _socket = new DatagramSocket(portNumber);
	}
    
    @Override
    public void run() {
        
        byte[] buf = new byte[Main.packetBufferLength];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        
        char command;
        byte[] receivedData;
        short requestedCommand;
        byte currentCurve = Byte.MAX_VALUE;
        boolean firstHeader = true;

        while (!Main.readyToQuit) {       
            try {
                if(Main.commsReset) {
                    System.out.println("Resetting Server queue");
                    _profiles.clear();
                    firstHeader = true;
                    Main.commsReset = false;
                }
                
                //Wait for a packet
                _socket.setSoTimeout(Main.socketTimeout);
                _socket.receive(packet);
                String rcvd = "rcvd from " + packet.getAddress() + ", " + packet.getPort() + ": "+ new String(packet.getData(), 0, packet.getLength());
                System.out.println(rcvd);
                //printData(packet.getData());
                
                //If _profiles is empty, continue waiting for headers
                if(_profiles.size() < 1) {
                    continue;
                }else {
                    //Analyse request
                    receivedData = packet.getData();
                    command = getCommand(receivedData);
                    requestedCommand = getShort(receivedData);
                    System.out.println("Command: " + command + " Req instruction: " + requestedCommand);
                    
                    if(command == 'H') {
                        if(firstHeader) {
                            currentCurve = receivedData[1];
                            firstHeader = false;
                        }
                        if(currentCurve != receivedData[1]){
                            _profiles.remove(0);
                            currentCurve = receivedData[1];
                            if(_profiles.isEmpty()) continue;
                        }
                        //Send header   
                        DoodlePacket header = new DoodlePacket();
                        header.header(Main.maxAccelX, Main.maxAccelY, Main.maxVelX, Main.maxVelY, (short) _profiles.get(0)._xVel.size(), Main.timeStep, packet.getAddress(), Main.plcPortNumber);
                        _socket.send(header._packet);
                    }
                    if(command == 'A') {

                        if(requestedCommand == 0) {
                            //Send move instruction
                            System.out.println(requestedCommand + ", x: " + _profiles.get(0)._xInitial + ", y: " + _profiles.get(0)._yInitial);
                            DoodlePacket moveInstruction = new DoodlePacket();
                            moveInstruction.instruction(requestedCommand, (short)0, _profiles.get(0)._xInitial, _profiles.get(0)._yInitial,  packet.getAddress(), Main.plcPortNumber);
                            _socket.send(moveInstruction._packet);
                        }
                        else if(requestedCommand > 0 && requestedCommand < _profiles.get(0)._xVel.size()) {
                            System.out.println(requestedCommand + ", x: " + _profiles.get(0)._xVel.get(requestedCommand) + ", y: " + _profiles.get(0)._yVel.get(requestedCommand) );
                            DoodlePacket instruction = new DoodlePacket();
                            instruction.instruction(requestedCommand, (short)1, _profiles.get(0)._xVel.get(requestedCommand), _profiles.get(0)._yVel.get(requestedCommand), packet.getAddress(), Main.plcPortNumber);
                            _socket.send(instruction._packet);
                        }
                    }
                }
            }
            catch (SocketTimeoutException e) {
                // timeout exception. 
                continue; 
            } catch (IOException e) {
            
            }
        
        }
        //Quitting
        _socket.close();
    }
    
    
    short getShort(byte[] received) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(received[1]);
        bb.put(received[2]);
        short shortVal = bb.getShort(0);
        return shortVal;
    }
    
    char getCommand(byte[] received) {
        String rec = new String(received, 0, received.length);
        return rec.charAt(0);
    }
    
    void printData (byte[] data) {
        
        System.out.println("Received data");
        for (byte b : data)
        {
            // Add 0x100 then skip char(0) to left-pad bits with zeros
            System.out.println(Integer.toBinaryString(0x100 + b).substring(1));
        }
        System.out.println("End Received Data");    
    }

    
}