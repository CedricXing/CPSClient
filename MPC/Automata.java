package MPC;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
    private ArrayList<String> parameters;
    private int initLoc;
    private Map<String,Double> initParameterValues;
    private ArrayList<Constraint> forbiddenConstraints;

    public Automata(String modelFileName,String cfgFileName){
        processModelFile(modelFileName);
        processCFGFile(cfgFileName);
    }

    void processCFGFile(String cfgFileName){
        File cfgFile = new File(cfgFileName);
        BufferedReader reader = null;
        initParameterValues = new HashMap<>();
        forbiddenConstraints = new ArrayList<>();
        try{
            reader = new BufferedReader(new FileReader(cfgFile));
            String tempLine = null;
            while((tempLine = reader.readLine()) != null){
                if(tempLine.indexOf("initially") != -1){
                    String []strings = tempLine.split("\"");
                    setInitParameterValues(strings[1]);
                }
                if(tempLine.indexOf("forbidden") != -1){
                    String []strings = tempLine.split("\"");
                    setForbiddenValues(strings[1]);
                }
            }

        }
        catch (FileNotFoundException e){
            System.out.println("File not found" + '\n' + e.getMessage());
        }
        catch (IOException e){
            System.out.println("IO Exception" + '\n' + e.getMessage());
        }
        finally {
            if(reader != null){
                try{
                    reader.close();
                }
                catch (IOException e){
                    System.out.println("IO Exception" + '\n' + e.getMessage());
                }
            }
        }
    }
    void processModelFile(String modelFileName){
        File modelFile = new File(modelFileName);
        BufferedReader reader = null;
        locations = new ArrayList<>();
        transitions = new ArrayList<>();
        parameters = new ArrayList<>();
        try{
            reader = new BufferedReader(new FileReader(modelFile));
            String tempLine = null;
            while((tempLine = reader.readLine()) != null){
                if(tempLine.indexOf("<para") != -1){ // paramater definition
                    while(true){
                        String []strings = tempLine.split("\"");
                        if(strings[3].equals("real"))
                            parameters.add(strings[1]);
                        tempLine = reader.readLine();
                        if(tempLine.indexOf("<para") == -1) {
                            parameters.sort(new Comparator<String>() {
                                @Override
                                public int compare(String o1, String o2) {
                                    if(o1.length() < o2.length())
                                        return -1;
                                    else if(o1.length() > o2.length())
                                        return 1;
                                    else return 0;
                                }
                            });
                            break;
                        }
                    }
                }
                if(tempLine.indexOf("<location") != -1){ // location definition
                    String []strings = tempLine.split("\"");
                    //System.out.println(strings[1]);
                    Location location = new Location(Integer.parseInt(strings[1]));
                    tempLine = reader.readLine();
                    int beginIndex = tempLine.indexOf("<invariant>") + 11;
                    int endIndex = tempLine.indexOf("</invariant>");
                    String variant = tempLine.substring(beginIndex,endIndex).trim();
                    //System.out.println(variant);
                    location.setVariant(variant,parameters);
                    tempLine = reader.readLine();
                    beginIndex = tempLine.indexOf("<flow>") + 6;
                    endIndex = tempLine.indexOf("</flow>");
                    String flow = tempLine.substring(beginIndex,endIndex).trim();
                    //System.out.println(flow);
                    location.setFlow(flow);

                    locations.add(location);
                }
                if(tempLine.indexOf("<transition") != -1){ // transition definition
                    String []strings = tempLine.split("\"");
                    int source = Integer.parseInt(strings[1]);
                    int target = Integer.parseInt(strings[3]);
                    Transition transition = new Transition(source,target);
                    tempLine = reader.readLine(); // label (useless)
                    tempLine = reader.readLine(); // guard
                    int beginIndex = tempLine.indexOf("<guard>") + 7;
                    int endIndex = tempLine.indexOf("</guard>");
                    String guard = tempLine.substring(beginIndex,endIndex).trim();
//                    System.out.println(source + "->" + target);
//                    System.out.println(guard);
                    transition.setGuard(guard,parameters);

                    tempLine = reader.readLine();
                    beginIndex = tempLine.indexOf("<assignment>") + 12;
                    endIndex = tempLine.indexOf("</assignment>");
                    String assignment = tempLine.substring(beginIndex,endIndex).trim();
                    transition.setAssignment(assignment,parameters);

                    transitions.add(transition);
                }
            }


        }
        catch (FileNotFoundException e){
            System.out.println("File not found" + '\n' + e.getMessage());
        }
        catch (IOException e){
            System.out.println("IO Exception" + '\n' + e.getMessage());
        }
        finally {
            if(reader != null){
                try{
                    reader.close();
                }
                catch (IOException e){
                    System.out.println("IO Exception" + '\n' + e.getMessage());
                }
            }
        }
    }

    void setInitParameterValues(String initValues){
        String []strings = initValues.split("&");
        for(int i = 0;i < strings.length;++i){
            String []temp = strings[i].split("==");
            if(temp[0].trim().equals("loc()")){
                initLoc = Integer.parseInt(temp[1].trim().substring(1));
            }
            else{
                initParameterValues.put(temp[0].trim(),Double.parseDouble(temp[1].trim()));
            }
        }
    }

    void setForbiddenValues(String forbiddenValues){
        String []strings = forbiddenValues.split("&");
        for(int i = 0;i < strings.length;++i){

        }
    }

    public static void main(String []args){
        Automata automata = new Automata("/home/cedricxing/Downloads/model.xml","/home/cedricxing/Downloads/cfg.txt");
        int samplesize = 30;       // parameter: the number of samples in each iteration
        int iteration = 1000;       // parameter: the number of iterations for batch racos
        int budget = 2000;         // parameter: the budget of sampling for sequential racos
        int positivenum = 1;       // parameter: the number of positive instances in each iteration
        double probability = 0.95; // parameter: the probability of sampling from the model
        int uncertainbit = 1;      // parameter: the number of sampled dimensions

        int maxPathSize = 2;
        for(int i = 1;i < maxPathSize;++i){
            int []path = new int[i];

        }
    }
}
