package Racos.ObjectiveFunction;

import Racos.Componet.Dimension;
import Racos.Componet.Instance;

public class EFunction implements Task {
    private Dimension dim;

    public EFunction(int size){
        dim = new Dimension();
        dim.setSize(size);
        dim.setDimension(0,0.0,50.0,true);
    }

    @Override
    public double getValue(Instance ins) {
        return Math.sqrt(200 - ins.getFeature(0));
    }

    @Override
    public Dimension getDim() {
        return dim;
    }
}
