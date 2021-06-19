package Racos.ObjectiveFunction;

import MPC.Transition;
import Racos.Componet.Dimension;
import Racos.Componet.Instance;
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
            ctx.set(entry.getKey(),entry.getValue());
        }
        if(automata.forbiddenConstraints==null)
            return true;
        boolean result = (boolean)fel.eval(automata.forbiddenConstraints);
        if(!result) return true;
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

    public HashMap<String,Double> computeValuesByFlow(HashMap<String,Double> parametersValues,Location location,double arg){
        HashMap<String,Double> tempMap = new HashMap<>();
        for(HashMap.Entry<String,Double> entry : parametersValues.entrySet()){
            ctx.set(entry.getKey(),entry.getValue());
        }
        for(HashMap.Entry<String,Double> entry : parametersValues.entrySet()){
            if(location.flows.containsKey(entry.getKey())){
                String expression = location.flows.get(entry.getKey());
                if(expression.contains("phi(vx)")){
                    if(parametersValues.get("vx")<=2)
                        expression = expression.replace("phi(vx)","1");
                    else if(parametersValues.get("vx")<=5)
                        expression = expression.replace("phi(vx)","2");
                    else
                        expression = expression.replace("phi(vx)","3");
                }
                if(expression.contains("phi(vy)")){
                    if(parametersValues.get("vy")<=2)
                        expression = expression.replace("phi(vy)","1");
                    else if(parametersValues.get("vy")<=5)
                        expression = expression.replace("phi(vy)","2");
                    else
                        expression = expression.replace("phi(vy)","3");
                }
                Object obj = fel.eval(expression);
                double result;
                if(obj instanceof Double)
                    result = (double)obj;
                else if(obj instanceof Integer) {
                    result = (int) obj;
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
                tempMap.put(entry.getKey(),entry.getValue() + delta);
            }
            else {
                tempMap.put(entry.getKey(),entry.getValue());
            }
        }
        return tempMap;

    }

    public boolean checkGuards(double []args){
        HashMap<String,Double> parameterValues;
        for(int i = 0;i < path.length;++i){
            Location location = automata.locations.get(path[i]);
            parameterValues = allParametersValues.get(i);
            int target;
            if(i + 1 < path.length){
                target = path[i + 1];
                int source = path[i];
                for(int k = 0;k < automata.transitions.size();++k){
                    Transition transition = automata.transitions.get(k);
                    if(transition.source == source && transition.target == target){
                        for(Map.Entry<String,Double> entry : parameterValues.entrySet()){
                            ctx.set(entry.getKey(),entry.getValue());
                        }
                        for(int guardIndex = 0;guardIndex < transition.guards.size();++guardIndex){
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
        }
        else if(obj2 instanceof Long){
            small = ((Long)obj2).doubleValue();
        }
        else {
            small = 0;
            System.out.println("Not Double and Not Integer!");
            System.exit(0);
        }
        return Math.abs(big-small);
    }

    public boolean checkCycle(double []args){
        double sum = 0;
        for(int i = 0;i < args.length;++i){
            sum += args[i];
        }
        if(sum > automata.cycle / delta) {
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
        for(int i = 0;i < path.length;++i){
            args[i] = ins.getFeature(i);
        }
        for(int i = path.length;i < ins.getFeature().length;++i){
            automata.initParameterValues.put(automata.rangeParameters.get(i-path.length).name,ins.getFeature(i));
        }
        checkInvarientsByODE(args);
        checkGuards(args);
        if(!sat) {
            if(penalty + globalPenalty == 0){
                //todo cfg file should have brackets
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
        return computeValue(args);
    }


    public double computeValue(double []args){
        HashMap<String,Double> map = allParametersValues.get(allParametersValues.size() - 1);
        for(HashMap.Entry<String,Double> entry : map.entrySet()){
            ctx.set(entry.getKey(),entry.getValue());
        }
        ctx.set("target_x",automata.target_x);
        ctx.set("target_y",automata.target_y);
        Object obj = fel.eval(automata.obj_function);
        double value = 0;
        if(obj instanceof Double)
            value = (double)obj - 10000;
        else if(obj instanceof Integer){
            value = (int) obj - 10000;
        }
        else {
            System.err.println("error: result not of double!");
            System.out.println(obj);
            System.exit(-1);
        }
        if (value + 10000 < 0){
            System.out.println(map.get("x"));
            System.exit(0);
        }
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
