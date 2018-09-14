package MPC;

import java.io.*;
import java.util.ArrayList;

/**
 * Automata
 *
 * @author CedricXing
 * Created on 2018/9/14
 * Copyright (c) 2018/9/14. CedricXing All rights Reserved.
 */

public class Automata {
    private ArrayList<Location> locations;
    private ArrayList<Transition> transitions;

    public Automata(String filename){
        File modelFile = new File(filename);
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(modelFile));
            String tempLine = null;
            while((tempLine = reader.readLine()) != null){
                if(tempLine.indexOf("<location") != -1){ // location definition

                }
            }
        }
        catch (FileNotFoundException e){

        }
        catch (IOException e){

        }
    }
}
