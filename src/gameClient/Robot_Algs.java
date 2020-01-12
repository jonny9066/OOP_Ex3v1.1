package gameClient;

import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import oop_utils.OOP_Point3D;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class Robot_Algs {
    public List<oop_node_data> pathToNearestFruit(String robot_json, List<String> fruit, OOP_DGraph graph) {
        return null;
    }


    static public oop_edge_data findFruitEdge(String fruitStr, OOP_DGraph graph, double eps) {
        try {
            OOP_Point3D p1, p2, p3;
            Iterator<oop_node_data> itr_n;
            Iterator<oop_edge_data> itr_e;
            oop_edge_data e;
            int fruitType;
            // get fruit coordinate
            JSONObject f = new JSONObject(fruitStr);
            String pos = f.getJSONObject("Fruit").getString("pos");
            fruitType = f.getJSONObject("Fruit").getInt("type");
            String[] xAndY = pos.split(",");
            p3 = new OOP_Point3D(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
            // go ever all edges and use the triangle equation to determine whether fruit is on any one
            itr_n = graph.getV().iterator();
            while (itr_n.hasNext()) {
                itr_e = graph.getE( itr_n.next().getKey()).iterator();
                while (itr_e.hasNext()) {
                    e = itr_e.next();
                    p1 = graph.getNode(e.getSrc()).getLocation();
                    p2 = graph.getNode(e.getDest()).getLocation();
                    // check is on edge, ignore direction
                    if(Math.abs(p1.distance3D(p2) - (p1.distance3D(p3) + p2.distance3D(p3))) < eps) {
                        //check direction
                        if(fruitType == e.getDest() - e.getSrc()){
                            return e;
                        }
                    }

                }
            }

        }
        catch(JSONException exc){
            System.out.println(exc);
        }
        return null;
    }

    // go over all robots, see whether any one is close to p, return robot id if so
    public static int closeToRobot(List<String> robots_json, OOP_Point3D p, double eps){
        for (int i = 0; i < robots_json.size(); i++) {
            try {
                JSONObject r = new JSONObject(robots_json.get(i));
                String pos = r.getJSONObject("Robot").getString("pos");
                String[] xAndY = pos.split(",");
                OOP_Point3D pr = new OOP_Point3D(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
                if(pr.distance3D(p) < eps){
                    return r.getJSONObject("Robot").getInt("id");
                }

            }
            catch (JSONException e){
                System.out.println(e);
            }
        }
        return -1;
    }
}
