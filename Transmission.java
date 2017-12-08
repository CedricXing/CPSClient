/**
 * Transmission
 *
 * @author CedricXing
 * Created on 2017/12/4
 * Copyright (c) 2017/12/4. CedricXing All rights Reserved.
 */

import com.jcraft.jsch.*;
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
    private String hostIP = "";

    /**
     * Command related
     */
    private String bound = "10";
    private String command = "./bach modelfile cfgfile" + bound;
    private String clearCommandModel = "true >modilefile";
    private String clearcommandCFG = "true >cfgfile";

    /**
     * Constructor
     * @param modelFilePath
     * @param cfgFilePath
     */
    public Transmission(String modelFilePath,String cfgFilePath){
        this.modelFilePath = modelFilePath;
        this.cfgFilePath = cfgFilePath;
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


}
