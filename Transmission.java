/**
 * Transmission
 *
 * @author CedricXing
 * Created on 2017/12/4
 * Copyright (c) 2017/12/4. CedricXing All rights Reserved.
 */

import com.jcraft.jsch.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

public class Transmission {
    /**
     * File related
     */
    private String modelFile = "modelfile";
    private String cfgFile = "cfgfile";
    private String charset = "UTF-8";
    private String modelFilePath;
    private String cfgFilePath;

    /**
     * User and Password
     */
    private String userName = "cedricxing";
    private String password = "cedricxing";
    private String hostIP = "10.211.55.25";

    /**
     * Command related
     */
    private String bound = "2";
    private String calcommand = "./bach modelfile cfgfile " + bound;
    //private String clearCommandModel = "true >modelfile";
    //private String clearcommandCFG = "true >cfgfile";
    private String clearCommandModel = "\n";
    private String clearcommandCFG = "\n";

    /**
     * Constructor
     */
    public Transmission(){
    }

    /**
     * JSch Connection
     * @return
     * @throws JSchException
     */
    public Session connect() throws JSchException{
        JSch jSch = new JSch();
        Session session = jSch.getSession(userName,hostIP,22);
        session.setPassword(password);

        /*The following codes actually make the connection lose
            much of the the SSH/SFTP security.But it's safe enough if the server
            is a trusted local host.
         */
        Properties configuration = new Properties();
        configuration.put("StrictHostKeyChecking","no");
        session.setConfig(configuration);

        System.out.println("Connection begins.");
        session.connect();
        System.out.println("Connection succeeded.");
        return session;
    }

    /**
     * Transmit modelfile and cfgfile to server and return the result of the
     * verification
     * @param session
     * @return
     */
    public String verification(Session session,String modelFilePath,String cfgFilePath) {
        this.modelFilePath = modelFilePath;
        this.cfgFilePath = cfgFilePath;
        //1 for safe
        String result = (transmitfile(session) ? "1" : "0");
        return result;
    }

