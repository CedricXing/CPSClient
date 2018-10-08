package MPC;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Transition
 *
 * @author CedricXing
 * Created on 2018/9/14
 * Copyright (c) 2018/9/14. CedricXing All rights Reserved.
 */

public class Transition {
    public int source;
    public int target;
    public ArrayList<String> guards;
    public HashMap<String,String> assignments;

    public Transition(int source,int target){
        this.source = source;
        this.target = target;
        guards = new ArrayList<>();
        assignments = new HashMap<>();
    }

    public void setGuard(String guard, ArrayList<String> parameters){
        int index = guard.indexOf("&amp;");
        while(index != -1){
            String temp = guard.substring(0,index).trim();
            processGuard(temp,parameters);
            guard = guard.substring(index + 5).trim();
            index = guard.indexOf("&amp;");
        }
        processGuard(guard,parameters);

//        for(int i = 0;i < guards.size();++i){
//            System.out.println(guards.get(i));
//        }
    }

    public void processGuard(String guard,ArrayList<String> parameters){
        guard = guard.replace("&gt;",">");
        guard = guard.replace("&lt;","<");

//        for(int i = parameters.size() - 1;i >= 0;--i ){
//            guard = guard.replace(parameters.get(i),"$" + i);
//        }
        guards.add(guard);
    }

    public void setAssignment(String assignment,ArrayList<String> parameters){
        int index = assignment.indexOf("&amp;");
        while(index != -1){
            String temp = assignment.substring(0,index).trim();
            processAssignment(temp,parameters);
            assignment = assignment.substring(index + 5).trim();
            index = assignment.indexOf("&amp;");
        }
        processAssignment(assignment,parameters);

        for(HashMap.Entry<String,String> entry : assignments.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    public void processAssignment(String assignment,ArrayList<String> parameters){
        String []strings = assignment.split("=");
        for(int i = parameters.size() - 1;i >= 0;--i){
            if(strings[0].indexOf(parameters.get(i)) != -1){
                String string = processMathFunction(strings[strings.length - 1]);
                if(string.indexOf("[") != -1){
                    int firstIndex = string.indexOf("[");
                    int lastIndex = string.indexOf("]");
                    String []temps = string.substring(firstIndex + 1,lastIndex).trim().split(",");
                    double lowerbound = Double.parseDouble(temps[0].trim());
                    double upperbound = Double.parseDouble(temps[1].trim());
                    String randomArea = new String((upperbound + lowerbound) / 2 + " + ($(Math).random() - 0.5) * " + (upperbound - lowerbound));
                    string = string.substring(0,firstIndex) + randomArea + string.substring(lastIndex + 1);
                }
                assignments.put(parameters.get(i),string);
                return;
            }
        }
    }

    public String processMathFunction(String string){
        string = string.replace("pow","$(Math).pow");
        string = string.replace("sin","$(Math).sin");
        string = string.replace("cos","$(Math).cos");
        string = string.replace("tan","$(Math).tan");
        return string;
    }

    public void printTransition(){
        System.out.println("source " + source + " target " + target);
        for(int i = 0;i < guards.size();++i){
            System.out.println(guards.get(i));
        }
    }
}
