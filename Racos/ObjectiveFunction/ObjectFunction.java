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
    private ArrayList<HashMap<String,Double>> allParametersValues;

    public ObjectFunction(Automata automata,int []path){
        this.automata = automata;
        dim = new Dimension();
        dim.setSize(path.length);
        for(int i = 0;i < path.length;++i)
            dim.setDimension(i,0.0,2.5,true);
        this.path = path;
    }

    public boolean checkConstraints(double []args){
        boolean result = (path[path.length - 1] == automata.forbiddenLoc);
        if(!result)  return true;
        fel = new FelEngineImpl();
        FelContext ctx = fel.getContext();
        for(Map.Entry<String,Double> entry : allParametersValues.get(allParametersValues.size() - 1).entrySet()){
            //System.out.println(allParametersValues.size());
            ctx.set(entry.getKey(),entry.getValue());
        }
        for(int i = 0;i < automata.forbiddenConstraints.size();++i){
            result = (boolean)fel.eval(automata.forbiddenConstraints.get(i));
            if(!result){
                //System.out.println(automata.forbiddenConstraints.get(i));
                return true;
            }
        }
        return false;
    }

    public HashMap<String,Double> computeValuesByFlow(HashMap<String,Double> parametersValues,Location location,double arg){
        HashMap<String,Double> tempMap = new HashMap<>();
        double t = parametersValues.get("t") + arg;
        tempMap.put("t",t);
        double a;
        if(location.getNo() == 2)   a = 5;
        else if(location.getNo() == 3)  a = 0 + (Math.random() - 0.5) * 10;
        //else if(location.getNo() == 3)  a = -10;
        else if(location.getNo() == 4)  a = -10;
        else a = 0;
        tempMap.put("a",a);
        double v = parametersValues.get("v") + a * arg;
        tempMap.put("v",v);
        double x = parametersValues.get("x") + parametersValues.get("v") * arg + 0.5 * a * arg * arg;
        tempMap.put("x",x);
        double vebi = Math.sqrt(2 * 10 * (200 - x));
        tempMap.put("vebi",vebi);
        return tempMap;
    }

    public boolean checkGuards(double []args){
        // System.out.println("???");
        HashMap<String,Double> parameterValues = automata.duplicateInitParametersValues();
        for(int i = 0;i < path.length;++i){
            Location location = automata.locations.get(path[i]);
            //System.out.println(location.getNo());
            parameterValues = computeValuesByFlow(parameterValues,location,args[i]);
            allParametersValues.add(parameterValues);
            int target;
            if(i + 1 < path.length){
                target = path[i + 1];
                int source = path[i];
                for(int k = 0;k < automata.transitions.size();++k){
                    Transition transition = automata.transitions.get(k);
                    if(transition.source == source && transition.target == target){
                        fel = new FelEngineImpl();
                        FelContext ctx = fel.getContext();
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
        double delta = 0.5;
        for(int locIndex = 0;locIndex < path.length;++locIndex){
            start = end;
            end = args[locIndex];
            double tempT = 0;
            HashMap<String,Double> tempMap;
            if(locIndex == 0)
                tempMap = automata.duplicateInitParametersValues();
            else tempMap = allParametersValues.get(locIndex - 1);
            while(tempT <= end - start){
                HashMap<String,Double> newMap = computeValuesByFlow(tempMap,automata.locations.get(path[locIndex]),tempT);
                fel = new FelEngineImpl();
                FelContext ctx = fel.getContext();
                for(HashMap.Entry<String,Double> entry : newMap.entrySet()){
                    ctx.set(entry.getKey(),entry.getValue());
                }
                for(int i = 0;i < automata.locations.get(path[locIndex]).invariants.size();++i){
                    boolean result = (boolean)fel.eval(automata.locations.get(path[locIndex]).invariants.get((i)));
                    if(!result) return false;
                }
                tempT += delta;
            }
        }
        return true;
    }

    public boolean checkInvarientsByRacos(double []args){
        int samplesize = 30;       // parameter: the number of samples in each iteration
        int iteration = 1000;       // parameter: the number of iterations for batch racos
        int budget = 2000;         // parameter: the budget of sampling for sequential racos
        int positivenum = 1;       // parameter: the number of positive instances in each iteration
        double probability = 0.95; // parameter: the probability of sampling from the model
        int uncertainbit = 1;      // parameter: the number of sampled dimensions
        Instance ins = null;
        int repeat = 15;
        Task t = new InvarientsObjectFunction(automata,path,args,allParametersValues);

        return true;
    }

    @Override
    public double getValue(Instance ins) {
        allParametersValues = new ArrayList<>();
        double []args = new double[ins.getFeature().length];
        for(int i = 0;i < args.length;++i){
            args[i] = ins.getFeature(i);
        }
        if(!checkGuards(args)) return Double.MAX_VALUE;
        if(!checkConstraints(args)) return Double.MAX_VALUE;
        if(!checkInvarientsByODE(args))  return Double.MAX_VALUE;
        return 0;
    }

    @Override
    public Dimension getDim() {
        return dim;
    }
}
