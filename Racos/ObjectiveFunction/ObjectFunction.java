package Racos.ObjectiveFunction;

import MPC.Transition;
import Racos.Componet.Dimension;
import Racos.Componet.Instance;
import Racos.Componet.*;
import Racos.Method.*;
import MPC.Automata;
import MPC.Location;
import Racos.Tools.ValueArc;
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
    public double delta;
    private double penalty = 0;
    private double globalPenalty = 0;
    private boolean sat = true;
    private double cerr = 0.01;
    int rangeParaSize;

    public ValueArc valueArc;

    public ObjectFunction(Automata automata,int []path){
        this.automata = automata;
        dim = new Dimension();
        delta = automata.delta;
        rangeParaSize = (automata.rangeParameters == null) ? 0 : automata.rangeParameters.size();
        dim.setSize(path.length + rangeParaSize);
        for(int i = 0;i < path.length;++i)
            dim.setDimension(i,0,automata.cycle / delta,false);
        for(int i = 0;i < rangeParaSize;++i){
            dim.setDimension(path.length + i,automata.rangeParameters.get(i).lowerBound,automata.rangeParameters.get(i).upperBound,true);
        }
        this.path = path;
        fel = new FelEngineImpl();
        ctx = fel.getContext();
        valueArc = new ValueArc();
    }

    public int getPathLength(){
        return this.path.length;
    }

    public boolean checkConstraints(double []args,HashMap<String,Double> parametersValues){
        for(Map.Entry<String,Double> entry : parametersValues.entrySet()){
            //System.out.println(allParametersValues.size());
            ctx.set(entry.getKey(),entry.getValue());
            //System.out.println(entry.getKey() + "  "  + entry.getValue());
        }
        //System.out.println(automata.forbiddenConstraints);
        if(automata.forbiddenConstraints==null)
            return true;
        boolean result = (boolean)fel.eval(automata.forbiddenConstraints);
//        computeConstraintValue(automata.forbiddenConstraints);
        if(!result) return true;
        //System.out.println(result);
        sat = false;
        globalPenalty += computeConstraintValue(automata.forbiddenConstraints);
        return false;
    }

    public double computeConstraintValue(String constraint){
        int firstRightBracket = constraint.trim().indexOf(")");
        if(firstRightBracket != -1 && constraint.indexOf('&') == -1 && constraint.indexOf('|') == -1)
            return computePenalty(constraint.substring(constraint.indexOf('(')+1,constraint.lastIndexOf(")")),false);
        if(firstRightBracket != -1 && firstRightBracket != constraint.length()-1){
            for(int i = firstRightBracket;i < constraint.length();++i){
                if(constraint.charAt(i) == '&'){
                    int index = 0;
                    int numOfBrackets = 0;
                    int partBegin = 0;
                    double pen = 0;
                    while(index < constraint.length()){
                        if(constraint.indexOf(index) == '(')
                            ++numOfBrackets;
                        else if(constraint.indexOf(index) == ')')
                            --numOfBrackets;
                        else if(constraint.indexOf(index) == '&' && numOfBrackets==0){
                            String temp = constraint.substring(partBegin,index);
                            boolean result = (boolean)fel.eval(temp);
                            if(!result) return 0;
                            else pen+= computeConstraintValue(temp);
                            index = index + 2;
                            partBegin = index;
                            constraint = constraint.substring(index);
                            continue;
                        }
                        ++index;
                    }
                    return pen;
//                    int index = firstRightBracket;
//                    double pen = 0;
//                    while(index != -1){
//                        String temp = constraint.substring(constraint.indexOf('(')+1,index);
//                        boolean result = (boolean)fel.eval(temp);
//                        if(!result) return 0;
//                        else pen += computeConstraintValue(temp);
//                        constraint = constraint.substring(index+1);
//                        index = constraint.indexOf(')');
//                    }
//                    return pen;
                }
                else if(constraint.charAt(i) == '|'){
                    int index = 0;
                    int numOfBrackets = 0;
                    int partBegin = 0;
                    double minPen = Double.MAX_VALUE;
                    while(index < constraint.length()){
                        if(constraint.indexOf(index) == '(')
                            ++numOfBrackets;
                        else if(constraint.indexOf(index) == ')')
                            --numOfBrackets;
                        else if(constraint.indexOf(index) == '|' && numOfBrackets==0){
                            String temp = constraint.substring(partBegin,index);
                            boolean result = (boolean)fel.eval(temp);
                            if(result){
                                minPen = (computeConstraintValue(temp) < minPen) ? computeConstraintValue(temp) : minPen;
                            }
                            index = index + 2;
                            partBegin = index;
                            constraint = constraint.substring(index);
                            continue;
                        }
                        ++index;
                    }
                    return minPen;
//                    int index = firstRightBracket;
//                    double minPen = Double.MAX_VALUE;
//                    while(index != -1){
//                        String temp =  constraint.substring(constraint.indexOf('(')+1,index);
//                        boolean result = (boolean)fel.eval(temp);
//                        if(result){
//                            minPen = (computeConstraintValue(temp) < minPen) ? computeConstraintValue(temp) : minPen;
//                        }
//                        constraint = constraint.substring(index + 1);
//                        index = constraint.indexOf(')');
//                    }
//                    return minPen;
                }
            }
        }
        else{
            if(firstRightBracket != -1){
                constraint = constraint.substring(constraint.indexOf('(')+1,firstRightBracket);
            }
            if(constraint.indexOf('&') != -1){
                String []strings = constraint.split("&");
                double pen = 0;
                for(int i = 0;i < strings.length;++i){
                    if(strings[i].equals("")) continue;
                    boolean result = (boolean)fel.eval(strings[i]);
                    if(!result) return 0;
                    else pen += computeConstraintValue(strings[i]);
                }
                return pen;
            }
            else if(constraint.indexOf('|') != -1){
                String []strings = constraint.split("\\|");
                double minPen = Double.MAX_VALUE;
                for(int i = 0;i < strings.length;++i){
                    if(strings[i].equals("")) continue;
                    boolean result = (boolean) fel.eval(strings[i]);
                    if(!result) continue;
                    else minPen = (computeConstraintValue(strings[i]) < minPen) ? computeConstraintValue(strings[i]) : minPen;
                }
                return minPen;
            }
            else return computePenalty(constraint,false);
        }
        return 0;
    }
