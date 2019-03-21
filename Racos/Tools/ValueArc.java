package Racos.Tools;

import java.util.HashMap;

public class ValueArc{
    public double penalty;
    public double globalPenalty;
    public boolean sat;
    public double value;
    public double penAll;
    public HashMap<String,Double> allParametersValues;
    public double []args;
    public ValueArc(double penalty,double globalPenalty,boolean sat){
        this.penalty = penalty;
        this.globalPenalty = globalPenalty;
        this.sat = sat;
    }

    public ValueArc(double value,boolean sat){
        this.value = value;
        this.sat = sat;
    }

    public ValueArc(){
        penAll = Double.MAX_VALUE;
        value = Double.MAX_VALUE;
    }
}
