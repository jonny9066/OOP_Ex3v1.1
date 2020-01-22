package gameClient;

import oop_dataStructure.oop_node_data;
import oop_utils.OOP_Point3D;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Robot {

    public Robot(String json_str){
        try {
            JSONObject r = new JSONObject(json_str);
            setId(r.getJSONObject("Robot").getInt("id"));
            setSrc(r.getJSONObject("Robot").getInt("src"));
            setDest(r.getJSONObject("Robot").getInt("dest"));
            setPos(tools.parseCoordinate(r.getJSONObject("Robot").getString("pos")));

        } catch (JSONException jsonException) {
            System.out.println(jsonException);
        }
    }
    // update position, src and dest
    public void updateCoordinates(String json_str){
        try {
            // get robot info
            JSONObject r = new JSONObject(json_str);
            setSrc(r.getJSONObject("Robot").getInt("src"));
            setDest(r.getJSONObject("Robot").getInt("dest"));
            setPos(tools.parseCoordinate(r.getJSONObject("Robot").getString("pos")));

        } catch (JSONException jsonException) {
            System.out.println(jsonException);
        }
    }

    // gets next node and removes it from list, if list is empty, returns null
    public int getNextNode(){
        if(path == null || path.size() == 0){
            return -1;
        }
        else{
            // get and remove next node
            int nextNode = path.get(0).getKey();
            path.remove(path.get(0));
            // if we got to the fruit, remove it from target
            if(path.size() == 0){
                targetFruit = null;
            }
            return nextNode;
        }
    }
    public void reset(){
        this.path = null;
        this.targetFruit = null;
    }

    // getters, setters
    public Fruit getTargetFruit(){ return targetFruit; }
    public int getId(){
        return id;
    }
    public int getSrc(){
        return src;
    }
    public int getDest(){
        return dest;
    }
    public OOP_Point3D getPos(){
        return pos;
    }
    public void setPath(List<oop_node_data> path){
        this.path = path;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setSrc(int src){
        this.src = src;
    }
    public void setDest(int dest){
        this.dest = dest;
    }
    public void setPos(OOP_Point3D p){
        this.pos = p;
    }
    public void setTargetFruit(Fruit fruit){
        targetFruit = fruit;
    }
    // data
    private List<oop_node_data> path;
    private int id;
    private int src;
    private int dest;
    private OOP_Point3D pos;
    private Fruit targetFruit = null;
}