//    public boolean checkConstraints(double []args,HashMap<String,Double> parametersValues){
//        double pen = globalPenalty;
//        boolean satOrigin = sat;
//        boolean result = (path[path.length - 1] == automata.forbiddenLoc || automata.forbiddenLoc == -1);
//        if(!result)  {
//            return true;
//        }
//        for(Map.Entry<String,Double> entry : parametersValues.entrySet()){
//            //System.out.println(allParametersValues.size());
//            ctx.set(entry.getKey(),entry.getValue());
//        }
//        result = (boolean)fel.eval(automata.cycleConstraint);
//        if(result && computePenalty(automata.cycleConstraint,true) > cerr) {
//            sat = false;
//            globalPenalty += computePenalty(automata.cycleConstraint,true);
//            return false;
//        }
//        //System.out.println(allParametersValues.get(allParametersValues.size() - 1).get("t"));
//        if(automata.forbiddenConstraints.size() == 0)
//            return true;
//        for(int i = 0;i < automata.forbiddenConstraints.size();++i){
//            result = (boolean)fel.eval(automata.forbiddenConstraints.get(i));
//            //System.out.println(automata.forbiddenConstraints.get(i));
//            if(!result || (result && computePenalty(automata.forbiddenConstraints.get(i),true) < cerr)){
//                String constraint = automata.forbiddenConstraints.get(i);
//                globalPenalty = pen;
//                if(sat != satOrigin)
//                    sat = satOrigin;
//                return true;
//            }
//            else{
//                globalPenalty += computePenalty(automata.forbiddenConstraints.get(i),true);
//                sat = false;
//            }
//        }
//        sat = false;
//        return false;
//    }

    public HashMap<String,Double> computeValuesByFlow(HashMap<String,Double> parametersValues,Location location,double arg){
        HashMap<String,Double> tempMap = new HashMap<>();
        for(HashMap.Entry<String,Double> entry : parametersValues.entrySet()){
            ctx.set(entry.getKey(),entry.getValue());
            //System.out.println(entry.getKey() + " " + entry.getValue());
        }
        for(HashMap.Entry<String,Double> entry : parametersValues.entrySet()){
            if(location.flows.containsKey(entry.getKey())){
                //System.out.println(location.flows.get(entry.getKey()));
                Object obj = fel.eval(location.flows.get(entry.getKey()));
                double result;
                if(obj instanceof Double)
                    result = (double)obj;
                else if(obj instanceof Integer) {
                    result = (int) obj;
                    //System.out.println(entry.getKey() + " " + entry.getValue());
                }
                else if(obj instanceof Long){
                    result = ((Long)obj).doubleValue();
                }
                else {
                    result = 0;
                    System.out.println("Not Double and Not Integer!");
                    System.out.println(obj.getClass().getName());
                    System.out.println(obj);
                    System.out.println(location.flows.get(entry.getKey()));
                    System.exit(0);
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
                                if(Double.isNaN(computePenalty(guard,false))){
                                    sat = false;
                                    penalty += 100000;
                                }
                                else if(computePenalty(guard,false) > cerr){
                                    sat = false;
                                    penalty += computePenalty(guard, false);
                                }
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

    public HashMap<String,Double> computeValuesByIntegral(HashMap<String,Double> parametersValues,Location location,double arg){
        HashMap<String,Double> tempMap = new HashMap<>();
        double t = parametersValues.get("t") + arg;
        tempMap.put("t",t);
        double a;
        if(location.getNo() == 2)   a = 5;
        else if(location.getNo() == 3)  a = 0;
        else if(location.getNo() == 4)  a = -10;
        else a = 0;
        tempMap.put("a",a);
        double v = parametersValues.get("v") + a * arg;
        tempMap.put("v",v);
        double fuel = parametersValues.get("fuel") + (parametersValues.get("v") + v) / 2 / 19 * 7;
        tempMap.put("fuel",fuel);
        double x = parametersValues.get("x") + parametersValues.get("v") * arg + 0.5 * a * arg * arg;
        tempMap.put("x",x);
        double vebi = Math.sqrt(2 * 10 * (parametersValues.get("MA") - x));
        if(Double.isNaN(vebi)){
            sat = false;
            tempMap.put("vebi",-1.0);
        }
        else tempMap.put("vebi",vebi);
        tempMap.put("MA",parametersValues.get("MA"));
        return tempMap;
    }

    public void computeParameterValues(double []args){
        HashMap<String,Double> newMap = new HashMap<>();
        for(int locIndex = 0;locIndex < path.length;++locIndex){
            if(locIndex == 0)
                newMap = automata.duplicateInitParametersValues();
            else{
                newMap = (HashMap<String, Double>) allParametersValues.get(locIndex - 1).clone();
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
                        System.out.println(entry.getValue());
                        System.exit(0);
                    }
                    newMap.put(entry.getKey(),result);
                }
            }
            newMap = computeValuesByIntegral(newMap,automata.locations.get(path[locIndex]),args[locIndex]);
            checkConstraints(args,newMap);
            for(HashMap.Entry<String,Double> entry : newMap.entrySet()){
                ctx.set(entry.getKey(),entry.getValue());
            }
            for(int i = 0;i < automata.locations.get(path[locIndex]).invariants.size();++i){
                boolean result = (boolean)fel.eval(automata.locations.get(path[locIndex]).invariants.get((i)));
                if(!result) {
                    String invariant = automata.locations.get(path[locIndex]).invariants.get(i);
                    if(computePenalty(invariant,false) < cerr)
                        continue;
                    //p2 = computePenalty(invariant);
                    //p2 = end - step;
                    //System.out.println(p2);
                    //System.out.println(invariant);
                    if(Double.isNaN(computePenalty(invariant,false))){
                        sat = false;
                        penalty += 100000;
                    }
                    else {
                        sat = false;
                        penalty += computePenalty(invariant, false);
                    }
                    //System.out.println(penalty);
                    //return false;
                }
            }
            allParametersValues.add(newMap);
        }
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
                newMap = (HashMap<String, Double>) allParametersValues.get(locIndex - 1).clone();
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
                        System.out.println(entry.getValue());
                        System.exit(0);
                    }
                    newMap.put(entry.getKey(),result);
                    //allParametersValues.get(locIndex - 1).put(entry.getKey(),result);
                }
            }
            if(end==0){
                checkConstraints(args,newMap);
            }
            while(step < end){
                newMap = computeValuesByFlow(newMap,automata.locations.get(path[locIndex]),delta);
                for(HashMap.Entry<String,Double> entry : newMap.entrySet()){
                    ctx.set(entry.getKey(),entry.getValue());
                }
                checkConstraints(args,newMap);
                for(int i = 0;i < automata.locations.get(path[locIndex]).invariants.size();++i){
                    boolean result = (boolean)fel.eval(automata.locations.get(path[locIndex]).invariants.get((i)));
                    if(!result) {
                        String invariant = automata.locations.get(path[locIndex]).invariants.get(i);
                        if(computePenalty(invariant,false) < cerr)
                            continue;
                        if(Double.isNaN(computePenalty(invariant,false))){
                            sat = false;
                            penalty += 100000;
                        }
                        else {
                            sat = false;
                            //System.out.println(invariant);
                            penalty += computePenalty(invariant, false);
                        }
                    }
                }
                step += 1;
            }
            allParametersValues.add(newMap);
        }
        return true;
    }

    public void updateInstanceRegion(Instance ins){
//        System.out.println("updateRegion");
        double args[] = new double[path.length];
        for(int i = 0;i < path.length;++i)
            args[i] = ins.getFeature(i);
        HashMap<String,Double> initParameter = automata.duplicateInitParametersValues();
        for(int i = path.length;i < ins.getFeature().length;++i){
            initParameter.put(automata.rangeParameters.get(i-path.length).name,ins.getFeature(i));
        }
        double end = 0;
        HashMap<String,Double> newMap = initParameter;
        for(int locIndex = 0;locIndex < path.length;++locIndex){
            end = args[locIndex];
            double step = 0;
            if(locIndex != 0){
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
                        System.out.println(entry.getValue());
                        System.exit(0);
                    }
                    newMap.put(entry.getKey(),result);
                    //allParametersValues.get(locIndex - 1).put(entry.getKey(),result);
                }
            }
            if(end==0){
                if(!checkConstraints(args,newMap)){
                    System.out.println("end==0");
                    ins.region[locIndex][1] = 1;
                    return;
                }
            }
            while(step < end){
                newMap = computeValuesByFlow(newMap,automata.locations.get(path[locIndex]),delta);
                for(HashMap.Entry<String,Double> entry : newMap.entrySet()){
                    ctx.set(entry.getKey(),entry.getValue());
                }
                if(!checkConstraints(args,newMap)) {
                    if(step == 0) ++step;
                    for(int i = 0;i < args.length;++i)
                        System.out.print(args[i] + " ");
                    System.out.println("locIndex " + locIndex);
                    ins.region[locIndex][1] = step;
                    return;
                }
                for(int i = 0;i < automata.locations.get(path[locIndex]).invariants.size();++i){
                    boolean result = (boolean)fel.eval(automata.locations.get(path[locIndex]).invariants.get((i)));
                    if(!result) {
                        String invariant = automata.locations.get(path[locIndex]).invariants.get(i);
                        if(computePenalty(invariant,false) < cerr)
                            continue;
                        if(Double.isNaN(computePenalty(invariant,false))){
                            System.out.println("NaN");
                            System.exit(-1);
                        }
                        else {
                            ins.region[locIndex][1] = step;
                            return;
                        }
                    }
                }
                step += 1;
            }
        }
    }

    private double computePenaltyOfConstraint(String expression){//just one level
        String []expressions = expression.split("\\|");
        //System.out.println(expressions.length);
        double result = Double.MAX_VALUE;
        for(String string:expressions){
            if(string.length()<=0)  continue;
            double temp = computePenalty(string,false);
            result = (temp < result) ? temp : result;
        }
        return result;
    }

    private double computePenalty(String expression,boolean isConstraint){
        if(isConstraint && expression.indexOf("|") != -1)
            return computePenaltyOfConstraint(expression);
        //System.out.println(expression);

        String []strings;
        String bigPart = "",smallPart = "";
        strings = expression.split("<=|<|>=|>|==");
        Object obj1 = fel.eval(strings[0].trim());
        Object obj2 = fel.eval(strings[1].trim());
        double big = 0,small = 0;
        if(obj1 instanceof Double)
            big = (double)obj1;
        else if(obj1 instanceof Integer) {
            big = (int) obj1;
            //System.out.println(entry.getKey() + " " + entry.getValue());
        }
        else {
            System.out.println("Not Double and Not Integer!");
            System.out.println(expression);
            System.out.println(obj1);
            System.out.println(obj1.getClass().getName());
            System.out.println("here");
            System.exit(0);
        }
        if(obj2 instanceof Double)
            small = (double)obj2;
        else if(obj2 instanceof Integer) {
            small = (int) obj2;
            //System.out.println(entry.getKey() + " " + entry.getValue());
        }
        else if(obj2 instanceof Long){
            small = ((Long)obj2).doubleValue();
        }
        else {
            small = 0;
            System.out.println("Not Double and Not Integer!");
            System.exit(0);
        }
//        if(isConstraint)
//            penal = small - big;
//        else penal = big - small;
        //System.out.println(penal);
//        if(penal < 0) {
//            System.out.println(expression);
//            System.out.println(small);
//        }
        return Math.abs(big-small);
    }

