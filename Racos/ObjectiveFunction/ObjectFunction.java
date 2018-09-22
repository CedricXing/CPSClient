package Racos.ObjectiveFunction;

import Racos.Componet.Dimension;
import Racos.Componet.Instance;
import Racos.Componet.*;
import Racos.Method.*;
import MPC.Automata;

import com.greenpineyu.fel.*;

public class ObjectFunction implements Task{
    private Dimension dim;
    private Automata automata;

    public ObjectFunction(Automata automata,int []path){
        this.automata = automata;
    }
    @Override
    public double getValue(Instance ins) {
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
