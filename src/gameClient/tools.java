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


    public static List<Fruit> fruitsFromJsonToObject(List<String> fruits_json, OOP_DGraph gg){
        ArrayList<Fruit> fruits = new ArrayList<>();
        for (int i = 0; i < fruits_json.size(); i++) {
            fruits.add(new Fruit(fruits_json.get(i), gg));
        }
        return fruits;
    }


}
