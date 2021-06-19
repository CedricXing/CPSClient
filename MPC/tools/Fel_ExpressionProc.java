package MPC.tools;

public class Fel_ExpressionProc {
    static public String processMathFunction(String string){
        string = string.replace("pow","$(Math).pow");
        string = string.replace("sin","$(Math).sin");
        string = string.replace("cos","$(Math).cos");
        string = string.replace("tan","$(Math).tan");
        string = string.replace("abs","$(Math).abs");
        return string;
    }
}
