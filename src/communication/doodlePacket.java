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
public class doodlePacket {

    DatagramPacket _packet;
    
    doodlePacket(byte[] data, InetAddress address, int port) {
        _packet = new DatagramPacket(data, data.length, address, port);
    }
    
    
    public byte[] intToBytes(int my_int) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeInt(my_int);
        out.close();
        byte[] int_bytes = bos.toByteArray();
        bos.close();
        return int_bytes;
    }
    
    public byte[] headerConstructor(int maxAcc, int nCommands, int deltaT){
        byte[] data = new byte[8];
        data[0] = (byte) 'Y';
        data[1] = (byte) 'O';
        data[2] = (byte)((maxAcc >> 8) & 0xFF);
        data[3] = (byte)(maxAcc & 0xFF);
        data[4] = (byte)((nCommands >> 8) & 0xFF);
        data[5] = (byte)(nCommands & 0xFF);
        data[6] = (byte)((deltaT >> 8) & 0xFF);
        data[7] = (byte)(deltaT & 0xFF);
        return data;
    }
    
    public byte[] instructionConstructor(int zBool, int xPosVel, int yPosVel)
    {
        //If Z is 1 - X and Y are velocities. if it is 0, X and Y are positions
        byte[] data = new byte[5];
        data[0] = (byte)(zBool & 0x01);
        data[1] = (byte)((xPosVel >> 8) & 0xFF);
        data[2] = (byte)(xPosVel & 0xFF);
        data[3] = (byte)((yPosVel >> 8) & 0xFF);
        data[4] = (byte)(yPosVel & 0xFF);
        return data;
    }
}


