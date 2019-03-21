//package Test;
//
//import Racos.Componet.*;
//import Racos.Method.*;
//import Racos.ObjectiveFunction.*;
//
//public class examples {
//
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//		// parameters of Racos
//		int samplesize = 30;       // parameter: the number of samples in each iteration
//		int iteration = 1000;       // parameter: the number of iterations for batch racos
//		int budget = 2000;         // parameter: the budget of sampling for sequential racos
//		int positivenum = 1;       // parameter: the number of positive instances in each iteration
//		double probability = 0.95; // parameter: the probability of sampling from the model
//		int uncertainbit = 1;      // parameter: the number of sampled dimensions
//
//		if (true) {
//			Instance ins = null;
//			int repeat = 15;
//			Task t = new MIN_L(3);
//			for (int i = 0; i < repeat; i++) {
//				Continue con = new Continue(t);
//				con.TurnOnSequentialRacos();
//				con.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
//				con.setBudget(budget);              // parameter: the budget of sampling
//				con.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
//				con.setRandProbability(probability);// parameter: the probability of sampling from the model
//				con.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
//				con.run();                          // call sequential Racos
//				ins = con.getOptimal();             // obtain optimal
//				System.out.print("best function value:");
//				if(ins.getValue() == Double.MAX_VALUE)
//					System.out.print("MaxValue     ");
//				else
//					System.out.print(ins.getValue() + "    ");
//				System.out.print("[");
//				for(int j = 0;j < ins.getFeature().length;++j)
//					System.out.print(ins.getFeature(j) + ",");
//				System.out.println("]");
//			}
//		}
//
//		// example for batch racos in continuous solutions space
//		if (false) {
//			//the default setting of racos is batch
//			Instance ins = null;
//			int repeat = 1;
//			Task t = new Ackley(100);
//			for (int i = 0; i < repeat; i++) {
//				Continue con = new Continue(t);
//				con.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
//				con.setMaxIteration(iteration);     // parameter: the number of iterations
//				con.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
//				con.setRandProbability(probability);// parameter: the probability of sampling from the model
//				con.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
//				con.run();                          // call batch Racos
//				ins = con.getOptimal();             // obtain optimal
//				System.out.println("best function value:" + ins.getValue());
//			}
//		}
//
//		// example for sequential racos in continuous solution space
//		if (false) {
//			Instance ins = null;
//			int repeat = 1;
//			Task t = new Ackley(100);
//			for (int i = 0; i < repeat; i++) {
//				Continue con = new Continue(t);
//				con.TurnOnSequentialRacos();
//				con.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
//				con.setBudget(budget);              // parameter: the budget of sampling
//				con.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
//				con.setRandProbability(probability);// parameter: the probability of sampling from the model
//				con.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
//				con.run();                          // call sequential Racos
//				ins = con.getOptimal();             // obtain optimal
//				System.out.println("best function value:" + ins.getValue());
//			}
//		}
//
//		// example for optimization in discrete solution space
//		if(false){
//			Instance ins = null;
//			int repeat = 1;
//			Task t = new SetCover(30, 20);
//			for (int i = 0; i < repeat; i++) {
//				Discrete discrete = new Discrete(t);
//				discrete.setSampleSize(samplesize);       // parameter: the number of samples in each iteration
//				discrete.setMaxIteration(iteration);      // parameter: the number of iterations
//				discrete.setPositiveNum(positivenum);     // parameter: the number of positive instances in each iteration
//				discrete.setRandProbability(probability); // parameter: the probability of sampling from the model
//				discrete.setUncertainBits(uncertainbit);  // parameter: the number of samplable dimensions
//				discrete.run();                           // call Racos
//				ins = discrete.getOptimal();              // obtain optimal
//				System.out.println("best function value:" + ins.getValue());
//			}
//		}
//
//		// example for optimization in mixed solutions space
//		if(false){
//			Instance ins = null;
//			int repeat = 10;
//			Task t = new MixedFunction(100);
//			for (int i = 0; i < repeat; i++) {
//				Mix mix = new Mix(t);
//				mix.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
//				mix.setMaxIteration(iteration);     // parameter: the number of iterations
//				mix.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
//				mix.setRandProbability(probability);// parameter: the probability of sampling from the model
//				mix.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
//				mix.run();                          // call Racos
//				ins = mix.getOptimal();             // obtain optimal
//				System.out.println("best function value:" + ins.getValue());
//			}
//		}
//
//	}
//
//}
