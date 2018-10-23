package Racos.ObjectiveFunction;

import MPC.Transition;
import Racos.Componet.Dimension;
import Racos.Componet.Instance;
import Racos.Componet.*;
import Racos.Method.*;
import MPC.Automata;
import MPC.Location;

import com.greenpineyu.fel.*;
import com.greenpineyu.fel.context.FelContext;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ObjectFunction implements Task{
    private Dimension dim;
    private Automata automata;
    private int []path;
    private FelEngine fel;
    private FelContext ctx;
    private ArrayList<HashMap<String,Double>> allParametersValues;
    public double delta = 0.05;
    private int c = 0;
    private double p1 = 0;
    private double p2 = 0;
    private double p3 = 0;
    private double p4 = 0;
    private boolean flag = false;
    private double penalty = 0;
    private boolean sat = true;

    public ObjectFunction(Automata automata,int []path){
        this.automata = automata;
        dim = new Dimension();
        dim.setSize(path.length);
        for(int i = 0;i < path.length;++i)
            dim.setDimension(i,0,automata.cycle / delta,false);
        this.path = path;
        fel = new FelEngineImpl();
        ctx = fel.getContext();
    }

    public boolean checkConstraints(double []args,HashMap<String,Double> parametersValues){
        double pen = penalty;
        boolean result = (path[path.length - 1] == automata.forbiddenLoc || automata.forbiddenLoc == -1);
        if(!result)  {
            return true;
        }
        for(Map.Entry<String,Double> entry : parametersValues.entrySet()){
            //System.out.println(allParametersValues.size());
            ctx.set(entry.getKey(),entry.getValue());
        }
        result = (boolean)fel.eval(automata.cycleConstraint);
        if(result) {
            sat = false;
            penalty += computePenalty(automata.cycleConstraint,true);
            return false;
        }
        //System.out.println(allParametersValues.get(allParametersValues.size() - 1).get("t"));
        if(automata.forbiddenConstraints.size() == 0)
            return true;
        for(int i = 0;i < automata.forbiddenConstraints.size();++i){
            result = (boolean)fel.eval(automata.forbiddenConstraints.get(i));
            //System.out.println(automata.forbiddenConstraints.get(i));
            if(!result){
                String constraint = automata.forbiddenConstraints.get(i);
                //p3 = computePenalty(constraint);
                //System.out.println(automata.forbiddenConstraints.get(i));
                //System.out.println(allParametersValues.get(allParametersValues.size() - 1).get("x"));
                //sat = false;
                //penalty += computePenalty(constraint);
                penalty = pen;
                return true;
            }
            else{
                penalty += computePenalty(automata.forbiddenConstraints.get(i),true);
            }
        }
//        for(int i = 0;i < args.length;++i){
//            System.out.println(args[i]);
//        }
        sat = false;
        return false;
    }

    public HashMap<String,Double> computeValuesByFlow(HashMap<String,Double> parametersValues,Location location,double arg){
        HashMap<String,Double> tempMap = new HashMap<>();
        for(HashMap.Entry<String,Double> entry : parametersValues.entrySet()){
            ctx.set(entry.getKey(),entry.getValue());
            //System.out.println(entry.getKey() + " " + entry.getValue());
        }
        for(HashMap.Entry<String,Double> entry : parametersValues.entrySet()){
            if(location.flows.containsKey(entry.getKey())){
                Object obj = fel.eval(location.flows.get(entry.getKey()));
                double result;
                if(obj instanceof Double)
                    result = (double)obj;
                else if(obj instanceof Integer) {
                    result = (int) obj;
                    //System.out.println(entry.getKey() + " " + entry.getValue());
                }
                else {
                    result = 0;
                    System.out.println("Not Double and Not Integer!");
                }
                double delta = result * arg;
                //System.out.println(delta);
                tempMap.put(entry.getKey(),entry.getValue() + delta);
            }
            else {
                //System.out.println("???");
                tempMap.put(entry.getKey(),entry.getValue());
            }
        }
        return tempMap;

//        double t = parametersValues.get("t") + arg;
//        tempMap.put("t",t);
//        double a;
//        if(location.getNo() == 2)   a = 5;
//        else if(location.getNo() == 3)  a = 0 + (Math.random() - 0.5) * 10;
//        //else if(location.getNo() == 3)  a = -10;
//        else if(location.getNo() == 4)  a = -10;
//        else a = 0;
//        tempMap.put("a",a);
//        double v = parametersValues.get("v") + a * arg;
//        tempMap.put("v",v);
//        double x = parametersValues.get("x") + parametersValues.get("v") * arg + 0.5 * a * arg * arg;
//        tempMap.put("x",x);
//        double vebi = Math.sqrt(2 * 10 * (200 - x));
//        tempMap.put("vebi",vebi);
    }

    public boolean checkGuards(double []args){
        // System.out.println("???");
        HashMap<String,Double> parameterValues;
        for(int i = 0;i < path.length;++i){
            Location location = automata.locations.get(path[i]);
            //System.out.println(location.getNo());
            parameterValues = allParametersValues.get(i);
            int target;
            if(i + 1 < path.length){
                target = path[i + 1];
                int source = path[i];
                for(int k = 0;k < automata.transitions.size();++k){
                    Transition transition = automata.transitions.get(k);
                    if(transition.source == source && transition.target == target){
                        //System.out.println(parameterValues.get("v"));
                        //System.out.println(parameterValues.get("vebi"));
                        for(Map.Entry<String,Double> entry : parameterValues.entrySet()){
                            //System.out.println();
                            ctx.set(entry.getKey(),entry.getValue());
                            //ctx.set("vebi",0);
                        }
                        for(int guardIndex = 0;guardIndex < transition.guards.size();++guardIndex){
                            //System.out.println(transition.guards.get(guardIndex));
                            boolean result = (boolean)fel.eval(transition.guards.get(guardIndex));
                            if(!result) {
                                String guard = transition.guards.get(guardIndex);
//                                if(flag)
//                                    p4 += computePenalty(guard);
                                //p4 = computePenalty(guard);
                                sat = false;
                                penalty += computePenalty(guard,false);
                                //System.out.println("p4 : " + p4);
                                //return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public boolean checkInvarientsByODE(double []args){
        //System.out.println("here");
        double end = 0;
        HashMap<String,Double> newMap = new HashMap<>();
        for(int locIndex = 0;locIndex < path.length;++locIndex){
            end = args[locIndex];
            double step = 0;
            if(locIndex == 0)
                newMap = automata.duplicateInitParametersValues();
            else {
                newMap = allParametersValues.get(locIndex - 1);
                //check assignments
                Transition transition = automata.getTransitionBySourceAndTarget(path[locIndex - 1],path[locIndex]);
                if(transition == null){
                    System.out.println("Found no transition");
                    System.exit(-1);
                }
                for(HashMap.Entry<String,String> entry : transition.assignments.entrySet()){
                    Object obj = fel.eval(entry.getValue());
                    double result = 0;
                    if(obj instanceof Integer)  result = (int)obj;
                    else if(obj instanceof Double) result = (double)obj;
                    else{
                        System.out.println("Not Double and Not Integer!");
                    }
                    newMap.put(entry.getKey(),result);
                }
            }
            while(step < end){
                newMap = computeValuesByFlow(newMap,automata.locations.get(path[locIndex]),delta);
                //checkConstraints(args,newMap);
                for(HashMap.Entry<String,Double> entry : newMap.entrySet()){
                    ctx.set(entry.getKey(),entry.getValue());
                }
                for(int i = 0;i < automata.locations.get(path[locIndex]).invariants.size();++i){
                    boolean result = (boolean)fel.eval(automata.locations.get(path[locIndex]).invariants.get((i)));
                    if(!result) {
                        String invariant = automata.locations.get(path[locIndex]).invariants.get(i);
                        //p2 = computePenalty(invariant);
                        if(computePenalty(invariant,false) < 0.1)
                            continue;
                        //p2 = computePenalty(invariant);
                        //p2 = end - step;
                        //System.out.println(p2);
                        //System.out.println(invariant);
                        sat = false;
                        penalty += computePenalty(invariant,false);
                        //System.out.println(penalty);
                        //return false;
                    }
                }
                step += 1;
            }
            allParametersValues.add(newMap);
        }
        return true;
    }

    private double computePenalty(String expression,boolean isConstraint){
        String []strings;
        String bigPart = "",smallPart = "";
        if(expression.indexOf("<=") != -1){
            strings = expression.split("<=");
            bigPart = strings[0].trim();
            smallPart = strings[1].trim();
        }
        else if(expression.indexOf("<") != -1){
            strings = expression.split("<");
            bigPart = strings[0].trim();
            smallPart = strings[1].trim();
        }
        else if(expression.indexOf(">=") != -1){
            strings = expression.split(">=");
            bigPart = strings[1].trim();
            smallPart = strings[0].trim();
        }
        else if(expression.indexOf(">") != -1){
            strings = expression.split(">");
            bigPart = strings[1].trim();
            smallPart = strings[0].trim();
        }
        Object obj1 = fel.eval(bigPart);
        Object obj2 = fel.eval(smallPart);
        double big = 0,small = 0;
        if(obj1 instanceof Double)
            big = (double)obj1;
        else if(obj1 instanceof Integer) {
            big = (int) obj1;
            //System.out.println(entry.getKey() + " " + entry.getValue());
        }
        else {
            obj1 = 0;
            System.out.println("Not Double and Not Integer!");
        }
        if(obj2 instanceof Double)
            small = (double)obj2;
        else if(obj2 instanceof Integer) {
            small = (int) obj2;
            //System.out.println(entry.getKey() + " " + entry.getValue());
        }
        else {
            small = 0;
            System.out.println("Not Double and Not Integer!");
        }
        //System.out.println(big);
        //System.out.println(small);
        double penal;
        if(isConstraint)
            penal = small - big;
        else penal = big - small;
        //System.out.println(penal);
        if(penal < 0) {
            System.out.println(expression);
            System.out.println(small);
        }
        return penal;
    }

    public boolean checkInvarientsByRacos(double []args){
        int samplesize = 30;       // parameter: the number of samples in each iteration
        int iteration = 100;       // parameter: the number of iterations for batch racos
        int budget = 2000;         // parameter: the budget of sampling for sequential racos
        int positivenum = 1;       // parameter: the number of positive instances in each iteration
        double probability = 0.95; // parameter: the probability of sampling from the model
        int uncertainbit = 1;      // parameter: the number of sampled dimensions
        Instance ins = null;
        int repeat = 15;
        allParametersValues.add(0,automata.duplicateInitParametersValues());
        allParametersValues.remove(allParametersValues.size() - 1);
        Task t = new InvarientsObjectFunction(automata,path,args,allParametersValues);
        for (int i = 0; i < repeat; i++) {
            Continue conti = new Continue(t);
            //conti.TurnOnSequentialRacos();
            conti.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
            conti.setBudget(budget);              // parameter: the budget of sampling
            conti.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
            conti.setRandProbability(probability);// parameter: the probability of sampling from the model
            conti.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
            conti.run();                          // call sequential Racos
            ins = conti.getOptimal();             // obtain optimal
            if(ins.getValue() == Double.MIN_VALUE)
                System.out.print("Invarient Check failed");
            return false;
        }
        return true;
    }

    public boolean checkCycle(double []args){
        double sum = 0;
        for(int i = 0;i < args.length;++i){
            sum += args[i];
        }
        if(sum > automata.cycle / delta) {
            //p1 = sum - automata.cycle / delta;
            sat = false;
            penalty += sum - automata.cycle / delta;
            return false;
        }
        return true;
    }
    @Override
    public double getValue(Instance ins) {
        penalty = 0;
        sat = true;
        allParametersValues = new ArrayList<>();
        double []args = new double[ins.getFeature().length];
        for(int i = 0;i < args.length;++i){
            args[i] = ins.getFeature(i);
//            if(args[i] >= 4000)
            //System.out.println(args[i] + " ");
        }
        //System.out.println("");
//        if(!checkCycle(args)){
//            //System.out.println("not");
//            return 100000;
//        }
//        //System.out.println("1");
//        if(!checkInvarientsByODE(args)) {
//            //System.out.println("1");
//            return 10000;
//        }
//        //System.out.println("2");
//        if(!checkConstraints(args)) {
//            //System.out.println("2");
//            return 1000;
//        }
//        //System.out.println("3");
//        if(!checkGuards(args)) {
//            //System.out.println("3");
//            //System.out.println(p4);
//            return 100;
//        }
        checkCycle(args);
        if(!sat) {
            System.out.println("over");
            return penalty;
        }
        checkInvarientsByODE(args);
        checkGuards(args);
        if(!sat) {
            return penalty;
        }
        System.out.println("what");
        return computeValue(args);
    }

    public double computeValue(double []args){

        HashMap<String,Double> map = allParametersValues.get(allParametersValues.size() - 1);
        return -map.get("t");
//        double sum = 0;
//        for(int i = 0;i < args.length;++i)
//            sum += args[i];
//        return  -sum;
    }

    @Override
    public Dimension getDim() {
        return dim;
    }
}