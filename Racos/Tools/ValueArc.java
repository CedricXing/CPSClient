package Racos.Tools;

import java.util.ArrayList;
import java.util.HashMap;

public class ValueArc{
    public double penalty;
    public double globalPenalty;
    public boolean sat;
    public double value;
    public double penAll;
    public HashMap<String,Double> allParametersValues;
    public double []args;
    public int []path;
    public int iterativeNums;
    public ArrayList<Double> arrayListBestValues;

    public ValueArc(double penalty,double globalPenalty,boolean sat){
        this.penalty = penalty;
        this.globalPenalty = globalPenalty;
        this.sat = sat;
        iterativeNums = 0;
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
