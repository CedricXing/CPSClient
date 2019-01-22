package MPC;

import Racos.Componet.Instance;
import Racos.Method.Continue;
import Racos.Method.Discrete;
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
    public String initLocName;
    public String forbiddenLocName;
    public int forbiddenLoc;
    public Map<String,Double> initParameterValues;
    public ArrayList<String> forbiddenConstraints;
    public double cycle;
    public String cycleConstraint;
    File output;
    BufferedWriter bufferedWriter;

    public Automata(String modelFileName,String cfgFileName){
        forbiddenLocName = null;
        forbiddenLoc = -1;
        initLocName = null;
        initLoc = -1;
        cycle = -1;
        processModelFile(modelFileName);
        processCFGFile(cfgFileName);
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
                if(tempLine.indexOf("<param") != -1){ // paramater definition
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
                    //ID stores in strings[1]
                    Location location = new Location(Integer.parseInt(strings[1]),strings[3]);
                    System.out.println(strings[3]);
                    tempLine = reader.readLine();
                    while(tempLine.indexOf("</location>") == -1){//the end of this location
                        int beginIndex,endIndex;
                        if(tempLine.indexOf("<invar") != -1){
                            beginIndex = tempLine.indexOf("<invariant>") + 11;
                            endIndex = tempLine.indexOf("</invariant>");
                            String variant = tempLine.substring(beginIndex,endIndex).trim();
                            location.setVariant(variant,parameters);
                        }
                        if(tempLine.indexOf("<flow>") != -1){
                            beginIndex = tempLine.indexOf("<flow>") + 6;
                            endIndex = tempLine.indexOf("</flow>");
                            //System.out.println(tempLine);
                            String flow = tempLine.substring(beginIndex,endIndex).trim();
                            location.setFlow(flow,parameters);
                        }
                        tempLine = reader.readLine();
                    }
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
                    while(tempLine.indexOf("</transi") == -1){
                        int beginIndex,endIndex;
                        if(tempLine.indexOf("<guard>") != -1){
                            beginIndex = tempLine.indexOf("<guard>") + 7;
                            endIndex = tempLine.indexOf("</guard>");
                            String guard = tempLine.substring(beginIndex,endIndex).trim();
                            transition.setGuard(guard,parameters);
                        }
                        if(tempLine.indexOf("<assignment>") != -1){
                            beginIndex = tempLine.indexOf("<assignment>") + 12;
                            endIndex = tempLine.indexOf("</assignment>");
                            String assignment = tempLine.substring(beginIndex,endIndex).trim();
                            transition.setAssignment(assignment,parameters);
                        }
                        tempLine = reader.readLine();
                    }
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

    public void setInitParameterValues(String initValues){
        String []strings = initValues.split("&");
        for(int i = 0;i < strings.length;++i){
            String []temp = strings[i].split("==");
            if(temp[0].trim().equals("loc()")){
                initLocName = temp[1].trim();
                for(Map.Entry<Integer,Location> entry : locations.entrySet()){
                    //System.out.println(allParametersValues.size());
                    if(entry.getValue().name.equals(initLocName)){
                        initLoc = entry.getKey();
                        break;
                    }
                }
            }
            else if(temp[1].indexOf('[') != -1){ // range value
                int firstIndex = temp[1].indexOf("[");
                int lastIndex = temp[1].indexOf("]");
                String []bounds = temp[1].substring(firstIndex + 1,lastIndex).trim().split(",");
                double lowerbound = Double.parseDouble(bounds[0].trim());
                double upperbound = Double.parseDouble(bounds[1].trim());
                double randomValue = (upperbound + lowerbound) / 2 + (Math.random() - 0.5) * (upperbound - lowerbound);
                initParameterValues.put(temp[0].trim(),randomValue);
                //System.out.println(temp[0] + Double.toString(randomValue));
                //System.exit(0);
            }
            else{
                initParameterValues.put(temp[0].trim(),Double.parseDouble(temp[1].trim()));
            }
        }
        if(initLoc == -1){
            System.out.println("Error ==> It is mandatory to set init loc.");
            System.exit(-1);
        }
    }

    public void setForbiddenValues(String forbiddenValues){
        String []strings = forbiddenValues.split("&");
        for(int i = 0;i < strings.length;++i){
            if(strings[i].indexOf("loc()") != -1){
                String []temps = strings[i].split("=");
                forbiddenLocName = temps[temps.length - 1].trim();
                for(Map.Entry<Integer,Location> entry : locations.entrySet()){
                    //System.out.println(allParametersValues.size());
                    if(entry.getValue().name.equals(forbiddenLocName)){
                        forbiddenLoc = entry.getKey();
                        break;
                    }
                }
                continue;
            }
            if(strings[i].trim().indexOf("t<=") != -1){
                cycle = Double.parseDouble(strings[i].trim().substring(3).trim());
                cycleConstraint = new String("t>=" + cycle);
                continue;
            }
            if(strings[i].trim().indexOf("t<") != -1){
                cycle = Double.parseDouble(strings[i].trim().substring(2).trim());
                cycleConstraint = new String("t>" + cycle);
                continue;
            }
            if(strings[i].trim().indexOf('(') != -1){
                strings[i] = strings[i].substring(strings[i].indexOf('(')+1,strings[i].indexOf(')')).trim();
            }
            forbiddenConstraints.add(strings[i].trim());
        }
    }

    public int getInitLoc(){
        return initLoc;
    }

    void DFS(Automata automata,int []path,int depth,int maxPathSize){
        if(depth + 1 == maxPathSize){
            //System.out.println("The depth is " + maxPathSize);
            println("The depth is " + maxPathSize);
            for(int i = 0;i < path.length - 1;++i){
                //System.out.print(path[i] + "->");
                print(path[i] + "->");
            }
            //System.out.println(path[path.length - 1]);
            println(Integer.toString(path[path.length - 1]));
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
        double currentT = System.currentTimeMillis();
        int samplesize = 1 ;       // parameter: the number of samples in each iteration
        int iteration = 1000;       // parameter: the number of iterations for batch racos
        int budget = 2000 ;         // parameter: the budget of sampling for sequential racos
        int positivenum = 10;       // parameter: the number of positive instances in each iteration
        double probability = 0.95; // parameter: the probability of sampling from the model
        int uncertainbit = 3;      // parameter: the number of sampled dimensions
        Instance ins = null;
        int repeat = 1;
        Task t = new ObjectFunction(automata,path);
        ArrayList<Instance> result = new ArrayList<>();
        for (int i = 0; i < repeat; i++) {
            Continue con = new Continue(t);
            con.setMaxIteration(iteration);
            con.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
            con.setBudget(budget);              // parameter: the budget of sampling
            con.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
            con.setRandProbability(probability);// parameter: the probability of sampling from the model
            con.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
            con.run();                          // call sequential Racos              // call Racos
            ins = con.getOptimal();             // obtain optimal
            //System.out.print("best function value:");
            print("best function value:");
            if(ins.getValue() == Double.MAX_VALUE)
                System.out.print("MaxValue     ");
            else {
                //System.out.print(ins.getValue() + "    ");
                print(ins.getValue() + "    ");
                result.add(ins);
            }
            //System.out.print("[");
            print("[");
            for(int j = 0;j < ins.getFeature().length;++j) {
                //System.out.print(ins.getFeature(j) * ((ObjectFunction) t).delta + ",");
                //print(Double.toString(ins.getFeature(j) * ((ObjectFunction) t).delta) + ",");
                print(Double.toString(ins.getFeature(j)) + ",");
            }
            //System.out.println("]");
            println("]");
        }
        double currentT2 = System.currentTimeMillis();
        for(int i = 0;i < result.size();++i){
            //System.out.println(result.get(i).getValue());
            println(Double.toString(result.get(i).getValue()));
            //System.out.print("[");
            print("[");
            for(int j = 0;j < result.get(i).getFeature().length;++j) {
                //System.out.print(result.get(i).getFeature(j) * ((ObjectFunction) t).delta+ ",");


                //print(Double.toString(result.get(i).getFeature(j) * ((ObjectFunction) t).delta) + ",");
                print(Double.toString(result.get(i).getFeature(j)) + ",");
            }
            //System.out.println("]");
            println("]");
        }
        println("Average time : " + Double.toString((currentT2 - currentT) / repeat / 1000));
    }

    void checkAutomata(){
        println("Init loc is " + initLocName);
        println("Init loc is " + initLoc);
        for(Map.Entry<String,Double> entry : initParameterValues.entrySet()){
            println("The init value of " + entry.getKey() + " is " + entry.getValue());
            System.out.println("The init value of " + entry.getKey() + " is " + entry.getValue());
        }
        println("Forbidden loc is " + forbiddenLocName);
        println("Forbidden loc is " + forbiddenLoc);
        println("Forbidden constraints is ");
        for(int i = 0;i < forbiddenConstraints.size();++i){
            println(forbiddenConstraints.get(i));
            System.out.println(forbiddenConstraints.get(i));
        }
        for(Map.Entry<Integer,Location> entry : locations.entrySet()){
            println(Integer.toString(entry.getKey()));
            entry.getValue().printLocation();
            println("**************");
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

    public Transition getTransitionBySourceAndTarget(int source,int target){
        for(int i = 0;i < transitions.size();++i){
            if(transitions.get(i).source == source && transitions.get(i).target == target)
                return transitions.get(i);
        }
        return null;
    }

    public void println(String str){
        try {
            bufferedWriter.write(str + "\n");
        }
        catch (IOException e){
            System.out.println("write to file error!");
        }
    }

    public void print(String str){
        try {
            bufferedWriter.write(str);
        }
        catch (IOException e){
            System.out.println("write to file error!");
        }
    }

    public static void main(String []args){
//        Automata automata = new Automata("/home/cedricxing/Desktop/cases/arch2017/arch2017_nonlinear_dynamics/quadrotor/quadrotor.xml",
//                "/home/cedricxing/Desktop/cases/arch2017/arch2017_nonlinear_dynamics/quadrotor/quadrotor.cfg");
//        //Automata automata = new Automata("/home/cedricxing/Desktop/CPS/src/case/train.xml",
//         //       "/home/cedricxing/Desktop/CPS/src/case/train.cfg");
//        automata.checkAutomata();
        //automata.output = new File("output/test_boucing_ball3.txt");
        int repeat = 0;
        while(repeat < 3) {
            Automata automata = new Automata("/home/cedricxing/Desktop/CPS/src/case/platoon_hybrid.xml",
                    "/home/cedricxing/Desktop/CPS/src/case/platoon.cfg");
            //Automata automata = new Automata("/home/cedricxing/Desktop/CPS/src/case/train.xml",
            //       "/home/cedricxing/Desktop/CPS/src/case/train.cfg");
            automata.output = new File("output/platoon" + repeat + ".txt");
            try {
                automata.bufferedWriter = new BufferedWriter(new FileWriter(automata.output));
                automata.checkAutomata();
                int maxPathSize = 2;
                for (int i = 1; i <= maxPathSize; ++i) {
                    int[] path = new int[i];
                    path[0] = automata.getInitLoc();
                    automata.DFS(automata, path, 0, i);
                }
            } catch (IOException e) {
                System.out.println("Open output.txt fail!");
            } finally {
                if (automata.bufferedWriter != null) {
                    try {
                        automata.bufferedWriter.close();
                    } catch (IOException e) {
                        System.out.println("IO Exception" + '\n' + e.getMessage());
                    }
                }
            }
            ++repeat;
        }
    }
}
