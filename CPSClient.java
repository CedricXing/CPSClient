/**
 * CPSClient
 *
 * @author CedricXing
 * Created on 2017/12/4
 * Copyright (c) 2017/12/4. CedricXing All rights Reserved.
 */


import java.net.*;

public class CPSClient {
    /**
     * modelFilePath
     */
    private String modelFilePath = "/Users/cedricxing/Desktop/GraduationProject/model.xml";

    private String cfgFilePath = "/Users/cedricxing/Desktop/GraduationProject/cfg.txt";

    /**
     * Socket related
     * USE UDP Protocol
     */
    private InetSocketAddress socketAddress = null;

    private DatagramSocket datagramSocket = null;

    private String sourceIP;

    private byte[] buffer = new byte[1024];

    /**
     *
     * @param hostIP local host IP address
     * @param port local host port
     * @throws Exception thrown when fail to create DatagramSocket
     */
    public CPSClient(String hostIP,int port){
        socketAddress = new InetSocketAddress(hostIP,port);
        try {
            datagramSocket = new DatagramSocket(socketAddress);
        }
        catch (SocketException exception){
            System.out.println(exception.getMessage());
        }
    }

    /**
     * Get Source IP Address
     * @return
     */
    public final String getSourceIP(){
        return sourceIP;
    }

    public void generateModelFile(){

    }

    public void generateCFGFile(){

    }

    public static void main(String []args) throws Exception{
        String localHost = "127.0.0.1";
        int localPort = 4455;
        CPSClient cpsClient = new CPSClient(localHost,localPort);

        //Todo:JSch Connection

        //Todo:Parameter parsing

    }
}
