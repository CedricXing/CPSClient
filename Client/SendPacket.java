package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Client.SendPacket
 *
 * @author CedricXing
 * Created on 29/03/2018
 * Copyright (c) 29/03/2018. CedricXing All rights Reserved.
 */

public class SendPacket {
    private DatagramSocket datagramSocket = null;

    public SendPacket() throws Exception{
        datagramSocket = new DatagramSocket();
    }

    public final DatagramPacket send(final String host,final int port,final byte[] bytes) throws Exception{
        DatagramPacket datagramPacket = new DatagramPacket(bytes,bytes.length, InetAddress.getByName(host),port);
        datagramSocket.send(datagramPacket);
        return datagramPacket;
    }

    public static void main(String []args) throws Exception{
        String mess = new String("010304453242");
        SendPacket sendPacket = new SendPacket();
        sendPacket.send("127.0.0.1",4455,mess.getBytes());
    }
}
