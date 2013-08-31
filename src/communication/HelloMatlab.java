/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import matlabcontrol.*;
import matlabcontrol.extensions.*;
/**
 *
 * @author anthony
 */
public class HelloMatlab {
    public static void main(String[] args) throws IOException, MatlabConnectionException, MatlabInvocationException
{
    
    MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
                                        .setMatlabLocation("/home/anthony/.MATLAB/bin/matlab")
                                        //.setHidden(true)
                                        .build();
    //Create a proxy, which we will use to control MATLAB
    MatlabProxyFactory factory = new MatlabProxyFactory(options);
    MatlabProxy proxy = factory.getProxy();

    //Create and print a NURBS representation
    
    //double[] cp = new double[] {

        
    //Send the array to MATLAB, transpose it, then retrieve it and convert it to a 2D double array
    MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
    processor.setNumericArray("array", new MatlabNumericArray(array, null));
    proxy.eval("array = transpose(array);");
    double[][] transposedArray = processor.getNumericArray("array").getRealArray2D();
        
     //Print the returned array, now transposed
     System.out.println("Transposed: ");
     for(int i = 0; i < transposedArray.length; i++)
     {
         System.out.println(Arrays.toString(transposedArray[i]));
     }

    //Disconnect the proxy from MATLAB
    proxy.disconnect();
}
    
    
        public static byte[] intToBytes(int my_int) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeInt(my_int);
        out.close();
        byte[] int_bytes = bos.toByteArray();
        bos.close();
    return int_bytes;
}
   
}
