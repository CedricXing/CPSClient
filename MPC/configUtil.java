package MPC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class configUtil {
    private Map<String,String> map = new HashMap<>();
    private String path = "config.ini";
    private Set<String> keySet = new HashSet<>(Arrays.asList("bound","system","mission"));
    private Map<String,Double> timeLimit = new HashMap<>();

    public configUtil(){
        File file_ = new File(path);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file_));
            String tempLine = null;
            while((tempLine = bufferedReader.readLine()) != null) {
                String[] strings = tempLine.split(":");
                map.put(strings[0].trim(),strings[1].trim());
            }
        }
        catch (IOException e){
            System.out.println("Error: configuration file not found!");
            System.exit(0);
        }

        for(Map.Entry<String,String> entry : map.entrySet()){
            if(!keySet.contains(entry.getKey())) {
                System.out.println("Error: invalid configuration keyword: " + entry.getKey() + "!");
                System.exit(0);
            }
//            System.out.println("key:"+entry.getKey()+" value:"+entry.getValue());
        }

        initializeTimeLimit();
    }

    public String get(String key){
        if(!map.containsKey(key)){
            System.out.println("Error: '" + key + "' not found!");
            System.exit(0);
        }
        return map.get(key);
    }

    public void initializeTimeLimit(){
        timeLimit.put("models/vehicle_forward.xml",30.0);
        timeLimit.put("models/vehicle_turn.xml",30.0);
        timeLimit.put("models/vehicle_circle.xml",35.0);
        timeLimit.put("models/drone_turn.xml",60.0);
        timeLimit.put("models/drone_two_turn.xml",60.0);
        timeLimit.put("models/drone_forward.xml",60.0);
        timeLimit.put("models/vehicle_multi_phase.xml",120.0);
        timeLimit.put("models/drone_multi_phase.xml",300.0);
        timeLimit.put("models/train_online.xml",10.0);
    }

    public double getTimeLimit(String key){
        return timeLimit.get(key);
    }
}
