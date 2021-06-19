package MPC;

import MPC.tools.Fel_ExpressionProc;
import Racos.Componet.Instance;
import Racos.Method.Continue;
import Racos.ObjectiveFunction.ObjectFunction;
import Racos.ObjectiveFunction.Task;
import Racos.Tools.ValueArc;
import sun.awt.SunHints;

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
    public Map<Integer, Location> locations;
    public ArrayList<Transition> transitions;
    public ArrayList<String> parameters;
    public int initLoc;
    public String initLocName;
    public String forbiddenLocName;
    public int forbiddenLoc;
    public Map<String, Double> initParameterValues;
    public String forbiddenConstraints;
    public double cycle;
    public String cycleConstraint;
    File output;
    BufferedWriter bufferedWriter;
    public ArrayList<RangeParameter> rangeParameters;
    public ValueArc minValueArc;
    public double delta = 0.05;
    public double target_x;
    public double target_y;
    public String obj_function;

    public Automata(String modelFileName, String cfgFileName) {
        forbiddenLocName = null;
        forbiddenLoc = -1;
        initLocName = null;
        initLoc = -1;
        cycle = -1;
        target_x = target_y = -1;
        obj_function = null;
        processModelFile(modelFileName);
        processCFGFile(cfgFileName);
    }

    void processModelFile(String modelFileName) {
        File modelFile = new File(modelFileName);
        BufferedReader reader = null;
        locations = new HashMap<>();
        transitions = new ArrayList<>();
        parameters = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(modelFile));
            String tempLine = null;
            while ((tempLine = reader.readLine()) != null) {
                if (tempLine.indexOf("<param") != -1) { // paramater definition
                    while (true) {
                        String[] strings = tempLine.split("\"");
                        if (strings[3].equals("real"))
                            parameters.add(strings[1]);
                        tempLine = reader.readLine();
                        if (tempLine.indexOf("<para") == -1) {
                            parameters.sort(new Comparator<String>() {
                                @Override
                                public int compare(String o1, String o2) {
                                    if (o1.length() < o2.length())
                                        return -1;
                                    else if (o1.length() > o2.length())
                                        return 1;
                                    else return 0;
                                }
                            });
                            break;
                        }
                    }
                }
                if (tempLine.indexOf("<location") != -1) { // location definition
                    String[] strings = tempLine.split("\"");
                    //ID stores in strings[1]
                    Location location = new Location(Integer.parseInt(strings[1]), strings[3]);
                    tempLine = reader.readLine();
                    while (tempLine.indexOf("</location>") == -1) {//the end of this location
                        int beginIndex, endIndex;
                        if (tempLine.indexOf("<invar") != -1) {
                            while (tempLine.indexOf("</invar") == -1) {
                                if (tempLine.indexOf("<invar") != -1) {
                                    beginIndex = tempLine.indexOf("<invar") + 11;
                                    tempLine = tempLine.substring(beginIndex).trim();
                                }
                                location.setVariant(tempLine, parameters);
                                tempLine = reader.readLine();
                            }
                            if (tempLine.indexOf("<invar") != -1) {
                                beginIndex = tempLine.indexOf("<invar") + 11;
                                endIndex = tempLine.indexOf("</invar");
                                tempLine = tempLine.substring(beginIndex, endIndex).trim();
                            } else {
                                endIndex = tempLine.indexOf("</invar");
                                tempLine = tempLine.substring(0, endIndex).trim();
                            }
                            location.setVariant(tempLine, parameters);
                        }
                        if (tempLine.indexOf("<flow>") != -1) {
                            while (tempLine.indexOf("</flow>") == -1) {
                                if (tempLine.indexOf("<flow>") != -1) {
                                    beginIndex = tempLine.indexOf("<flow>") + 6;
                                    tempLine = tempLine.substring(beginIndex).trim();
                                }
                                location.setFlow(tempLine, parameters);
                                tempLine = reader.readLine();
                            }
                            if (tempLine.indexOf("<flow>") != -1) {
                                beginIndex = tempLine.indexOf("<flow>") + 6;
                                endIndex = tempLine.indexOf("</flow>");
                                tempLine = tempLine.substring(beginIndex, endIndex).trim();
                            } else {
                                endIndex = tempLine.indexOf("</flow>");
                                tempLine = tempLine.substring(0, endIndex).trim();
                            }
                            location.setFlow(tempLine, parameters);
                        }
                        tempLine = reader.readLine();
                    }
                    locations.put(location.getNo(), location);
                }
                if (tempLine.indexOf("<transition") != -1) { // transition definition
                    String[] strings = tempLine.split("\"");
                    int source = Integer.parseInt(strings[1]);
                    int target = Integer.parseInt(strings[3]);
                    Transition transition = new Transition(source, target);
                    locations.get(source).addNeibour(target);
                    tempLine = reader.readLine(); // guard
                    while (tempLine.indexOf("</transi") == -1) {
                        int beginIndex, endIndex;
                        if (tempLine.indexOf("<guard>") != -1) {
                            beginIndex = tempLine.indexOf("<guard>") + 7;
                            endIndex = tempLine.indexOf("</guard>");
                            String guard = tempLine.substring(beginIndex, endIndex).trim();
                            transition.setGuard(guard, parameters);
                        }
                        if (tempLine.indexOf("<assignment>") != -1) {
                            beginIndex = tempLine.indexOf("<assignment>") + 12;
                            endIndex = tempLine.indexOf("</assignment>");
                            String assignment = tempLine.substring(beginIndex, endIndex).trim();
                            transition.setAssignment(assignment, parameters);
                        }
                        tempLine = reader.readLine();
                    }
                    transitions.add(transition);
                }
            }


        } catch (FileNotFoundException e) {
            System.out.println("File not found" + '\n' + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Exception" + '\n' + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("IO Exception" + '\n' + e.getMessage());
                }
            }
        }
    }

    void processCFGFile(String cfgFileName) {
        File cfgFile = new File(cfgFileName);
        BufferedReader reader = null;
        initParameterValues = new HashMap<>();
        assert (forbiddenConstraints instanceof String);
        try {
            reader = new BufferedReader(new FileReader(cfgFile));
            String tempLine = null;
            while ((tempLine = reader.readLine()) != null) {
                if (tempLine.charAt(0) == '#')
                    continue;
                if (tempLine.startsWith("initially")) {
                    String[] strings = tempLine.split("\"");
                    setInitParameterValues(strings[1]);
                }
                if (tempLine.startsWith("forbidden")) {
                    String[] strings = tempLine.split("\"");
                    strings[1] = strings[1].replace("pow", "$(Math).pow");
                    strings[1] = strings[1].replace("sin", "$(Math).sin");
                    strings[1] = strings[1].replace("cos", "$(Math).cos");
                    strings[1] = strings[1].replace("tan", "$(Math).tan");
                    strings[1] = strings[1].replace("sqrt", "$(Math).sqrt");
                    forbiddenConstraints = strings[1];
                }
                if (tempLine.startsWith("time-horizon")) {
                    String[] strings = tempLine.split("\"");
                    cycle = Double.parseDouble(strings[1]);
                    cycleConstraint = new String("t>" + cycle);
                }
                if (tempLine.startsWith("target_x")) {
                    String[] strings = tempLine.split("\"");
                    target_x = Double.parseDouble(strings[1]);
                }
                if (tempLine.startsWith("target_y")) {
                    String[] strings = tempLine.split("\"");
                    target_y = Double.parseDouble(strings[1]);
                }
                if (tempLine.startsWith("obj_function")){
                    String[] strings = tempLine.split("\"");
                    obj_function= Fel_ExpressionProc.processMathFunction(strings[1]);
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found" + '\n' + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Exception" + '\n' + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("IO Exception" + '\n' + e.getMessage());
                }
            }
        }
    }

    public void setInitParameterValues(String initValues) {
        String[] strings = initValues.split("&");
        for (int i = 0; i < strings.length; ++i) {
            String[] temp = strings[i].split("==");
            if (temp[0].trim().equals("loc()")) {
                initLocName = temp[1].trim();
                for (Map.Entry<Integer, Location> entry : locations.entrySet()) {
                    //System.out.println(allParametersValues.size());
                    if (entry.getValue().name.equals(initLocName)) {
                        initLoc = entry.getKey();
                        break;
                    }
                }
            } else if (temp[1].indexOf('[') != -1) { // range value,put into Racos
                int firstIndex = temp[1].indexOf("[");
                int lastIndex = temp[1].indexOf("]");
                String[] bounds = temp[1].substring(firstIndex + 1, lastIndex).trim().split(",");
                double lowerbound = Double.parseDouble(bounds[0].trim());
                double upperbound = Double.parseDouble(bounds[1].trim());
                if (rangeParameters == null) rangeParameters = new ArrayList<>();
                rangeParameters.add(new RangeParameter(temp[0].trim(), lowerbound, upperbound));
            } else {
                initParameterValues.put(temp[0].trim(), Double.parseDouble(temp[1].trim()));
            }
        }
//        if(initLoc == -1){
//            System.out.println("Error ==> It is mandatory to set init loc.");
//            System.exit(-1);
//        }
    }

    public int getInitLoc() {
        return initLoc;
    }

    void DFS(Automata automata, int[] path, int depth, int maxPathSize) {
        if (depth + 1 == maxPathSize) {
            //System.out.println("The depth is " + maxPathSize);
            println("The depth is " + maxPathSize);
            for (int i = 0; i < path.length - 1; ++i) {
                //System.out.print(path[i] + "->");
                print(path[i] + "->");
            }
            //System.out.println(path[path.length - 1]);
            println(Integer.toString(path[path.length - 1]));
            runRacos(automata, path);
        } else {
            ArrayList<Integer> neibours = automata.locations.get(path[depth]).getNeibours();
            for (int i = 0; i < neibours.size(); ++i) {
                path[depth + 1] = neibours.get(i);
                DFS(automata, path, depth + 1, maxPathSize);
            }
        }
    }

    void DFS1(Automata automata, ArrayList<Integer> arrayListPath, int maxPathSize) {
        int len = arrayListPath.size();
        int path[] = new int[len];
        for (int i = 0; i < len; ++i)
            path[i] = arrayListPath.get(i);
        println("The depth is " + len);
        System.out.println("The depth is " + len);
        for (int i = 0; i < len - 1; ++i) {
            System.out.print(path[i] + "->");
            print(path[i] + "->");
        }
        System.out.println(path[len - 1]);
        println(Integer.toString(path[len - 1]));
        boolean pruning = runRacos(automata, path);
        if (pruning || len == maxPathSize)
            return;
        ArrayList<Integer> neibours = automata.locations.get(path[len - 1]).getNeibours();
        for (int i = 0; i < neibours.size(); ++i) {
            int nextPos = neibours.get(i);
            arrayListPath.add(neibours.get(i));
            DFS1(automata, arrayListPath, maxPathSize);
            arrayListPath.remove(arrayListPath.size() - 1);
        }

    }


    boolean runRacos(Automata automata, int[] path) {
        int samplesize = 1;       // parameter: the number of samples in each iteration
        int iteration = 50;       // parameter: the number of iterations for batch racos
        int budget = 2000;         // parameter: the budget of sampling for sequential racos
        int positivenum = 1;       // parameter: the number of positive instances in each iteration
        double probability = 0.95; // parameter: the probability of sampling from the model
        int uncertainbit = 1;      // parameter: the number of sampled dimensions
        Instance ins = null;
        int repeat = 1;
        Task t = new ObjectFunction(automata, path);
        ArrayList<Instance> result = new ArrayList<>();
        ArrayList<Instance> feasibleResult = new ArrayList<>();
        double feasibleResultAllTime = 0;
        boolean pruning = true;
        for (int i = 0; i < repeat; i++) {
            double currentT = System.currentTimeMillis();
            Continue con = new Continue(t, automata);
            con.setMaxIteration(iteration);
            con.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
            con.setBudget(budget);              // parameter: the budget of sampling
            con.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
            con.setRandProbability(probability);// parameter: the probability of sampling from the model
            con.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
            ValueArc valueArc = con.run();                          // call sequential Racos              // call Racos
//            ValueArc valueArc = con.RRT();                          // call sequential Racos              // call Racos
//            ValueArc valueArc = con.monte();                          // call sequential Racos              // call Racos
//            ValueArc valueArc = con.run2();
            double currentT2 = System.currentTimeMillis();
            ins = con.getOptimal();             // obtain optimal
            if (ins.getValue() < 0) {
                feasibleResult.add(ins);
                feasibleResultAllTime += (currentT2 - currentT) / 1000;
                pruning = false;
                if (minValueArc == null || minValueArc.value >= valueArc.value) {
                    minValueArc = valueArc;
                    minValueArc.path = path;
                }
            } else if (valueArc.penalty < 0) {
                pruning = false;
            }
            print("best function value:");
            print(ins.getValue() + "     ");
            result.add(ins);
            //System.out.print("[");
            print("[");
            for (int j = 0; j < ins.getFeature().length; ++j) {
                print(Double.toString(ins.getFeature(j)) + ",");
            }
            println("]");
        }

        for (int i = 0; i < result.size(); ++i) {
            //System.out.println(result.get(i).getValue());
            println(Double.toString(result.get(i).getValue()));
            //System.out.print("[");
            print("[");
            for (int j = 0; j < result.get(i).getFeature().length; ++j) {
                //System.out.print(result.get(i).getFeature(j) * ((ObjectFunction) t).delta+ ",");
                //print(Double.toString(result.get(i).getFeature(j) * ((ObjectFunction) t).delta) + ",");
                print(Double.toString(result.get(i).getFeature(j)) + ",");
            }
            //System.out.println("]");
            println("]");
        }
        println("Feasible Result:");
        for (int i = 0; i < feasibleResult.size(); ++i) {
            println(Double.toString(feasibleResult.get(i).getValue()));
            print("[");
            for (int j = 0; j < feasibleResult.get(i).getFeature().length; ++j) {
                print(Double.toString(feasibleResult.get(i).getFeature(j)) + ",");
            }
            println("]");
        }
        println("Average time : " + Double.toString(feasibleResultAllTime / feasibleResult.size()));
        System.out.println("Average time : " + Double.toString(feasibleResultAllTime / feasibleResult.size()));
        return pruning;
    }

    void checkAutomata() {
        println("Init loc is " + initLocName);
        println("Init loc is " + initLoc);
        for (Map.Entry<String, Double> entry : initParameterValues.entrySet()) {
            println("The init value of " + entry.getKey() + " is " + entry.getValue());
//            System.out.println("The init value of " + entry.getKey() + " is " + entry.getValue());
        }
        println("Forbidden loc is " + forbiddenLocName);
        println("Forbidden loc is " + forbiddenLoc);
        println("Forbidden constraints is ");
        assert (forbiddenConstraints instanceof String);
        if(obj_function == null){
            System.err.println("objective function cannot be empty");
            System.exit(-1);
        }
        for (Map.Entry<Integer, Location> entry : locations.entrySet()) {
            println(Integer.toString(entry.getKey()));
            entry.getValue().printLocation();
            println("**************");
        }

        for (int i = 0; i < transitions.size(); ++i) {
            transitions.get(i).printTransition();
        }
    }

    public HashMap<String, Double> duplicateInitParametersValues() {
        HashMap<String, Double> newMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : initParameterValues.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue());
        }
        return newMap;
    }

    public Transition getTransitionBySourceAndTarget(int source, int target) {
        for (int i = 0; i < transitions.size(); ++i) {
            if (transitions.get(i).source == source && transitions.get(i).target == target)
                return transitions.get(i);
        }
        return null;
    }

    public void println(String str) {
        try {
            bufferedWriter.write(str + "\n");
        } catch (IOException e) {
            System.out.println("write to file error!");
        }
    }

    public void print(String str) {
        try {
            bufferedWriter.write(str);
        } catch (IOException e) {
            System.out.println("write to file error!");
        }
    }

    public static String format(double value) {
        return String.format("%.5f", value);
    }

    public static void main(String[] args) {
        configUtil config = new configUtil();
        String prefix = new String("models/" + config.get("system") + "_" + config.get("mission"));
        String modelFile = prefix + ".xml";
        String cfgFile = prefix + ".cfg";

        double currentTime = System.currentTimeMillis();
        Automata automata = new Automata(modelFile, cfgFile);
        automata.output = new File("logs.txt");
        try {
            automata.bufferedWriter = new BufferedWriter(new FileWriter(automata.output));
            automata.checkAutomata();
            int maxPathSize = Integer.parseInt(config.get("bound"));

            File file = new File("result.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
//                   while(automata.initParameterValues.get("x") < automata.target_x && Math.abs(automata.target_x - automata.initParameterValues.get("x")) > 0.5){
//                   while(Math.pow(automata.target_x-automata.initParameterValues.get("x"),2) + Math.pow(automata.target_y-automata.initParameterValues.get("y"),2) > 2){
            double delta = 4.0;
            if(config.get("mission").contains("two_turn")) {
                delta = 16.0;
            }

            if (config.get("system").equals("vehicle")) {
                bufferedWriter.write("mode\t\tdwell time\t\tu1\t\tu2\n");
            } else if (config.get("system").equals("drone")) {
                bufferedWriter.write("mode\t\tdwell time\t\tT1\t\tT2\t\tT3\n");
            } else if(config.get("system").equals("train")){
                bufferedWriter.write("model\t\tdwell time\n");
            } else {
                System.out.println("Error: system '" + config.get("system") + "' not found!");
                System.exit(0);
            }
            while (Math.pow(automata.target_x - automata.initParameterValues.get("x"), 2) > delta) {
                double tmpTime = System.currentTimeMillis();
                if ((tmpTime - currentTime) / 1000 > config.getTimeLimit(modelFile)) {
                    System.out.println("Fail to synthesize feasible solutions: timeout!");
                    System.exit(0);
                }
//                   while(Math.abs(200 - automata.initParameterValues.get("x")) + Math.abs(200-automata.initParameterValues.get("y")) > 2){
                ArrayList<Integer> arrayListPath = new ArrayList<>();
                if (automata.getInitLoc() != -1) {
                    arrayListPath.add(automata.getInitLoc());
                    automata.DFS1(automata, arrayListPath, maxPathSize);
                } else {
                    for (Map.Entry<Integer, Location> entry : automata.locations.entrySet()) {
                        arrayListPath.clear();
                        arrayListPath.add(entry.getValue().getNo());
                        automata.DFS1(automata, arrayListPath, maxPathSize);
                    }
                }
                if(automata.minValueArc == null){
                    System.out.println("Error: automata.minValueArc is Null!");
                    System.exit(-1);
                }
                HashMap<String, Double> map = automata.minValueArc.allParametersValues;
                int index;
                for (index = 0; index < automata.minValueArc.args.length; ++index) {
                    if (automata.minValueArc.args[index] != 0) {
                        break;
                    }
                }
                if (index == automata.minValueArc.args.length)
                    continue;

                for (int i = 0; i < automata.minValueArc.path.length; ++i) {
                    bufferedWriter.write(automata.minValueArc.path[i] + "\t\t");
                    if (config.get("system").equals("vehicle")) {
                        if (automata.minValueArc.path[i] == 1) {
                            bufferedWriter.write(format(automata.minValueArc.args[i] * automata.delta) + " seconds\t\t" + format(map.get("u2")) + "\t\t" + format(0) + "\n");
                        } else {
                            bufferedWriter.write(format(automata.minValueArc.args[i] * automata.delta) + " seconds\t\t" + format(0) + "\t\t" + format(map.get("u1")) + "\n");
                        }
                    } else if (config.get("system").equals("drone")){
                        if (automata.minValueArc.path[i] == 1) {
                            bufferedWriter.write(format(automata.minValueArc.args[i] * automata.delta) + " seconds\t\t" + format(0) + "\t\t" + format(map.get("T12")) + "\t\t" + format(0) + "\n");
                        } else if (automata.minValueArc.path[i] == 2) {
                            bufferedWriter.write(format(automata.minValueArc.args[i] * automata.delta) + " seconds\t\t" + format(0) + "\t\t" + format(map.get("T22")) + "\t\t" + format(map.get("T23")) + "\n");
                        } else {
                            bufferedWriter.write(format(automata.minValueArc.args[i] * automata.delta) + " seconds\t\t" + format(map.get("T31")) + "\t\t" + format(map.get("T32")) + "\t\t" + format(0) + "\n");
                        }
                    }else if (config.get("system").equals("train")){
                        bufferedWriter.write(format(automata.minValueArc.args[i] * automata.delta) + " seconds\n");
                    }
                }

                bufferedWriter.write("current x is " + map.get("x") + "\n");

                if (map.containsKey("x"))
                    automata.initParameterValues.put("x", map.get("x"));
                if (map.containsKey("y"))
                    automata.initParameterValues.put("y", map.get("y"));
                if (map.containsKey("angle"))
                    automata.initParameterValues.put("angle", map.get("angle"));
                if (map.containsKey("angle_v"))
                    automata.initParameterValues.put("angle_v", map.get("angle_v"));
                if (map.containsKey("vx"))
                    automata.initParameterValues.put("vx", map.get("vx"));
                if (map.containsKey("vy"))
                    automata.initParameterValues.put("vy", map.get("vy"));
                if (map.containsKey("fuel"))
                    automata.initParameterValues.put("fuel", map.get("fuel"));
                if (map.containsKey("v"))
                    automata.initParameterValues.put("v", map.get("v"));
                if (map.containsKey("a"))
                    automata.initParameterValues.put("a", map.get("a"));
                automata.minValueArc = null;
            }
            double endTime = System.currentTimeMillis();
            bufferedWriter.write("\ntime:" + (endTime - currentTime) / 1000 + " seconds");
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

        double endTime = System.currentTimeMillis();
        System.out.println("Time cost :" + (endTime - currentTime) / 1000 + " seconds");

    }
}
