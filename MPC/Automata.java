package MPC;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Logger;

/**
 * Automata
 *
 * @author CedricXing
 * Created on 2018/9/14
 * Copyright (c) 2018/9/14. CedricXing All rights Reserved.
 */

public class Automata {
    private ArrayList<Location> locations;
    private ArrayList<Transition> transitions;
    private ArrayList<String> parameters;

    public Automata(String filename){
        File modelFile = new File(filename);
        BufferedReader reader = null;
        locations = new ArrayList<>();
        transitions = new ArrayList<>();
        parameters = new ArrayList<>();
        try{
            reader = new BufferedReader(new FileReader(modelFile));
            String tempLine = null;
            while((tempLine = reader.readLine()) != null){
                if(tempLine.indexOf("<para") != -1){ // paramater definition
                    while(true){
                        String []strings = tempLine.split("\"");
                        if(strings[3].equals("real"))
                            parameters.add(strings[1]);
                        tempLine = reader.readLine();
                        if(tempLine.indexOf("<para") == -1) {
                            parameters.sort(new Comparator<String>() {
                                @Override
                                public int compare(String o1, String o2) {
                                    if(o1.length() < o2.length())
                                        return -1;
                                    else if(o1.length() > o2.length())
                                        return 1;
                                    else return 0;
                                }
                            });
                            break;
                        }
                    }
                }
                if(tempLine.indexOf("<location") != -1){ // location definition
                    String []strings = tempLine.split("\"");
                    //System.out.println(strings[1]);
                    Location location = new Location(Integer.parseInt(strings[1]));
                    tempLine = reader.readLine();
                    int beginIndex = tempLine.indexOf("<invariant>") + 11;
                    int endIndex = tempLine.indexOf("</invariant>");
                    String variant = tempLine.substring(beginIndex,endIndex).trim();
                    //System.out.println(variant);
                    location.setVariant(variant,parameters);
                    tempLine = reader.readLine();
                    beginIndex = tempLine.indexOf("<flow>") + 6;
                    endIndex = tempLine.indexOf("</flow>");
                    String flow = tempLine.substring(beginIndex,endIndex).trim();
                    //System.out.println(flow);
                    location.setFlow(flow);

                    locations.add(location);
                }
                if(tempLine.indexOf("<transition") != -1){ // transition definition
                    String []strings = tempLine.split("\"");
                    int source = Integer.parseInt(strings[1]);
                    int target = Integer.parseInt(strings[3]);
                    Transition transition = new Transition(source,target);
                    tempLine = reader.readLine(); // label (useless)
                    tempLine = reader.readLine(); // guard
                    int beginIndex = tempLine.indexOf("<guard>") + 7;
                    int endIndex = tempLine.indexOf("</guard>");
                    String guard = tempLine.substring(beginIndex,endIndex).trim();
//                    System.out.println(source + "->" + target);
//                    System.out.println(guard);
                    transition.setGuard(guard,parameters);

                    tempLine = reader.readLine();
                    beginIndex = tempLine.indexOf("<assignment>") + 12;
                    endIndex = tempLine.indexOf("</assignment>");
                    String assignment = tempLine.substring(beginIndex,endIndex).trim();
                    transition.setAssignment(assignment,parameters);

                    transitions.add(transition);
                }
            }
        }
        catch (FileNotFoundException e){
            System.out.println("File not found" + '\n' + e.getMessage());
        }
        catch (IOException e){
            System.out.println("IO Exception" + '\n' + e.getMessage());
        }
        finally {
            if(reader != null){
                try{
                    reader.close();
                }
                catch (IOException e){
                    System.out.println("IO Exception" + '\n' + e.getMessage());
                }
            }
        }
    }

    public static void main(String []args){
        Automata automata = new Automata("/home/cedricxing/Downloads/model.xml");
    }
}
