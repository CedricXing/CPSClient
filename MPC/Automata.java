package MPC;

import Racos.Componet.Instance;
import Racos.Method.Continue;
import Racos.ObjectiveFunction.ObjectFunction;
import Racos.ObjectiveFunction.Task;
import Racos.Tools.ValueArc;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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
    public String forbiddenConstraints;
    public double cycle;
    public String cycleConstraint;
    File output;
    BufferedWriter bufferedWriter;
    public ArrayList<RangeParameter> rangeParameters;
    public ValueArc minValueArc;
    public double delta = 0.05;

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
                    //System.out.println(strings[3]);
                    tempLine = reader.readLine();
                    while(tempLine.indexOf("</location>") == -1){//the end of this location
                        int beginIndex,endIndex;
                        if(tempLine.indexOf("<invar") != -1){
                            while(tempLine.indexOf("</invar") == -1){
                                if(tempLine.indexOf("<invar") != -1){
                                    beginIndex = tempLine.indexOf("<invar") + 11;
                                    tempLine = tempLine.substring(beginIndex).trim();
                                }
                                location.setVariant(tempLine,parameters);
                                tempLine = reader.readLine();
                            }
                            if(tempLine.indexOf("<invar") != -1){
                                beginIndex = tempLine.indexOf("<invar") + 11;
                                endIndex = tempLine.indexOf("</invar");
                                tempLine = tempLine.substring(beginIndex,endIndex).trim();
                            }
                            else{
                                endIndex = tempLine.indexOf("</invar");
                                tempLine = tempLine.substring(0,endIndex).trim();
                            }
                            location.setVariant(tempLine,parameters);
                        }
                        if(tempLine.indexOf("<flow>") != -1){
                            while(tempLine.indexOf("</flow>") == -1){
                                if(tempLine.indexOf("<flow>") != -1){
                                    beginIndex = tempLine.indexOf("<flow>") + 6;
                                    tempLine = tempLine.substring(beginIndex).trim();
                                }
                                location.setFlow(tempLine,parameters);
                                tempLine = reader.readLine();
                            }
                            if(tempLine.indexOf("<flow>") != -1) {
                                beginIndex = tempLine.indexOf("<flow>") + 6;
                                endIndex = tempLine.indexOf("</flow>");
                                tempLine = tempLine.substring(beginIndex,endIndex).trim();
                            }
                            else{
                                endIndex = tempLine.indexOf("</flow>");
                                tempLine = tempLine.substring(0,endIndex).trim();
                            }
                            //System.out.println(tempLine);
                            location.setFlow(tempLine,parameters);
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
                    //tempLine = reader.readLine(); // label (useless)
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
        assert(forbiddenConstraints instanceof String);
        //forbiddenConstraints = new ArrayList<>();
        try{
            reader = new BufferedReader(new FileReader(cfgFile));
            String tempLine = null;
            while((tempLine = reader.readLine()) != null){
                if(tempLine.charAt(0) == '#')
                    continue;
                if(tempLine.indexOf("initially") != -1){
                    String []strings = tempLine.split("\"");
                    setInitParameterValues(strings[1]);
                }
                if(tempLine.indexOf("forbidden") != -1){
                    String []strings = tempLine.split("\"");
                    strings[1] = strings[1].replace("pow","$(Math).pow");
                    strings[1] = strings[1].replace("sin","$(Math).sin");
                    strings[1] = strings[1].replace("cos","$(Math).cos");
                    strings[1] = strings[1].replace("tan","$(Math).tan");
                    strings[1] = strings[1].replace("sqrt","$(Math).sqrt");
                    forbiddenConstraints = strings[1];
                    //setForbiddenValues(strings[1]);
                }
                if(tempLine.indexOf("time-horizon") != -1){
                    String []strings = tempLine.split("\"");
                    cycle = Double.parseDouble(strings[1]);
                    cycleConstraint = new String("t>" + cycle);
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
            else if(temp[1].indexOf('[') != -1){ // range value,put into Racos
                int firstIndex = temp[1].indexOf("[");
                int lastIndex = temp[1].indexOf("]");
                String []bounds = temp[1].substring(firstIndex + 1,lastIndex).trim().split(",");
                double lowerbound = Double.parseDouble(bounds[0].trim());
                double upperbound = Double.parseDouble(bounds[1].trim());
//                double randomValue = (upperbound + lowerbound) / 2 + (Math.random() - 0.5) * (upperbound - lowerbound);
//                initParameterValues.put(temp[0].trim(),randomValue);
                if(rangeParameters == null) rangeParameters = new ArrayList<>();
                rangeParameters.add(new RangeParameter(temp[0].trim(),lowerbound,upperbound));
                //System.out.println(temp[0] + Double.toString(randomValue));
                //System.exit(0);
            }
            else{
                initParameterValues.put(temp[0].trim(),Double.parseDouble(temp[1].trim()));
            }
        }
//        if(initLoc == -1){
//            System.out.println("Error ==> It is mandatory to set init loc.");
//            System.exit(-1);
//        }
    }

    /*
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
//            if(strings[i].trim().indexOf("t<=") != -1){
//                cycle = Double.parseDouble(strings[i].trim().substring(3).trim());
//                cycleConstraint = new String("t>=" + cycle);
//                continue;
//            }
//            if(strings[i].trim().indexOf("t<") != -1){
//                cycle = Double.parseDouble(strings[i].trim().substring(2).trim());
//                cycleConstraint = new String("t>" + cycle);
//                continue;
//            }
            if(strings[i].trim().indexOf('(') != -1){
                strings[i] = strings[i].substring(strings[i].indexOf('(')+1,strings[i].indexOf(')')).trim();
            }
            forbiddenConstraints.add(strings[i].trim());
        }
    }
    */
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

    void DFS1(Automata automata,ArrayList<Integer> arrayListPath,int maxPathSize){
        int len = arrayListPath.size();
        int path[] = new int[len];
        for(int i = 0;i < len;++i)
            path[i] = arrayListPath.get(i);
        println("The depth is " + len);
        System.out.println("The depth is " + len);
        for(int i = 0;i < len - 1;++i){
            System.out.print(path[i] + "->");
            print(path[i] + "->");
        }
        System.out.println(path[len-1]);
        println(Integer.toString(path[len-1]));
        boolean pruning = runRacos(automata,path);
        if(pruning || len == maxPathSize)
            return;
        ArrayList<Integer> neibours = automata.locations.get(path[len-1]).getNeibours();
        for(int i = 0;i < neibours.size();++i){
            int nextPos = neibours.get(i);
            if(arrayListPath.contains(nextPos)) continue;
            arrayListPath.add(neibours.get(i));
            DFS1(automata,arrayListPath,maxPathSize);
            arrayListPath.remove(arrayListPath.size()-1);
        }

    }


    boolean runRacos(Automata automata,int []path){
        int samplesize = 1;       // parameter: the number of samples in each iteration
        int iteration = 500;       // parameter: the number of iterations for batch racos
        int budget = 2000 ;         // parameter: the budget of sampling for sequential racos
        int positivenum = 1;       // parameter: the number of positive instances in each iteration
        double probability = 0.95; // parameter: the probability of sampling from the model
        int uncertainbit = 1;      // parameter: the number of sampled dimensions
        Instance ins = null;
        int repeat = 1;
        Task t = new ObjectFunction(automata,path);
        ArrayList<Instance> result = new ArrayList<>();
        ArrayList<Instance> feasibleResult = new ArrayList<>();
        double feasibleResultAllTime = 0;
        boolean pruning = true;
        for (int i = 0; i < repeat; i++) {
            double currentT = System.currentTimeMillis();
            Continue con = new Continue(t,automata);
            con.setMaxIteration(iteration);
            con.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
            con.setBudget(budget);              // parameter: the budget of sampling
            con.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
            con.setRandProbability(probability);// parameter: the probability of sampling from the model
            con.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
            ValueArc valueArc = con.run();                          // call sequential Racos              // call Racos
            double currentT2 = System.currentTimeMillis();
            ins = con.getOptimal();             // obtain optimal
            //System.out.print("best function value:");
            //System.out.println(valueArc.penAll);
            if(ins.getValue() < 0){
                feasibleResult.add(ins);
                feasibleResultAllTime += (currentT2-currentT) / 1000;
                pruning = false;
                if(minValueArc == null || minValueArc.value >= valueArc.value){
                    minValueArc = valueArc;
                    minValueArc.path = path;
                }
                //System.out.println(valueArc.allParametersValues.get("x"));
            }
            else if(valueArc.penalty < 0){
                pruning = false;
                //print("----------------------" + valueArc.penalty + "-------" + valueArc.globalPenalty + "-----------\n");
            }
            print("best function value:");
            print(ins.getValue() + "     ");
            result.add(ins);
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
        println("Feasible Result:");
        for(int i = 0;i < feasibleResult.size();++i){
            println(Double.toString(feasibleResult.get(i).getValue()));
            print("[");
            for(int j = 0;j < feasibleResult.get(i).getFeature().length;++j) {
                print(Double.toString(feasibleResult.get(i).getFeature(j)) + ",");
            }
            println("]");
        }
        println("Average time : " + Double.toString(feasibleResultAllTime / feasibleResult.size()));
        System.out.println("Average time : " + Double.toString(feasibleResultAllTime / feasibleResult.size()));
        return pruning;
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
        assert(forbiddenConstraints instanceof String);
//        for(int i = 0;i < forbiddenConstraints.size();++i){
//            println(forbiddenConstraints.get(i));
//            System.out.println(forbiddenConstraints.get(i));
//        }
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
        File file_ = new File("output/success_id.txt");
        int success_id = 0;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file_));
            success_id = bufferedReader.read();
        }
        catch (IOException e){
            System.out.println("io exception.");
        }
        String []modelFiles = new String[]{"src/case/boucing_ball.xml", // 0
                                           "src/case/quadrotor.xml", // 1
                                           "src/case/model_passive_4d.xml", // 2
                                           "src/case/platoon_hybrid.xml",// 3
                                           "src/case/helir_10.xml",// 4
                                            "src/case/productionSystem.xml",// 5
                                            "src/case/new_train.xml",// 6
                                            "src/case/new_quad.xml", // 7
                                            "src/case/new_quad_expanded.xml"}; // 8
        String []cfgFiles = new String[]{"src/case/bouncing_ball_racos.cfg",
                                         "src/case/quadrotor.cfg",
                                         "src/case/COLLISION.cfg",
                                         "src/case/platoon.cfg",
                                         "src/case/helir_10.cfg",
                                          "src/case/productionSystem.cfg",
                                            "src/case/new_train.cfg",
                                            "src/case/new_quad.cfg",
                                            "src/case/new_quad_expanded.cfg"};
        for(int fileIndex = 8;fileIndex < modelFiles.length;++fileIndex) {
            String []temp = modelFiles[fileIndex].split("/");
            int repeat = 0;
            double currentTime = System.currentTimeMillis();
            while (repeat < 1) {
                Automata automata = new Automata(modelFiles[fileIndex], cfgFiles[fileIndex]);
                //Automata automata = new Automata("/home/cedricxing/Desktop/CPS/src/case/train.xml",
                //       "/home/cedricxing/Desktop/CPS/src/case/train.cfg");
                automata.output = new File("output/new_quad" + "_newSampleSize_" + repeat + ".txt");
                try {
                    automata.bufferedWriter = new BufferedWriter(new FileWriter(automata.output));
                    automata.checkAutomata();
                    int maxPathSize = 3;
//                    ArrayList<Integer> arrayListPath = new ArrayList<>();
//                    if (automata.getInitLoc() != -1) {
//                        arrayListPath.add(automata.getInitLoc());
//                        automata.DFS1(automata, arrayListPath, maxPathSize);
//                    } else {
//                        for (Map.Entry<Integer, Location> entry : automata.locations.entrySet()) {
//                            arrayListPath.clear();
//                            arrayListPath.add(entry.getValue().getNo());
//                            automata.DFS1(automata, arrayListPath, maxPathSize);
//                        }
//                    }


                   File file = new File("output/result/result_" + success_id + ".txt");
                   BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                   while(automata.initParameterValues.get("x") < 40 && Math.abs(40-automata.initParameterValues.get("x")) > 0.5){
//                   while(Math.abs(40-automata.initParameterValues.get("x")) > 1){
//                   while(Math.abs(200 - automata.initParameterValues.get("x")) + Math.abs(200-automata.initParameterValues.get("y")) > 1){
                       //automata.DFS1(automata,arrayListPath,maxPathSize);
                       ArrayList<Integer> arrayListPath = new ArrayList<>();
                       if(automata.getInitLoc() != -1) {
                           arrayListPath.add(automata.getInitLoc());
                           automata.DFS1(automata,arrayListPath,maxPathSize);
                       }
                       else{
                           for(Map.Entry<Integer,Location> entry:automata.locations.entrySet() ){
                               arrayListPath.clear();
                               arrayListPath.add(entry.getValue().getNo());
                               automata.DFS1(automata,arrayListPath,maxPathSize);
                           }
                       }
                       HashMap<String ,Double> map = automata.minValueArc.allParametersValues;
                       int index;
                       for(index = 0;index < automata.minValueArc.args.length;++index){
                           if(automata.minValueArc.args[index] != 0) {
                               break;
                           }
                       }
                       if(index == automata.minValueArc.args.length)
                           continue;
                       for(int i = 0;i < automata.minValueArc.path.length;++i)
                           bufferedWriter.write(automata.minValueArc.path[i] + ",");
                       bufferedWriter.write(" & ");
                       for(int i = 0;i < automata.minValueArc.args.length;++i) {
                           System.out.println(automata.minValueArc.args[i] + " & ");
                           bufferedWriter.write(automata.minValueArc.args[i] + " & ");
                       }
                       //bufferedWriter.write(map.get("a1") + " & " + map.get("a2") + " & " + map.get("a3") + " & " + map.get("b1") + " & " + map.get("b2") + " & " + map.get("b3") + " & " + map.get("u1") + " & " + map.get("u2") + " & " + map.get("x")  + " & " + map.get("y") + "\n");
//                       bufferedWriter.write(map.get("u1") + " & " + map.get("u2") + " & " + map.get("x")  + " & " + map.get("y") + "\n");
//                       bufferedWriter.write(map.get("T11") + " & " + map.get("T12") + " & " + map.get("T13") + " & " + map.get("T21") + " & " + map.get("T22") + " & " + map.get("T23") + " & " + map.get("T31") + " & " + map.get("T32") + " & " + map.get("T33")  + " & " + map.get("y") + "\n");
                       bufferedWriter.write("itera:" + automata.minValueArc.iterativeNums + "\n");
                       System.out.println(map.get("x") + " " + map.get("y"));
                       System.out.println("vx : " + map.get("vx") + " " + "vy : " + map.get("vy"));
                       System.out.println("angle : " + map.get("angle"));

                       //System.out.println(map.get("a1") + " & " + map.get("a2") + " & " + map.get("a3") + " & " + map.get("u1") + " & " + map.get("u2")) ;
                       if(map.containsKey("x"))
                           automata.initParameterValues.put("x",map.get("x"));
                       if(map.containsKey("y"))
                           automata.initParameterValues.put("y",map.get("y"));
                       if(map.containsKey("angle"))
                           automata.initParameterValues.put("angle",map.get("angle"));
                       if(map.containsKey("vx"))
                           automata.initParameterValues.put("vx",map.get("vx"));
                       if(map.containsKey("vy"))
                           automata.initParameterValues.put("vy",map.get("vy"));
                       if(map.containsKey("fuel"))
                           automata.initParameterValues.put("fuel",map.get("fuel"));
                       if(map.containsKey("v"))
                           automata.initParameterValues.put("v",map.get("v"));
                       if(map.containsKey("u1"))
                           automata.initParameterValues.put("u1",map.get("u1"));
                       if(map.containsKey("u2"))
                           automata.initParameterValues.put("u2",map.get("u2"));
                       automata.minValueArc = null;
                   }
                    double endTime = System.currentTimeMillis();
                   bufferedWriter.write("time:" + (endTime-currentTime)/1000/60);
                   bufferedWriter.close();
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
            double endTime = System.currentTimeMillis();
            System.out.println("Time cost :" + (endTime-currentTime)/1000/60 + "minutes");

        }
    }
}
