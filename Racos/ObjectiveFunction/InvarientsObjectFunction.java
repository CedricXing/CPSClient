package Racos.ObjectiveFunction;

import MPC.Automata;
import Racos.Componet.Dimension;
import Racos.Componet.Instance;
import com.greenpineyu.fel.FelEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class InvarientsObjectFunction implements Task {
    private Dimension dim;
    private Automata automata;
    private int []path;
    private FelEngine fel;
    private ArrayList<HashMap<String,Double>> allParametersValues;

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
    }

    @Override
    public double getValue(Instance ins) {
        return 0;
    }

    @Override
    public Dimension getDim() {
        return null;
    }
}
