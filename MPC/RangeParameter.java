package MPC;

public class RangeParameter {
    public double lowerBound;
    public double upperBound;
    public String name;

    public RangeParameter(String name,double lowerBound,double upperBound){
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }
}
