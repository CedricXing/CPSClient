package MPC;

import java.util.ArrayList;

/**
 * Transition
 *
 * @author CedricXing
 * Created on 2018/9/14
 * Copyright (c) 2018/9/14. CedricXing All rights Reserved.
 */

public class Transition {
    private int source;
    private int target;
    private ArrayList<String> guards;

    public Transition(int source,int target){
        this.source = source;
        this.target = target;
        guards = new ArrayList<>();
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

        for(int i = parameters.size() - 1;i >= 0;--i ){
            guard = guard.replace(parameters.get(i),"$" + i);
        }
        guards.add(guard);
    }

    public void setAssignment(String assignment,ArrayList<String> parameters){

    }
}
