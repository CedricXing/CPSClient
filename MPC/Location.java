package MPC;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Location
 *
 * @author CedricXing
 * Created on 2018/9/14
 * Copyright (c) 2018/9/14. CedricXing All rights Reserved.
 */

public class Location {
    private int no;
    private ArrayList<String> variants;

    public Location(int no){
        this.no = no;
        variants = new ArrayList<>();
    }

    public void setVariant(String variant,ArrayList<String> parameters){
        int index = variant.indexOf("&amp;");
        while(index != -1){
            String temp = variant.substring(0,index).trim();
            processVariant(temp,parameters);
            variant = variant.substring(index + 5).trim();
            index = variant.indexOf("&amp;");
        }
        processVariant(variant,parameters);

//        for(int i = 0;i < variants.size();++i){
//            System.out.println(variants.get(i));
//        }
    }

    private void processVariant(String variant,ArrayList<String> parameters){
        variant = variant.replace("&gt;",">");
        variant = variant.replace("&lt;","<");

        for(int i = parameters.size() - 1;i >= 0;--i ){
            variant = variant.replace(parameters.get(i),"$" + i);
        }
        variants.add(variant);
    }

    public void setFlow(String flow){

    }
}
