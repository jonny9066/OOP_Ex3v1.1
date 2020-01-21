package gameClient;

import oop_dataStructure.oop_node_data;

import java.util.List;

public class PathAndTarget {
    public PathAndTarget(List<oop_node_data> path, String target){
        this.path = path;
        this.target = target;
    }
    public List<oop_node_data> getPath(){
        return path;
    }
    public String getTarget(){
        return target;
    }
    private List<oop_node_data> path;
    private String target;
}
