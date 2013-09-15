/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 *
 * @author anthony
 */
public class DoodlePacket {

    DatagramPacket _packet;
    
    DoodlePacket() {
        
    }
            
    
    DoodlePacket(byte[] data, InetAddress address, int port) {
        _packet = new DatagramPacket(data, data.length, address, port);
    }
    
    public void header(short maxAccX, short maxAccY, short maxVelX, short maxVelY, short nCommands, short deltaT, InetAddress address, int port) {
        byte[] headerData = headerConstructor(maxAccX,maxAccY, maxVelX, maxVelY, nCommands, deltaT);
        _packet = new DatagramPacket(headerData, headerData.length, address, port);
    }
    
    public void instruction(short commandNumber, short zBool, short xPosVel, short yPosVel, InetAddress address, int port) {
        byte[] instructionData = instructionConstructor(commandNumber, zBool, xPosVel, yPosVel);
        _packet = new DatagramPacket(instructionData, instructionData.length, address, port);
    }
    
    public byte[] intToBytes(int my_int) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeInt(my_int);
        out.close();
        byte[] int_bytes = bos.toByteArray();
        bos.close();
        return  int_bytes;
    }
    
    public byte[] headerConstructor(short maxAccX, short maxAccY, short maxSpeedX, short maxSpeedY, short nCommands, short deltaT){
        byte[] data = new byte[14];
        data[0] = (byte) 'Y';
        data[1] = (byte) 'O';
        data[3] = (byte)((maxAccX >> 8) & 0xFF);
        data[2] = (byte)(maxAccX & 0xFF);
        data[5] = (byte)((maxAccY >> 8) & 0xFF);
        data[4] = (byte)(maxAccY & 0xFF);
        data[7] = (byte)((maxSpeedX >> 8) & 0xFF);
        data[6] = (byte)(maxSpeedX & 0xFF);
        data[9] = (byte)((maxSpeedY >> 8) & 0xFF);
        data[8] = (byte)(maxSpeedY & 0xFF);
        data[11] = (byte)((nCommands >> 8) & 0xFF);
        data[10] = (byte)(nCommands & 0xFF);
        data[13] = (byte)((deltaT >> 8) & 0xFF);
        data[12] = (byte)(deltaT & 0xFF);
        return data;
    }
    
    public byte[] instructionConstructor(short commandNumber, short zBool, short xPosVel, short yPosVel)
    {
        //If Z is 1 - X and Y are velocities. if it is 0, X and Y are positions
        byte[] data = new byte[7];
        data[1] = (byte)((commandNumber >> 8) & 0xFF);
        data[0] = (byte)(commandNumber & 0xFF);
        data[2] = (byte)(zBool & 0x01);
        data[4] = (byte)((xPosVel >> 8) & 0xFF);
        data[3] = (byte)(xPosVel & 0xFF);
        data[6] = (byte)((yPosVel >> 8) & 0xFF);
        data[5] = (byte)(yPosVel & 0xFF);
         for (byte b : data)
    {
        // Add 0x100 then skip char(0) to left-pad bits with zeros
        System.out.println(Integer.toBinaryString(0x100 + b).substring(1));
    }
        return data;
    }
}


