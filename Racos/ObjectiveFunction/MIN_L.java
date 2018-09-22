package Racos.ObjectiveFunction;

import Racos.Componet.Dimension;
import Racos.Componet.Instance;
import Racos.Componet.*;
import Racos.Method.*;

/**
 * MIN_L
 *
 * @author CedricXing
 * Created on 2018/9/6
 * Copyright (c) 2018/9/6. CedricXing All rights Reserved.
 */

public class MIN_L implements Task{
    private Dimension dim;

    public MIN_L(int size){
        dim = new Dimension();
        dim.setSize(size);
        dim.setDimension(0,0.0,5.0,true);
        dim.setDimension(1,0.0,5.0,true);
        //dim.setDimension(2,0.0,5.0,true);
        dim.setDimension(2,-5.0,5.0,true);
    }

    @Override
    public double getValue(Instance ins){
        double v0 = 30;
        double x0 = 0;
        double MA = 200,f = 8.3;
        double []v = new double[ins.getFeature().length];
        for(int i=0; i<ins.getFeature().length; i++){
            v[i] = ins.getFeature(i);
        }
        double in0[] = new double[2];
        in0[0] = v0;
        in0[1] = x0;
        double []out0 = AC(in0,v[0]);
        //System.out.println(out0[1]);
        double []out1 = CC(out0,v[1],v[2]);
        if(out0[1] > MA)
            return Double.MAX_VALUE;
        double ebi0 = Math.sqrt(2 * 10 * (MA - out0[1]));
        if(out1[1] > MA)
            return Double.MAX_VALUE;
        double ebi1 = Math.sqrt(2 * 10 * (MA - out1[1]));
        if(out1[1] > MA || v[0] + v[1] > 5 || !(out0[0] < ebi0 - 15 && out0[0] > ebi0 - 20) || out1[0] >= ebi1 || out1[0] <= ebi1 - 20)
            return Double.MAX_VALUE;
//        //System.out.println("Ready to check");
//        boolean result = checkConstrain(v,v0,MA);
//        boolean result1 = checkConstrain1(v,v0,MA);
//        if(result != result1 && result == false ) {
//            System.out.println("what?");
//            System.out.println(v[0]);
//            System.out.println(v[1]);
//            System.out.println(v[2]);
//            System.exit(0);
//        }
//        if(!result1 && result){
//            System.out.println("Not enough");
//        }
//        if(!(result && result1)){
//            return Double.MAX_VALUE;
//        }
//        if(!checkConstrain(v,v0,MA)){
//            return Double.MAX_VALUE;
//        }
        if(gradientCheck(v,v0,MA)){
            return Double.MAX_VALUE;
        }
//        if(!checkConstrain(v,v0,MA)){
//            //System.out.println("not enough");
//            return Double.MAX_VALUE;
//        }
        //System.out.println("Check finished");
        double cost = (v[0] + 2.5 * v[0] * v[0] / v0) * f + (v[1] + v[2] * v[1] * v[1] / 2 / (v0 + 5 * v[0])) * (1 + 5 * v[0] / v0) * f;
        //double cost = (in0[0] + out0[0]) / 2 / v0 * f * v[0] + (out0[0] + out1[0]) / 2 / v0 * f * v[1] + (out1[0] + out2[0]) / 2 / v0 * f * v[2];
        return MA - out1[1] + cost;
    }
//
    public boolean checkConstrain1(double []v,double v0,double MA){
        double tStart = v[0];
        double tEnd = v[1];
        double delta = 0.000001;
        double t = tStart + delta;
        while(t < tEnd){
            double tempT = t - tStart;
            double v1 = v0 + 5 * v[0];
            double f1 = v1 + v[2] * tempT;
            double x = v0 * v[0] + 0.5 * 5 * v[0] * v[0] + v1 * tempT + 0.5 * v[2] * tempT * tempT;
            double f2 = Math.sqrt(2 * 10 * (MA - x));
            //System.out.println(" ");
            if(f2 - f1 < 0) {
                //System.out.println("check");
                return false;
            }
            t += delta;
        }
        return true;
    }

