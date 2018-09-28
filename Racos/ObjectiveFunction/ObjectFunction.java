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

    public ObjectFunction(Automata automata,int []path){
        this.automata = automata;
        dim = new Dimension();
        dim.setSize(path.length);
        for(int i = 0;i < path.length;++i)
            dim.setDimension(i,0.0,automata.cycle,true);
        this.path = path;
        fel = new FelEngineImpl();
        ctx = fel.getContext();
    }

    public boolean checkConstraints(double []args){
        boolean result = (path[path.length - 1] == automata.forbiddenLoc || automata.forbiddenLoc == -1);
        if(!result)  return true;
        for(Map.Entry<String,Double> entry : allParametersValues.get(allParametersValues.size() - 1).entrySet()){
            //System.out.println(allParametersValues.size());
            ctx.set(entry.getKey(),entry.getValue());
        }
        result = (boolean)fel.eval(automata.cycleConstraint);
        if(result) return false;
        //System.out.println(allParametersValues.get(allParametersValues.size() - 1).get("t"));
        for(int i = 0;i < automata.forbiddenConstraints.size();++i){
            result = (boolean)fel.eval(automata.forbiddenConstraints.get(i));
            //System.out.println(automata.forbiddenConstraints.get(i));
            if(!result){
                //System.out.println(automata.forbiddenConstraints.get(i));
                //System.out.println(allParametersValues.get(allParametersValues.size() - 1).get("x"));
                return true;
            }
        }
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
                            if(!result) return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public boolean checkInvarientsByODE(double []args){
        //System.out.println("here");
        double start = 0,end = 0;
        double delta = 0.05;
        HashMap<String,Double> newMap = new HashMap<>();
        for(int locIndex = 0;locIndex < path.length;++locIndex){
            start = end;
            end = args[locIndex];
            double tempT = 0;
            if(locIndex == 0)
                newMap = automata.duplicateInitParametersValues();
            else {
                //System.out.println(allParametersValues.get(locIndex - 1).get("t"));
                newMap = allParametersValues.get(locIndex - 1);
            }
            int no = automata.locations.get(path[locIndex]).getNo();
            if(no == 2){
                newMap.put("a",5.0);
            }
            else if(no == 3){
                newMap.put("a",0 + (Math.random() - 0.5) * 10);
            }
            else newMap.put("a",-10.0);
            while(tempT <= end - start){
                newMap = computeValuesByFlow(newMap,automata.locations.get(path[locIndex]),delta);
                for(HashMap.Entry<String,Double> entry : newMap.entrySet()){
                    ctx.set(entry.getKey(),entry.getValue());
                }
                for(int i = 0;i < automata.locations.get(path[locIndex]).invariants.size();++i){
                    boolean result = (boolean)fel.eval(automata.locations.get(path[locIndex]).invariants.get((i)));
                    if(!result) return false;
                }
                tempT += delta;
                //newMap.put("t",newMap.get("t") + delta);
            }
            if(newMap.get("t") > 5.0){
                System.out.println("?????");
            }
            //System.out.println(newMap.get("t"));
            allParametersValues.add(newMap);
        }
        return true;
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
            conti.TurnOnSequentialRacos();
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
            if(sum > automata.cycle)
                return false;
        }
        return true;
    }
    @Override
    public double getValue(Instance ins) {
        allParametersValues = new ArrayList<>();
        double []args = new double[ins.getFeature().length];
        for(int i = 0;i < args.length;++i){
            args[i] = ins.getFeature(i);
        }
//        if(!checkCycle(args)){
//            return Double.MAX_VALUE;
//        }
        if(!checkInvarientsByODE(args)) {
            //System.out.println("3");
            return Double.MAX_VALUE;
        }
//        for(int i = 0;i < allParametersValues.size();++i){
//            for(HashMap.Entry<String,Double> entry : allParametersValues.get(i).entrySet()){
//                System.out.println(entry.getKey() + " " + entry.getValue());
//            }
//        }
        if(!checkConstraints(args)) {
            System.out.println("2");
            return Double.MAX_VALUE;
        }
        if(!checkGuards(args)) {
            //System.out.println("1");
            return Double.MAX_VALUE;
        }

        //System.out.println(allParametersValues.size());
//        double sum = 0;
//        for(int i = 0;i < args.length;++i)
//            sum += args[i];
//        if(Math.abs(sum - allParametersValues.get(allParametersValues.size() - 1).get("t")) > 0.5){
//            System.out.println("what");
//            System.out.println(sum);
//            System.out.println(allParametersValues.get(allParametersValues.size() - 1).get("t"));
//            System.out.println(args[0]);
//            System.out.println(args[1]);
//        }
//        if(allParametersValues.get(allParametersValues.size() - 1).get("t") > 5.0){
//            System.out.println("no");
//        }
        //if(!checkInvarientsByRacos(args))   return Double.MAX_VALUE;
        return computeValue(args);
    }

    public double computeValue(double []args){
        return 200 - allParametersValues.get(allParametersValues.size() - 1).get("x");
    }

    @Override
    public Dimension getDim() {
        return dim;
    }
}
