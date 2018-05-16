/**
 * CPSClient
 *
 * @author CedricXing
 * Created on 2017/12/4
 * Copyright (c) 2017/12/4. CedricXing All rights Reserved.
 */

import com.jcraft.jsch.*;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CPSClient {
    /**
     * Socket related
     * USE UDP Protocol
     */
    private InetSocketAddress socketAddress = null;

    private DatagramSocket datagramSocket = null;

    private DatagramPacket datagramPacket = null;

    private InetAddress sourceInetAddress;

    private byte[] buffer = new byte[1024];

    /**
     * 小车参数信息
     */
    private int carNum = 2;
    private int[] loc = new int[carNum];
    private String[] velocity = new String[carNum];
    public int cycle;
    /**
     * rfid Number
     */
    //private Map<Long,Integer> rfidInfo = null;
    /**
     *
     * @param hostIP local host IP address
     * @param port local host port
     * @throws Exception thrown when fail to create DatagramSocket
     */
    public CPSClient(String hostIP,int port){
        cycle = 0;
        socketAddress = new InetSocketAddress(hostIP,port);
        try {
            datagramSocket = new DatagramSocket(socketAddress);
        }
        catch (SocketException exception){
            System.out.println(exception.getMessage());
        }
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/Users/cedricxing/Documents/大四/毕业设计/Client/src/loc.txt"))));
//            rfidInfo = new HashMap<Long,Integer>();
//            String line = "";
//            line = bufferedReader.readLine();
//            int no = 0;
//            while (line != null){
//                Long t = Long.parseLong(line);
//                //System.out.println(t);
//                rfidInfo.put(t,no);
//                ++no;
//                line = bufferedReader.readLine();
//            }
//            bufferedReader.close();
//        }
//        catch (FileNotFoundException e){
//            System.err.println("File not found!");
//        }
//        catch (IOException e){
//            System.err.println("IO exception!");
//        }
    }


    /**
     * Generate Model File to modelFilePath
     */
    public void generateModelFile(String path,String ma){
        try {
            BufferedWriter outXml = new BufferedWriter(new FileWriter(new File(path)));
            outXml.write("<?xml version=" + '"' + "1.0" + '"' + " encoding=" + '"' + "iso-8859-1" + '"' + "?>" + "\n");
            outXml.write("<sspaceex xmlns=" + '"' + "http://www-verimag.imag.fr/xml-namespaces/sspaceex" + '"'
                    + " version=" + '"' + "0.2" + '"' + " math=" + '"' + "SpaceEx" + '"' + ">" + "\n");
            outXml.write("<component id=" + '"' + "system" + '"' + ">" + "\n");
            outXml.write("<param name=" + '"' + "x" + '"' + " type=" + '"' + "real" + '"' + " local=" + '"' + "false"
                    + '"' + " d1=" + '"' + "1" + '"' + " d2=" + '"' + "1" + '"' + " dynamics=" + '"' + "any" + '"'
                    + "/>" + "\n");
            outXml.write("<param name=" + '"' + "v" + '"' + " type=" + '"' + "real" + '"' + " local=" + '"' + "false"
                    + '"' + " d1=" + '"' + "1" + '"' + " d2=" + '"' + "1" + '"' + " dynamics=" + '"' + "any" + '"'
                    + "/>" + "\n");
            outXml.write("<param name=" + '"' + "t" + '"' + " type=" + '"' + "real" + '"' + " local=" + '"' + "false"
                    + '"' + " d1=" + '"' + "1" + '"' + " d2=" + '"' + "1" + '"' + " dynamics=" + '"' + "any" + '"'
                    + "/>" + "\n");
            outXml.write("<param name=" + '"' + "vebi" + '"' + " type=" + '"' + "real" + '"' + " local=" + '"' + "false"
                    + '"' + " d1=" + '"' + "1" + '"' + " d2=" + '"' + "1" + '"' + " dynamics=" + '"' + "any" + '"'
                    + "/>" + "\n");
            outXml.write("<param name=" + '"' + "a" + '"' + " type=" + '"' + "real" + '"' + " local=" + '"' + "false"
                    + '"' + " d1=" + '"' + "1" + '"' + " d2=" + '"' + "1" + '"' + " dynamics=" + '"' + "any" + '"'
                    + "/>" + "\n");

            //label
            for(int i = 1;i <= 7;++i){
                outXml.write("<param name=" + '"' + "e" + i + '"' + " type=" + '"' + "label" + '"' + " local=" + '"' + "false"
                        + '"' + "/>" + "\n");
            }

            //State Init
            outXml.write("	<location id=" + '"' + "1" + '"' + " name=" + '"' + "v1" + '"' + " x=" + '"' + "710" + '"'
                    + " y=" + '"' + "351" + '"' + " width=" + '"' + "135.0" + '"' + " height=" + '"' + "73.0" + '"'
                    + ">" + "\n");
            outXml.write("	  <invariant>"  + "t == 0 " + " </invariant>" + "\n");
            outXml.write("      <flow>x'==v &amp; t'==1 &amp; v'==0 &amp; vebi'==0 &amp; a'==0 </flow>" + "\n");
            outXml.write("    </location>" + "\n");

            //State AC
            outXml.write("	<location id=" + '"' + "2" + '"' + " name=" + '"' + "v2" + '"' + " x=" + '"' + "710" + '"'
                    + " y=" + '"' + "351" + '"' + " width=" + '"' + "135.0" + '"' + " height=" + '"' + "73.0" + '"'
                    + ">" + "\n");
            outXml.write("	  <invariant> vebi-20&gt;=v&gt;=0  " + "</invariant>" + "\n");
            //outXml.write("      <flow>x'==v &amp; v' == a &amp; t'==1 &amp;vebi'== -10 * v / (20 * " + ma + ") ^ 0.5 - 100 * v * x / (20 * " + ma + ") ^ 1.5 - 1500 * v * x * x / (20 * " + ma + ") ^ 2.5 &amp;a' == 0</flow>" + "\n");
            outXml.write("      <flow>x'==v &amp; v' == a &amp; t'==1 &amp;vebi'== -10 * 2 * v / (20 * " + ma + ") ^ 0.5 - 100 * 4 * v * x / (20 * " + ma + ") ^ 1.5 - 1500 * 8 * v * x * x / (20 * " + ma + ") ^ 2.5 &amp;a' == 0</flow>" + "\n");
            //outXml.write("      <flow>x'==v &amp; v' == a &amp; t'==1 &amp;vebi'== -10 * v / (20 * (" + ma + " - x)) ^ 0.5 &amp;a' == 0</flow>" + "\n");
            outXml.write("    </location>" + "\n");

            //State CC
            outXml.write("	<location id=" + '"' + "3" + '"' + " name=" + '"' + "v3" + '"' + " x=" + '"' + "710" + '"'
                    + " y=" + '"' + "351" + '"' + " width=" + '"' + "135.0" + '"' + " height=" + '"' + "73.0" + '"'
                    + ">" + "\n");
            outXml.write("	  <invariant> vebi&gt;=v&gt;=vebi-20  " + "</invariant>" + "\n");
            //outXml.write("      <flow>x'==v &amp; v' == a &amp; t'==1 &amp;vebi'== -10 * v / (20 * " + ma + ") ^ 0.5 - 100 * v * x / (20 * " + ma + ") ^ 1.5 - 1500 * v * x * x / (20 * " + ma + ") ^ 2.5 &amp;a' == 0</flow>" + "\n");
            outXml.write("      <flow>x'==v &amp; v' == a &amp; t'==1 &amp;vebi'== -10 * 2 * v / (20 * " + ma + ") ^ 0.5 - 100 * 4 * v * x / (20 * " + ma + ") ^ 1.5 - 1500 * 8 * v * x * x / (20 * " + ma + ") ^ 2.5 &amp;a' == 0</flow>" + "\n");
            //outXml.write("      <flow>x'==v &amp; v' == a &amp; t'==1 &amp;vebi'== -10 * v / (20 * (" + ma + " - x)) ^ 0.5 &amp;a' == 0</flow>" + "\n");
            outXml.write("    </location>" + "\n");

            //State EB
            outXml.write("	<location id=" + '"' + "4" + '"' + " name=" + '"' + "v4" + '"' + " x=" + '"' + "710" + '"'
                    + " y=" + '"' + "351" + '"' + " width=" + '"' + "135.0" + '"' + " height=" + '"' + "73.0" + '"'
                    + ">" + "\n");
            outXml.write("	  <invariant> v&gt;=vebi  " + "</invariant>" + "\n");
            //outXml.write("      <flow>x'==v &amp; v' == a &amp; t'==1 &amp;vebi'== -10 * v / (20 * " + ma + ") ^ 0.5 - 100 * v * x / (20 * " + ma + ") ^ 1.5 - 1500 * v * x * x / (20 * " + ma + ") ^ 2.5 &amp;a' == 0</flow>" + "\n");
            outXml.write("      <flow>x'==v &amp; v' == a &amp; t'==1 &amp;vebi'== -10 * 2 * v / (20 * " + ma + ") ^ 0.5 - 100 * 4 * v * x / (20 * " + ma + ") ^ 1.5 - 1500 * 8 * v * x * x / (20 * " + ma + ") ^ 2.5 &amp;a' == 0</flow>" + "\n");
            //outXml.write("      <flow>x'==v &amp; v' == a &amp; t'==1 &amp;vebi'== -10 * v / (20 * (" + ma + " - x)) ^ 0.5 &amp;a' == 0</flow>" + "\n");
            outXml.write("    </location>" + "\n");

            //Transition Init->AC
            outXml.write("    <transition source=" + '"' + "1" + '"' + " target=" + '"' + "2" + '"' + ">" + "\n");
            outXml.write("      <label>e1</label>" + "\n");
            outXml.write("	  <guard>0&lt;=v&lt;vebi - 20" + "</guard>" + "\n");
            outXml.write("	  <assignment>v'=v &amp; x'=x &amp; t'=t  &amp;vebi'=vebi &amp;a' = 10</assignment>" + "\n");
            outXml.write("    </transition>" + "\n");

            //Transition Init->CC
            outXml.write("    <transition source=" + '"' + "1" + '"' + " target=" + '"' + "3" + '"' + ">" + "\n");
            outXml.write("      <label>e2</label>" + "\n");
            outXml.write("	  <guard>vebi-20&lt;=v&lt;vebi" + "</guard>" + "\n");
            outXml.write("	  <assignment>v'=v &amp; x'=x &amp; t'=t  &amp;vebi'=vebi &amp;a' = [0,10]</assignment>" + "\n");
            outXml.write("    </transition>" + "\n");

            //Transition Init->EB
            outXml.write("    <transition source=" + '"' + "1" + '"' + " target=" + '"' + "4" + '"' + ">" + "\n");
            outXml.write("      <label>e3</label>" + "\n");
            outXml.write("	  <guard>vebi&lt;=v" + "</guard>" + "\n");
            outXml.write("	  <assignment>v'=v &amp; x'=x &amp; t'=t  &amp;vebi'=vebi &amp;a' = -15</assignment>" + "\n");
            outXml.write("    </transition>" + "\n");

            //Transition AC->CC
            outXml.write("    <transition source=" + '"' + "2" + '"' + " target=" + '"' + "3" + '"' + ">" + "\n");
            outXml.write("      <label>e4</label>" + "\n");
            outXml.write("	  <guard>vebi-20&lt;=v&lt;=vebi" + "</guard>" + "\n");
            outXml.write("	  <assignment>v'=v &amp; x'=x &amp; t'=t  &amp;vebi'=vebi &amp;a' = [0,10]</assignment>" + "\n");
            outXml.write("    </transition>" + "\n");

            //Transition CC->AC
            outXml.write("    <transition source=" + '"' + "3" + '"' + " target=" + '"' + "2" + '"' + ">" + "\n");
            outXml.write("      <label>e5</label>" + "\n");
            outXml.write("	  <guard>0&lt;=v&lt;=vebi - 20" + "</guard>" + "\n");
            outXml.write("	  <assignment>v'=v &amp; x'=x &amp; t'=t  &amp;vebi'=vebi &amp;a' = 10</assignment>" + "\n");
            outXml.write("    </transition>" + "\n");

            //Transition CC->EB
            outXml.write("    <transition source=" + '"' + "3" + '"' + " target=" + '"' + "4" + '"' + ">" + "\n");
            outXml.write("      <label>e6</label>" + "\n");
            outXml.write("	  <guard>vebi&lt;=v" + "</guard>" + "\n");
            outXml.write("	  <assignment>v'=v &amp; x'=x &amp; t'=t  &amp;vebi'=vebi &amp;a' = -15 </assignment>" + "\n");
            outXml.write("    </transition>" + "\n");

            //Transition EB->CC
            outXml.write("    <transition source=" + '"' + "4" + '"' + " target=" + '"' + "3" + '"' + ">" + "\n");
            outXml.write("      <label>e7</label>" + "\n");
            outXml.write("	  <guard>vebi-20&lt;=v&lt;=vebi" + "</guard>" + "\n");
            outXml.write("	  <assignment>v'=v &amp; x'=x &amp; t'=t  &amp;vebi'=vebi &amp;a' = [0,10]</assignment>" + "\n");
            outXml.write("    </transition>" + "\n");

            outXml.write("  </component>" + "\n");
            outXml.write("</sspaceex>" + "\n");
            outXml.close();
        }
        catch (Exception e){
            System.err.println("Error occured when the xmlfile was created!");
        }
    }

    /**
     * Generate CFG File to cfgFilePath
     */
    public void generateCFGFile(String v,String vebi,String ma,String path){
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path)));
            bufferedWriter.write("# analysis options" + "\n");
            bufferedWriter.write("system = " + '"' + "system" + '"' + "" + "\n");
            //To be modified
            bufferedWriter.write("initially = " + '"' + "loc()==v1 & x==" + 0 + "& v==" + v + " & vebi==" + vebi + " & t==0 &a==0" + '"' + "" + "\n");
            bufferedWriter.write("forbidden=" + '"' + "x>" + ma + "& t<5 & loc() == v4" + '"' + "" + "\n");
            bufferedWriter.write("scenario = " + '"' + "phaver" + '"' + "" + "\n");
            bufferedWriter.write("directions = " + '"' + "uni32" + '"' + "" + "\n");
            bufferedWriter.write("sampling-time = 0.1" + "\n");
            bufferedWriter.write("time-horizon = 40000" + "\n");
            bufferedWriter.write("output-variables = " + '"' + "x,v,t" + '"' + "" + "\n");
            bufferedWriter.write("output-format = " + '"' + "INTV" + '"' + "" + "\n");
            bufferedWriter.write("rel-err = 1.0e-12" + "\n");
            bufferedWriter.write("abs-err = 1.0e-13" + "\n");

            bufferedWriter.close();
        }
        catch (Exception e){
            System.err.println("Error occured when the cfgfile was created!");
        }
    }

    /**
     * Receive data
     * @return String of data
     * @throws IOException
     */
    public final String receive() throws IOException{
        datagramPacket = new DatagramPacket(buffer,buffer.length);
        datagramSocket.receive(datagramPacket);
        sourceInetAddress = datagramPacket.getAddress();
        String mes = new String(datagramPacket.getData(),0,datagramPacket.getLength());
        return mes;
    }

    /**
     * Send data
     * @param mes
     * @throws IOException
     */
    public final void response(String mes) throws IOException{
        DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,sourceInetAddress,4455);
        sendPacket.setData(mes.getBytes());
        datagramSocket.send(sendPacket);
    }

    public class VerificationTask extends Thread{
        private Transmission transmission;
        private Session session;
        private int carID;
        private int cycle;

        public VerificationTask(Transmission transmission,Session session,int carID,int cycle){
            this.transmission = transmission;
            this.session = session;
            this.carID = carID;
            this.cycle = cycle;
        }

        public void run(){
            int previousCarLoc = loc[(carID + 1) % carNum];
            int selfCarloc = loc[carID];
            int ma = (previousCarLoc >= selfCarloc) ? (previousCarLoc * 10 - selfCarloc * 10) : ((previousCarLoc + 120) * 10 - selfCarloc *10);
            if(ma > 200) ma = 200;
            double vebi = Math.sqrt(2 * 10 * ma);//compute vebi
            String modelFilePath = "/Users/cedricxing/Desktop/GraduationProject/model_" + this.cycle + "_" + Integer.toString(carID) + ".xml";
            String cfgFilePath = "/Users/cedricxing/Desktop/GraduationProject/cfg_" + this.cycle + "_" + Integer.toString(carID) + ".txt";
            generateModelFile(modelFilePath,Integer.toString(ma));
            generateCFGFile(velocity[carID],Double.toString(vebi),Integer.toString(ma),cfgFilePath);
            //1 for safe ,0 for unsafe
            System.out.println(carID + "号小车当前的位置为" + selfCarloc + " ,速度为" + velocity[carID] + " ,vebi为" + vebi + "  ,ma为" + ma);
            String result = transmission.verification(session,modelFilePath,cfgFilePath);
            System.out.println(carID + "号车的验证结果为" + result);
            String preCarLocString = Integer.toString(previousCarLoc);
            if(preCarLocString.length() == 1) preCarLocString = "00" + preCarLocString;
            else if(preCarLocString.length() == 2) preCarLocString = "0" + preCarLocString;
            String returnMessage = String.valueOf(carID) + result + preCarLocString;
            System.out.println(returnMessage);
            try {
                response(returnMessage);
            }
            catch (Exception e){
                System.err.println("Send verification result failed!");
            }
        }
    }

    /**
     * Main Function
     * @param args
     * @throws Exception
     */
    public static void main(String []args) throws Exception{
        String localHost = "192.168.1.102";
        int localPort = 4455;
        CPSClient cpsClient = new CPSClient(localHost,localPort);

        String modelFilePath = "/Users/cedricxing/Desktop/GraduationProject/model.xml";
        String cfgFilePath = "/Users/cedricxing/Desktop/GraduationProject/cfg.txt";
        int x = 100;
        double vebi = Math.sqrt(2 * 20 * x);
        cpsClient.generateModelFile(modelFilePath,Integer.toString(x));
        cpsClient.generateCFGFile(Integer.toString(30),Double.toString(vebi),Integer.toString(x),cfgFilePath);



        //Todo:JSch Connection
        Transmission transmission = new Transmission();
        Session session = transmission.connect();

        System.out.println("参数信息：");
        System.out.println("*******移动授权距离为100");
        System.out.println("*******初始速度为30");
        System.out.println("*******初始顶棚速度为" + vebi);
        transmission.verification(session,modelFilePath,cfgFilePath);

//        int i = 0;
//        while(i < 10){
//            String result = transmission.verification(session,modelFilePath,cfgFilePath);
//            ++ i;
//            System.out.println(result);
//        }

//        while(true){
//            String message = cpsClient.receive();
//            System.out.println(message);
//            cpsClient.response("01123");
//        }

        while(true){
            Set<Integer> set = new HashSet<Integer>();
            for(int i = 0;i < cpsClient.carNum;++i){
                set.add(i);
            }
            while(!set.isEmpty()){
                String message = cpsClient.receive();
                if(message.equals("11111") || message.equals("00000"))
                    continue;
                System.out.println("接收到的数据包为： " + message);
                char carID = message.charAt(0);
//                String returnText = "10123";
//                cpsClient.response(returnText);
                if(set.contains(carID - '0')){
                    String v = "",rfid = "";
                    for(int i = 1;i <= 5;++i) {
                        //System.out.println(message.charAt(i));
                        v += String.valueOf(message.charAt(i));
                    }
                    if(v.charAt(0) == '-')
                        v = "1";
                    for(int i = 6;i <= 15;++i){
                        rfid += String.valueOf(message.charAt(i));
                    }
                    cpsClient.velocity[carID - '0'] = v;
//                    Long rfidTemp = Long.parseLong(rfid);
//                    if(rfidTemp == 0)
//                        continue;
                    cpsClient.loc[carID - '0'] = Integer.parseInt(rfid);
                    //System.out.println("loc:" + cpsClient.loc[carID - '0']);
                    //System.out.println("NO:" + carID + "   ,v:" + v + "loc :" + cpsClient.rfidInfo.get(cpsClient.loc[carID - '0']));
                    //System.out.println("get here");
                    //cpsClient.new VerificationTask(transmission,session,carID - '0').start();
                    set.remove(carID-'0');
                    if(!set.isEmpty()){
                        String ackMessage = "11111";
                        //System.out.println("Send Ack Message!");
                        cpsClient.response(ackMessage);
                    }
                }
                else{//Packet Loss or other problems
                    System.err.println("Waiting for other car!");
                }
            }
            String ackMessage = "00000";
            cpsClient.response(ackMessage);
            System.out.println("Start Verification!");
            System.out.println("******************************周期" + cpsClient.cycle + "******************************************");
//            for(int i = 0;i < cpsClient.carNum;++i) {
//                cpsClient.new VerificationTask(transmission, session,i,cpsClient.cycle).start();
//            }
            ExecutorService threadPool = Executors.newFixedThreadPool(1);
            for(int i = 0;i < cpsClient.carNum;++i) {
                threadPool.execute(cpsClient.new VerificationTask(transmission, session, i, cpsClient.cycle));
            }
            ++cpsClient.cycle;
        }

    }


}