    /**
     * Transmission Code.
     * Reference:http://www.jcraft.com/jsch/examples/ScpTo.java
     * @param session
     * @return
     */
    private boolean transmitfile(Session session) {
        String modelfilePath = modelFilePath;
        String cfgfilePath = cfgFilePath;
        boolean ptimestamp = true;
        String commandModel = "scp " + (ptimestamp ? "-p" : "") + " -t " + modelFile + "\n";

        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(commandModel);
            OutputStream outputStream = channel.getOutputStream();
            InputStream inputStream = channel.getInputStream();
            channel.connect();

            //TODO:checkAck
            if (checkAck(inputStream) != 0) {
                System.out.println(checkAck(inputStream));
                System.exit(0);
            }

            File modelFile = new File(modelfilePath);
            if(ptimestamp) {
                commandModel = "T" + (modelFile.lastModified() / 1000) + " 0";
                commandModel += (" " + (modelFile.lastModified() / 1000) + " 0\n");
                outputStream.write(commandModel.getBytes());
                outputStream.flush();

                //TODO:checkACK
                if (checkAck(inputStream) != 0) {
                    System.exit(0);
                }
            }

            long filesize = modelFile.length();
            commandModel = "C0644 " + filesize + " ";
            if(modelfilePath.lastIndexOf('/') > 0){
                commandModel += modelfilePath.substring(modelfilePath.lastIndexOf('/') + 1);
            }
            else{
                commandModel += modelfilePath;
            }
            commandModel += "\n";
            outputStream.write(commandModel.getBytes());
            outputStream.flush();

            //TODO:checkAck
            if (checkAck(inputStream) != 0) {
                System.exit(0);
            }

            //System.out.println(modelfilePath);
            FileInputStream fileInputStream = new FileInputStream(modelfilePath);
            byte[] buffer = new byte[1024];
            while(true){
                int len = fileInputStream.read(buffer,0,buffer.length);
                if(len <= 0) break;
                outputStream.write(buffer,0,len);
            }
            fileInputStream.close();
            fileInputStream = null;

            //send '\0'
            buffer[0] = 0;
            outputStream.write(buffer,0,1);
            outputStream.flush();
            outputStream.close();
            channel.disconnect();

            String commandCFG = "scp " + (ptimestamp ? "-p" : "") + " -t " + cfgFile;
            commandCFG += "\n";
            commandCFG += calcommand;
            commandCFG += "\n";
            commandCFG += clearCommandModel;
            commandCFG += "\n";
            commandCFG += clearcommandCFG;

            Channel channelCFG = session.openChannel("exec");
            ((ChannelExec)channelCFG).setCommand(commandCFG);

            OutputStream outputStreamCFG = channelCFG.getOutputStream();
            InputStream inputStreamCFG = channelCFG.getInputStream();

            channelCFG.connect();

            //TODO:checkAck
            if (checkAck(inputStreamCFG) != 0) {
                System.exit(0);
            }

            File cfgFile = new File(cfgfilePath);

            if(ptimestamp){
                commandCFG = "T" + (cfgFile.lastModified() / 1000) + " 0";
                commandCFG += (" " + (cfgFile.lastModified() / 1000) + " 0\n");
                outputStreamCFG.write(commandCFG.getBytes());
                outputStreamCFG.flush();

                //TODO:checkAck
                if (checkAck(inputStreamCFG) != 0) {
                    System.exit(0);
                }
            }

            long filesizeCFG = cfgFile.length();
            commandCFG = "C0644 " + filesizeCFG + " ";
            if(cfgfilePath.lastIndexOf('/') > 0){
                commandCFG += cfgfilePath.substring(cfgfilePath.lastIndexOf('/') + 1);
            }
            else{
                commandCFG += cfgfilePath;
            }
            commandCFG += "\n";
            outputStreamCFG.write(commandCFG.getBytes());
            outputStreamCFG.flush();

            //TODO:checkAck
            if (checkAck(inputStreamCFG) != 0) {
                System.exit(0);
            }

            inputStreamCFG = new FileInputStream(cfgfilePath);
            byte[] bufferCFG = new byte[1024];
            while(true){
                int len = inputStreamCFG.read(bufferCFG,0,bufferCFG.length);
                if(len <= 0) break;
                outputStreamCFG.write(bufferCFG,0,len);
            }
            inputStreamCFG.close();
            inputStreamCFG = null;
            //send '\0'
            bufferCFG[0] = 0;
            outputStreamCFG.write(bufferCFG,0,1);
            outputStreamCFG.flush();
            outputStreamCFG.close();

            //Get result
            InputStream resultInputStrem = channelCFG.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resultInputStrem, Charset.forName(charset)));
            String result = "";
            String notReachable = "not reachable";
            boolean judge = false;
            System.out.println("验证过程信息:");
            while((result = bufferedReader.readLine()) != null){
                System.out.println(result);
                if(result.contains(notReachable)) judge = true;
            }
            if(judge){
                System.out.println("**************** 验证结果:安全！");
            }
            else{
                System.out.println("**************** 验证结果:危险!");
            }
            bufferedReader.close();
            channelCFG.disconnect();
            session.disconnect();
            return judge;
        }
        catch (Exception e){
            System.out.println(e);
            return false;
        }
    }

    /**
     * checkAck
     * @param in
     * @return
     * @throws IOException
     */
    static int checkAck(InputStream in) throws IOException{
        int b=in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if(b==0) return b;
        if(b==-1) return b;

        if(b==1 || b==2){
            StringBuffer sb=new StringBuffer();
            int c;
            do {
                c=in.read();
                sb.append((char)c);
            }
            while(c!='\n');
            if(b==1){ // error
                System.out.print(sb.toString());
            }
            if(b==2){ // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}
