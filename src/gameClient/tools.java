package gameClient;

import Server.Game_Server;
import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import oop_utils.OOP_Point3D;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class tools {
    public static OOP_Point3D parseCoordinate(String c) {
        String[] xAndY = c.split(",");
        OOP_Point3D p = new OOP_Point3D(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
        return p;
    }

    static public oop_edge_data findFruitEdge(String fruitStr, OOP_DGraph graph) {
        try {
            oop_edge_data e;
            OOP_Point3D p_src, p_dst, p_fruit;
            Iterator<oop_node_data> itr_n;
            Iterator<oop_edge_data> itr_e;
            // get fruit info
            JSONObject f = new JSONObject(fruitStr);
            //System.out.println(f);
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

        return null;
    }

    public static List<Fruit> fruitsFromJsonToObject(List<String> fruits_json, OOP_DGraph gg){
        ArrayList<Fruit> fruits = new ArrayList<>();
        for (int i = 0; i < fruits_json.size(); i++) {
            fruits.add(new Fruit(fruits_json.get(i), gg));
        }
        return fruits;
    }


}
