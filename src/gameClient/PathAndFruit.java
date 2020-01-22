package gameClient;

import oop_dataStructure.oop_node_data;

import java.util.List;

/**
 * Just a class to store path and target for returning them together
 */
public class PathAndFruit {
    public PathAndFruit(List<oop_node_data> path, Fruit fruit){
        this.path = path;
        this.fruit = fruit;
    }
    public List<oop_node_data> getPath(){
        return path;
    }
    public Fruit getFruit(){
        return fruit;
    }
    private List<oop_node_data> path;
    private Fruit fruit;
}
