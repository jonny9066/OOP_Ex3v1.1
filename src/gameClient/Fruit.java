package gameClient;

import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_utils.OOP_Point3D;
import org.json.JSONException;
import org.json.JSONObject;



public class Fruit {
    public Fruit(String json_str, OOP_DGraph graph){
        try {
            JSONObject f = new JSONObject(json_str);
            setPos(tools.parseCoordinate(f.getJSONObject("Fruit").getString("pos")));
            setType(f.getJSONObject("Fruit").getInt("type"));
            setValue(f.getJSONObject("Fruit").getDouble("value"));

        } catch (JSONException jsonException) {
            System.out.println(jsonException);
        }
        oop_edge_data edge = tools.findFruitEdge(json_str, graph);
        setSrc(edge.getSrc());
        setDest(edge.getDest());
    }

    public int getType(){
        return type;
    }
    public int getSrc(){
        return src;
    }
    public int getDest(){
        return dest;
    }

    public double getValue(){
        return value;
    }
    public OOP_Point3D getPos(){
        return pos;
    }
    public void setType(int type){
        this.type = type;
    }
    public void setSrc(int src){
        this.src = src;
    }
    public void setDest(int dest){
        this.dest = dest;
    }
    public void setValue(double value){
        this.value = value;
    }
    public void setPos(OOP_Point3D p){
        this.pos = p;
    }

    // data
    private double value;
    private int type;
    private int src;
    private int dest;
    private OOP_Point3D pos;
}


