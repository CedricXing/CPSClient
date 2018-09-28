package MPC;

import Racos.Componet.Instance;
import Racos.Method.Continue;
import Racos.ObjectiveFunction.MIN_L;
import Racos.ObjectiveFunction.ObjectFunction;
import Racos.ObjectiveFunction.Task;

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
    public Map<Integer,Location> locations;
    public ArrayList<Transition> transitions;
    public ArrayList<String> parameters;
    public int initLoc;
    public int forbiddenLoc;
    public Map<String,Double> initParameterValues;
    public ArrayList<String> forbiddenConstraints;
    public double cycle;
    public String cycleConstraint;

    public Automata(String modelFileName,String cfgFileName){
        forbiddenLoc = -1;
        cycle = -1;
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
        locations = new HashMap<>();
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
                    location.setFlow(flow,parameters);

                    locations.put(location.getNo(),location);
                }
                if(tempLine.indexOf("<transition") != -1){ // transition definition
                    String []strings = tempLine.split("\"");
                    int source = Integer.parseInt(strings[1]);
                    int target = Integer.parseInt(strings[3]);
                    Transition transition = new Transition(source,target);
                    locations.get(source).addNeibour(target);
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

    public void setInitParameterValues(String initValues){
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

    public void setForbiddenValues(String forbiddenValues){
        String []strings = forbiddenValues.split("&");
        for(int i = 0;i < strings.length;++i){
            if(strings[i].indexOf("loc()") != -1){
                forbiddenLoc = Integer.parseInt(strings[i].substring(strings[i].indexOf("v") + 1));
                continue;
            }
            if(strings[i].trim().indexOf("t<=") != -1){
                cycle = Double.parseDouble(strings[i].trim().substring(3));
                cycleConstraint = new String("t>=" + cycle);
                continue;
            }
            if(strings[i].trim().indexOf("t<") != -1){
                cycle = Double.parseDouble(strings[i].trim().substring(2));
                cycleConstraint = new String("t>" + cycle);
                continue;
            }
            forbiddenConstraints.add(strings[i].trim());
//            for(int j = parameters.size() - 1;j >= 0;--j ){
//                String tempString = strings[i].replace(parameters.get(j),"$" + j);
//            }
        }
    }

    public int getInitLoc(){
        return initLoc;
    }

    void DFS(Automata automata,int []path,int depth,int maxPathSize){
        if(depth + 1 == maxPathSize){
            System.out.println("hello");
            runRacos(automata,path);
        }
        else{
            ArrayList<Integer> neibours = automata.locations.get(path[depth]).getNeibours();
            for(int i = 0;i < neibours.size();++i){
                path[depth + 1] = neibours.get(i);
                DFS(automata,path,depth + 1,maxPathSize);
            }
        }
    }

    void runRacos(Automata automata,int []path){
        int samplesize = 30;       // parameter: the number of samples in each iteration
        int iteration = 10000;       // parameter: the number of iterations for batch racos
        int budget = 2000;         // parameter: the budget of sampling for sequential racos
        int positivenum = 1;       // parameter: the number of positive instances in each iteration
        double probability = 0.95; // parameter: the probability of sampling from the model
        int uncertainbit = 1;      // parameter: the number of sampled dimensions
        Instance ins = null;
        int repeat = 1;
        Task t = new ObjectFunction(automata,path);
        ArrayList<Instance> result = new ArrayList<>();
        for (int i = 0; i < repeat; i++) {
            Continue conti = new Continue(t);
            conti.TurnOnSequentialRacos();
            conti.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
            conti.setBudget(budget);              // parameter: the budget of sampling
            conti.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
            conti.setRandProbability(probability);// parameter: the probability of sampling from the model
            conti.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
            conti.run();                          // call sequential Racos
            ins = conti.getOptimal();             // obtain optimal
            System.out.print("best function value:");
            if(ins.getValue() == Double.MAX_VALUE)
                System.out.print("MaxValue     ");
            else {
                System.out.print(ins.getValue() + "    ");
                result.add(ins);
            }
            System.out.print("[");
            for(int j = 0;j < ins.getFeature().length;++j)
                System.out.print(ins.getFeature(j) + ",");
            System.out.println("]");
        }
        for(int i = 0;i < result.size();++i){
            System.out.println(result.get(i).getValue());
            System.out.print("[");
            for(int j = 0;j < result.get(i).getFeature().length;++j)
                System.out.print(result.get(i).getFeature(j) + ",");
            System.out.println("]");
        }
    }

    void checkAutomata(){
        System.out.println("Init loc is " + initLoc);
        for(Map.Entry<String,Double> entry : initParameterValues.entrySet()){
            System.out.println("The init value of " + entry.getKey() + " is " + entry.getValue());
        }
        System.out.println("Forbidden loc is " + forbiddenLoc);
        System.out.println("Forbidden constraints is ");
        for(int i = 0;i < forbiddenConstraints.size();++i){
            System.out.println(forbiddenConstraints.get(i));
        }
        for(Map.Entry<Integer,Location> entry : locations.entrySet()){
            System.out.println(entry.getKey());
            entry.getValue().printLocation();
            System.out.println("**************");
        }

        for(int i = 0;i < transitions.size();++i){
            transitions.get(i).printTransition();
        }
    }

    public HashMap<String,Double> duplicateInitParametersValues(){
        HashMap<String,Double> newMap = new HashMap<>();
        for(Map.Entry<String,Double> entry : initParameterValues.entrySet()){
            newMap.put(entry.getKey(),entry.getValue());
        }
        return newMap;
    }

    public static void main(String []args){
        Automata automata = new Automata("/home/cedricxing/Downloads/model1.xml","/home/cedricxing/Downloads/cfg.txt");
        //automata.checkAutomata();
        int maxPathSize = 3;
        for(int i = 2;i <= maxPathSize;++i){
            int []path = new int[i];
            path[0] = automata.getInitLoc();
            automata.DFS(automata,path,0,i);
        }
    }
}
