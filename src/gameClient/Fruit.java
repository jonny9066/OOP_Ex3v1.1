package gameClient;

import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import oop_utils.OOP_Point3D;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


public class Fruit {
    /**
     * We copy the values of the fruit and find the edge it's on
     * @param json_str a JSON string representing a fruit
     * @param graph the game graph the fruit is on
     */
    public Fruit(String json_str, OOP_DGraph graph){
        try {
            JSONObject f = new JSONObject(json_str);
            setPos(tools.parseCoordinate(f.getJSONObject("Fruit").getString("pos")));
            setType(f.getJSONObject("Fruit").getInt("type"));
            setValue(f.getJSONObject("Fruit").getDouble("value"));

        } catch (JSONException jsonException) {
            System.out.println(jsonException);
        }
        oop_edge_data edge = findFruitEdge(json_str, graph);
        setSrc(edge.getSrc());
        setDest(edge.getDest());
    }

    static public oop_edge_data findFruitEdge(String fruitStr, OOP_DGraph graph) {
        oop_edge_data e = null;
        try {
            OOP_Point3D p_src, p_dst, p_fruit;
            Iterator<oop_node_data> itr_n;
            Iterator<oop_edge_data> itr_e;
            // get fruit info
            JSONObject f = new JSONObject(fruitStr);
            String pos = f.getJSONObject("Fruit").getString("pos");
            int fruitType = f.getJSONObject("Fruit").getInt("type");
            // parse fruit coordinates
            p_fruit = tools.parseCoordinate(pos);
            // go ever all edges and use the triangle equation to determine whether fruit is on any one
            itr_n = graph.getV().iterator();
            while (itr_n.hasNext()) {
                // iterator over edges belonging to node
                oop_node_data n = itr_n.next();
                itr_e = graph.getE(n.getKey()).iterator();
                while (itr_e.hasNext()) {
                    e = itr_e.next();
                    p_src = graph.getNode(e.getSrc()).getLocation();
                    p_dst = graph.getNode(e.getDest()).getLocation();
                    // delta must be positive and very small by the triangle equation
                    double eps = 0.0000001;
                    double delta = Math.abs(p_src.distance3D(p_fruit) + p_dst.distance3D(p_fruit) - p_src.distance3D(p_dst));
                    if (delta < eps) {
                        //check direction
                        if (fruitType < 0 && e.getDest() - e.getSrc() < 0) {
                            return e;
                        }
                        if (fruitType > 0 && e.getDest() - e.getSrc() > 0) {
                            return e;
                        }

                    }

                }
            }
        } catch (JSONException exc) {
            System.out.println(exc);
        }
        if(e == null)
            throw new RuntimeException("Can't find fruit edge!");
        return null;
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


