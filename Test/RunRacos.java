//package Test;
//
//import Racos.Componet.*;
//import Racos.Method.*;
//import Racos.ObjectiveFunction.*;
//
//public class RunRacos {
//    public static void main(String []args){
//        int samplesize = 30;       // parameter: the number of samples in each iteration
//        int iteration = 1000;       // parameter: the number of iterations for batch racos
//        int budget = 2000;         // parameter: the budget of sampling for sequential racos
//        int positivenum = 1;       // parameter: the number of positive instances in each iteration
//        double probability = 0.95; // parameter: the probability of sampling from the model
//        int uncertainbit = 1;      // parameter: the number of sampled dimensions
//
//        double current = System.currentTimeMillis();
//        Instance ins = null;
//        int repeat = 15;
//        Task t = new MIN_L(3);
//        for (int i = 0; i < repeat; i++) {
//            Continue con = new Continue(t);
//            con.TurnOnSequentialRacos();
//            con.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
//            con.setBudget(budget);              // parameter: the budget of sampling
//            con.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
//            con.setRandProbability(probability);// parameter: the probability of sampling from the model
//            con.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
//            con.run();                          // call sequential Racos
//            ins = con.getOptimal();             // obtain optimal
//            System.out.print("best function value:");
//            if(ins.getValue() == Double.MAX_VALUE)
//                System.out.print("MaxValue     ");
//            else
//                System.out.print(ins.getValue() + "    ");
//            System.out.print("[");
//            for(int j = 0;j < ins.getFeature().length;++j)
//                System.out.print(ins.getFeature(j) + ",");
//            System.out.println("]");
//        }
//        System.out.println(System.currentTimeMillis() - current );
////        double currentTime = System.currentTimeMillis();
////        double t = 0;
////        double min = Double.MAX_VALUE;
////        while(t < 50){
////            double temp = Math.sqrt(200 - t);
////            if(temp < min)
////                min = temp;
////            t = t + 0.00001;
////        }
////        System.out.println(t);
////        System.out.println("time");
////        System.out.println(System.currentTimeMillis() - currentTime);
//    }
//}