//    public boolean checkInvarientsByRacos(double []args){
//        int samplesize = 30;       // parameter: the number of samples in each iteration
//        int iteration = 100;       // parameter: the number of iterations for batch racos
//        int budget = 2000;         // parameter: the budget of sampling for sequential racos
//        int positivenum = 1;       // parameter: the number of positive instances in each iteration
//        double probability = 0.95; // parameter: the probability of sampling from the model
//        int uncertainbit = 1;      // parameter: the number of sampled dimensions
//        Instance ins = null;
//        int repeat = 15;
//        allParametersValues.add(0,automata.duplicateInitParametersValues());
//        allParametersValues.remove(allParametersValues.size() - 1);
//        Task t = new InvarientsObjectFunction(automata,path,args,allParametersValues);
//        for (int i = 0; i < repeat; i++) {
//            Continue conti = new Continue(t);
//            //conti.TurnOnSequentialRacos();
//            conti.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
//            conti.setBudget(budget);              // parameter: the budget of sampling
//            conti.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
//            conti.setRandProbability(probability);// parameter: the probability of sampling from the model
//            conti.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
//            conti.run();                          // call sequential Racos
//            ins = conti.getOptimal();             // obtain optimal
//            if(ins.getValue() == Double.MIN_VALUE)
//                System.out.print("Invarient Check failed");
//            return false;
//        }
//        return true;
//    }

    public boolean checkCycle(double []args){
        double sum = 0;
        for(int i = 0;i < args.length;++i){
            sum += args[i];
        }
        if(sum > automata.cycle / delta) {
            //p1 = sum - automata.cycle / delta;
            sat = false;
            penalty += sum - automata.cycle /delta;
            return false;
        }
        return true;
    }
    @Override
    public double getValue(Instance ins) {
        penalty = 0;
        globalPenalty = 0;
        sat = true;
        allParametersValues = new ArrayList<>();
        double []args = new double[path.length];
//        args[0] = 1.984442452584224;
//        args[1] = 0.1293550138895525;
//        args[2] = 0.3706788439511385;
//        args[3] = 1.4731068040009903;
//        for(int i = 0;i < ins.getFeature().length;++i){
//            System.out.println(ins.getFeature(i));
//        }
        for(int i = 0;i < path.length;++i){
            args[i] = ins.getFeature(i);
            //args[i] = 0.478405658973357;
//            if(args[i] >= 4000)
            //System.out.println(args[i] + " ");
        }
        for(int i = path.length;i < ins.getFeature().length;++i){
            automata.initParameterValues.put(automata.rangeParameters.get(i-path.length).name,ins.getFeature(i));
        }
//        if(!checkCycle(args))
//            return penalty;
        checkInvarientsByODE(args);
        checkGuards(args);
        if(!sat) {
            if(penalty + globalPenalty == 0){
                System.out.println("penalty = 0 when unsat");
                System.exit(0);
            }
            double penAll = penalty + globalPenalty;
            if(penAll < valueArc.penAll) {
                valueArc.penalty = penalty;
                valueArc.globalPenalty = globalPenalty;
                valueArc.penAll = penAll;
            }
            return penAll;
        }
        //System.out.println("what");
//        HashMap<String,Double> map = allParametersValues.get(allParametersValues.size() - 1);
//        System.out.println("x  " + map.get("x"));
//        System.out.println("fuel    " + map.get("fuel"));
//        double value = -10000 + map.get("MA") - map.get("x") + map.get("fuel");
//        System.out.println(value);
//        System.exit(0);
//        if(allParametersValues.get(allParametersValues.size()-1).get("angle") > 0.78 && allParametersValues.get(allParametersValues.size()-1).get("angle") < 0.79 && args[0]==19){
//            System.out.println(allParametersValues.get(allParametersValues.size()-1).get("x"));
//        }
        return computeValue(args);
    }


    public double computeValue(double []args){
        HashMap<String,Double> map = allParametersValues.get(allParametersValues.size() - 1);
        double value = Math.abs(200-map.get("x")) + Math.abs(200-map.get("y")) - 10000;
        if(value < valueArc.value){
            valueArc.value = value;
            valueArc.allParametersValues = allParametersValues.get(allParametersValues.size()-1);
            valueArc.args = args;
        }
        return value;
    }

    @Override
    public Dimension getDim() {
        return dim;
    }
}