    public boolean gradientCheck(double []v,double v0,double MA){
        double v1 = v0 + 5 * v[0];
        double x1 = v0 * v[0] + 0.5 * 5 * v[0] * v[0];
        double step = 0.01;
        double t = 0;
        int loop = 1;
        while(t >= 0 && t <= v[1] - v[0] && loop < 10000){
            double l_t = v[2] + (10 * v1 + 10 * v[2] * t) / Math.sqrt(20 * (MA - x1 - v1 * t - 0.5 * v[2] * t * t));
            t = t -l_t * step;
            double x = v1 * t + 0.5 * v[2] * t * t;
            double f = v1 + v[2] * t - Math.sqrt(20 * (MA - x1 - x));
            System.out.println(l_t);
            if(f > 0)
                return false;
            ++loop;
        }
        //System.out.println("*********************************************");
        return true;
    }

    public boolean checkConstrain(double []v,double v0,double MA) {
        int samplesize = 300;       // parameter: the number of samples in each iteration
        int iteration = 10000;       // parameter: the number of iterations for batch racos
        int budget = 2000;         // parameter: the budget of sampling for sequential racos
        int positivenum = 1;       // parameter: the number of positive instances in each iteration
        double probability = 0.99; // parameter: the probability of sampling from the model
        int uncertainbit = 1;      // parameter: the number of sampled dimensions

        Instance ins = null;
        int repeat = 15;
        Task t = new constrain(1,v,v0,MA);
        for (int i = 0; i < repeat; i++) {
            Continue con = new Continue(t);
            //con.TurnOnSequentialRacos();
            con.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
            con.setBudget(budget);              // parameter: the budget of sampling
            con.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
            con.setRandProbability(probability);// parameter: the probability of sampling from the model
            con.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
            con.run();                          // call sequential Racos
            ins = con.getOptimal();             // obtain optimal
            if(ins.getValue() < 0) {
                //System.out.println("check");
                System.out.println(ins.getFeature(0));
                System.out.println(ins.getValue());
                return false;
            }
        }
        return true;
    }

    @Override
    public Dimension getDim() {
        return dim;
    }

    public double[] AC(double in[],double t){
        /*
            in[0] v
            in[1] x
         */
        double []out = new double[in.length];
        out[0] = in[0] + 5 * t;
        out[1] = in[1] + in[0] * t + 0.5 * 5 * t * t;
        return out;
    }

    public double[] CC(double in[],double t,double a){
        /*
            in[0] v
            in[1] x
         */
        double []out = new double[in.length];
        out[0] = in[0] + a * t;
        out[1] = in[1] + in[0] * t + 0.5 * a * t * t;
        return out;
    }

    public class constrain implements Task{
        private Dimension dim;
        private double v0;
        private double MA;
        private double []arg;

        public constrain(int size,double []v,double v0,double MA){
            dim = new Dimension();
            dim.setSize(size);
            dim.setDimension(0,v[0],v[1],true);
            this.v0 = v0;
            this.MA = MA;
            this.arg = new double[v.length];
            for(int i = 0;i < v.length;++i)
                this.arg[i] = v[i];
        }

        @Override
        public double getValue(Instance ins) {
            double []v = new double[ins.getFeature().length];
            for(int i=0; i<ins.getFeature().length; i++){
                v[i] = ins.getFeature(i);
            }
            double deltaT = v[0] - arg[0];
            double v1 = v0 + 5 * arg[0];
            double f1 = v1 + arg[2] * deltaT;
            double x = v0 * arg[0] + 0.5 * 5 * arg[0] * arg[0] + v1 * deltaT + 0.5 * arg[2] * deltaT * deltaT;
            if(MA <= x)
                return Double.MAX_VALUE;
            double f2 = Math.sqrt(2 * 10 * (MA - x));
            //if(f2 - f1 < 0)
              //  System.out.println(f2 - f1);
            return f2 - f1;
        }

        @Override
        public Dimension getDim() {
            return dim;
        }
    }
}
