package gameClient;

import oop_dataStructure.oop_node_data;

import java.util.ArrayList;
import java.util.List;

public class Robot {

    public Robot(int id){
        setId(id);
        path = new ArrayList<>();
    }

    // gets next node and removes it from list, if list is empty, returns null
    public oop_node_data getNextNode(){
        if(path.size() == 0){
            return null;
        }
        else{
            oop_node_data nextNode = path.get(0);
            path.remove(path.get(0));
            // if we got to the fruit, remove it from target
            if(path.size() == 0){
                targetFruitJSON = null;
            }
            return nextNode;
        }
    }

    // getters, setters
    public String getTargetFruitJSON(){
        return targetFruitJSON;
    }
    public int getId(){
        return id;
    }
    public void setPath(List<oop_node_data> path){
        this.path = path;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setTargetFruitJSON(String fruit){
        targetFruitJSON = fruit;
    }
    // data
    private List<oop_node_data> path;
    private int id;
    private String targetFruitJSON = null;
}
