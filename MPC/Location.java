package MPC;

import MPC.tools.Fel_ExpressionProc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Location
 *
 * @author CedricXing
 * Created on 2018/9/14
 * Copyright (c) 2018/9/14. CedricXing All rights Reserved.
 */

public class Location {
    private int no;
    public String name;
    public ArrayList<String> invariants;
    public ArrayList<String> invarientsExpression;
    private ArrayList<Integer> neibours;
    public HashMap<String,String> flows;

    public Location(int no,String name){
        this.no = no;
        this.name = name;
        invariants = new ArrayList<>();
        invarientsExpression = new ArrayList<>();
        neibours = new ArrayList<>();
        flows = new HashMap<>();
    }

    public void setVariant(String variant,ArrayList<String> parameters){
        int index = variant.indexOf("&amp;");
        while(index != -1){
            String temp = variant.substring(0,index).trim();
            if(temp.length() > 0)   processVariant(temp,parameters);
            variant = variant.substring(index + 5).trim();
            index = variant.indexOf("&amp;");
        }
        processVariant(variant,parameters);

    }

    private void processVariant(String variant,ArrayList<String> parameters){
        variant = variant.replace("&gt;",">");
        variant = variant.replace("&lt;","<");

        invariants.add(variant);
        if(variant.indexOf(">=") != -1){
            invarientsExpression.add(variant.substring(0,variant.indexOf(">=")) + "-(" + variant.substring(variant.indexOf(">=") + 2) + ")");
        }
        else if(variant.indexOf(">") != -1){
            invarientsExpression.add(variant.substring(0,variant.indexOf(">")) + "-(" + variant.substring(variant.indexOf(">") + 1) + ")");
        }
        else if(variant.indexOf("<=") != -1){
            invarientsExpression.add(variant.substring(variant.indexOf("<=") + 2) + "-(" + variant.substring(0,variant.indexOf("<=")) + ")");
        }
        else if(variant.indexOf("<") != -1){
            invarientsExpression.add(variant.substring(variant.indexOf("<") + 1) + "-(" + variant.substring(0,variant.indexOf("<")) + ")");
        }
    }

    private void processFlow(String flow,ArrayList<String> parameters){
        String []strings = flow.split("=");
        for(int i = parameters.size() - 1;i >= 0;--i){
            if(strings[0].indexOf(parameters.get(i)) != -1){
//                String string = processMathFunction(strings[strings.length - 1]);
                String string = Fel_ExpressionProc.processMathFunction(strings[strings.length - 1]);
                flows.put(parameters.get(i),string);
                return;
            }
        }
    }

    public void setFlow(String flow,ArrayList<String> parameters){
        int index = flow.indexOf("&amp;");
        while(index != -1){
            String temp = flow.substring(0,index).trim();
            if(temp.length() > 0) processFlow(temp,parameters);
            flow = flow.substring(index + 5).trim();
            index = flow.indexOf("&amp;");
        }
        processFlow(flow,parameters);
//        for(HashMap.Entry<String,String> entry : flows.entrySet()){
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        }
    }

    public void addNeibour(int no){
        neibours.add(no);
    }

    public int getNo(){
        return no;
    }

    public ArrayList<Integer> getNeibours(){
        return neibours;
    }

    public void printLocation(){
        System.out.println("no : " + no + " name : " + name + "\n");
        for(int i = 0;i < invariants.size();++i){
            System.out.println(invariants.get(i));
        }
        for(Map.Entry<String,String> entry : flows.entrySet()){
            System.out.print(entry.getKey() + "'==");
            System.out.println(entry.getValue());
        }
        System.out.println("Neibours");
        for(int i = 0;i < neibours.size();++i){
            System.out.print(neibours.get(i) + ",");
        }
        System.out.println("");
    }
}
