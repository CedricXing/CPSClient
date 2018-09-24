package Racos.ObjectiveFunction;

import Racos.Componet.Dimension;
import Racos.Componet.Instance;
import Racos.Componet.*;
import Racos.Method.*;
import MPC.Automata;
import MPC.Location;

import com.greenpineyu.fel.*;

import java.util.Map;

public class ObjectFunction implements Task{
    private Dimension dim;
    private Automata automata;
    private int []path;
    private FelEngine fel;

    public ObjectFunction(Automata automata,int []path){
        this.automata = automata;
        dim = new Dimension();
        dim.setSize(path.length);
        this.path = path;
        fel = new FelEngineImpl();
    }

    public boolean checkConstraints(double []args){
        Map<String,Double> parametersValues = automata.duplicateInitParametersValues();
        for(int i = 0;i < path.length;++i){
            Location location = automata.locations.get(path[i]);
            computeValuesByFlow(parametersValues,location,args[i]);
        }
        return true;
    }

    public void computeValuesByFlow(Map<String,Double> parametersValues,Location location,double arg){
//        double t = parametersValues.get("t") + arg;
//        parametersValues.put("t",t);
//        double a;
//        if(location.getNo() == 1)   a = 5;
//        else if(location.getNo() == 2)  a = 0 + (Math.random() - 0.5) * 10;
//        else a = -10;
//        double v = parametersValues.get("v") + a * arg;
//        double x =
    }

    @Override
    public double getValue(Instance ins) {
        double []args = new double[ins.getFeature().length];
        for(int i = 0;i < args.length;++i){
            args[i] = ins.getFeature(i);
        }
        if(!checkConstraints(args)) return Double.MAX_VALUE;

        return 0;
    }

    boolean checkConstrain(){
        return true;
    }

    @Override
    public Dimension getDim() {
        return dim;
    }
}
