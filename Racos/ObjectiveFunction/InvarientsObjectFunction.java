package Racos.ObjectiveFunction;

import MPC.Automata;
import MPC.Location;
import Racos.Componet.Dimension;
import Racos.Componet.Instance;
import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.context.FelContext;

import java.util.ArrayList;
import java.util.HashMap;

public class InvarientsObjectFunction implements Task {
    private Dimension dim;
    private Automata automata;
    private int []path;
    private FelEngine fel;
    private FelContext ctx;
    private ArrayList<HashMap<String,Double>> allParametersValues;
    private double[] preArgs;

    public InvarientsObjectFunction(Automata automata, int []path, double []args, ArrayList<HashMap<String,Double>> allParametersValues){
        this.automata = automata;
        dim = new Dimension();
        dim.setSize(path.length);
        for(int i = 0;i < dim.getSize();++i){
            if(i == 0)  dim.setDimension(0,0,args[0],true);
            else dim.setDimension(i,args[i - 1],args[i],true);
        }
        this.path = path;
        this.allParametersValues = allParametersValues;
        this.preArgs = args;
        fel = new FelEngineImpl();
        ctx = fel.getContext();
    }

    @Override
    public double getValue(Instance ins) {
        double []args = new double[ins.getFeature().length];
        for(int i = 0;i < args.length;++i){
            args[i] = ins.getFeature(i);
        }
        for(int i = 0;i < path.length;++i){
            //double t = (i == 0) ? args[0] : (args[i] - preArgs[i - 1]);
            HashMap<String,Double> tempMap = computeValuesByFlow(allParametersValues.get(i),automata.locations.get(path[i]),args[i]);
            for(HashMap.Entry<String,Double> entry : tempMap.entrySet()){
                ctx.set(entry.getKey(),entry.getValue());
            }
            ArrayList<String> invarients = automata.locations.get(path[i]).invarientsExpression;
            for(int j = 0;j < invarients.size();++j){
                double result = (double)fel.eval(invarients.get(j));
                if(result < 0)  {
                    //System.out.println(result);
                    return Double.MIN_VALUE;
                }
            }
        }
        return 0;
    }

    public HashMap<String,Double> computeValuesByFlow(HashMap<String,Double> parametersValues, Location location, double arg){
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

    @Override
    public Dimension getDim() {
        return dim;
    }
}
