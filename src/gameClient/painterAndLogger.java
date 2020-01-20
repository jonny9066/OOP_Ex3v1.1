package gameClient;

import Server.game_service;
import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import oop_utils.OOP_Point3D;
import org.json.JSONException;
import org.json.JSONObject;
import utils.StdDraw;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class painterAndLogger {
    public static void drawAndLog(OOP_DGraph gg, game_service game, List<String> robots_json, double eps,
                             KML_Logger kml_log, OOP_Point3D topMid) {
        Iterator<oop_node_data> itr1;
        Iterator<oop_edge_data> itr2;
        oop_node_data n;
        oop_edge_data e;
        OOP_Point3D p1, p2, p3;
        // Draw nodes
        itr1 = gg.getV().iterator();
        while (itr1.hasNext()) {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.setPenRadius(0.02);
            n = itr1.next();
            p1 = n.getLocation();
            // Actually draw
            StdDraw.point(p1.x(), p1.y());
            // Annotate
            StdDraw.setFont(new Font("Arial", Font.BOLD, 10));
            StdDraw.text(p1.x(), p1.y() + eps*0.5, "" + n.getKey());
        }
        // draw edges
        itr1 = gg.getV().iterator();
        while (itr1.hasNext()) {
            n = itr1.next();
            itr2 = gg.getE(n.getKey()).iterator();
            while (itr2.hasNext()) {
                e = itr2.next();
                p1 = gg.getNode(e.getSrc()).getLocation();
                p2 = gg.getNode(e.getDest()).getLocation();
                // Actually draw
                StdDraw.setPenColor(StdDraw.BLACK);
                StdDraw.setPenRadius(0.0025);
                StdDraw.line(p1.x(), p1.y(), p2.x(), p2.y());
                // draw directional dots
                StdDraw.setPenColor(StdDraw.YELLOW);
                StdDraw.setPenRadius(0.01);
                p3 = new OOP_Point3D(0.25 * p1.x() + 0.75 * p2.x(), 0.25 * p1.y() + 0.75 * p2.y());
                StdDraw.point(p3.x(), p3.y());



            }
        }
        // draw fruit and log in kml
        java.util.List<String> fruit = game.getFruits();
        for (int i = 0; i < fruit.size(); i++) {
            try {
                JSONObject f = new JSONObject(fruit.get(i));
                String pos = f.getJSONObject("Fruit").getString("pos");
                int type = f.getJSONObject("Fruit").getInt("type");
                String[] xAndY = pos.split(",");
                p1 = new OOP_Point3D(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
                // type is dest - src of fruit's edge, if negative then src > dest and we mark it banana
                if(type < 0){
                    StdDraw.picture(p1.x(), p1.y(), "banana.png", 0.0004, 0.0003);
                    kml_log.addMovingPlacemark(p1.x(), p1.y(), "Banana");
                }
                else {
                    StdDraw.picture(p1.x(), p1.y(), "apple.png", 0.0004, 0.0004);
                    kml_log.addMovingPlacemark(p1.x(), p1.y(), "Apple");
                }

            } catch (JSONException jsonException) {
                System.out.println(jsonException);
            }

        }
        // draw robots and log in kml
        if(robots_json != null) {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.text(topMid.x(), topMid.y() , "Scores:");
            for (int i = 0; i < robots_json.size(); i++) {
                try {
                    JSONObject r = new JSONObject(robots_json.get(i));
                    String pos = r.getJSONObject("Robot").getString("pos");
                    int score = r.getJSONObject("Robot").getInt("value");
                    int id = r.getJSONObject("Robot").getInt("id");
                    String[] xAndY = pos.split(",");
                    p1 = new OOP_Point3D(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
                    StdDraw.setPenColor(intToColor(i));
                    StdDraw.setPenRadius(0.004);
                    StdDraw.circle(p1.x(), p1.y(), 0.00015);
                    // draw score for robot
                    StdDraw.setFont(new Font("Arial", Font.BOLD, 10));
                    StdDraw.text(topMid.x(), topMid.y() - eps*(id+1), "Robot "+id+": "+score+" points");
                    // log
                    kml_log.addMovingPlacemark(p1.x(), p1.y(), "Robot "+ String.valueOf(id));

                } catch (JSONException jsonException) {
                    System.out.println(jsonException);
                }

            }
        }
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 10));
        StdDraw.text(topMid.x(), topMid.y() + eps, String.valueOf(game.timeToEnd()));

    }
    private static Color intToColor(int i){
        int cn = i%8;
        if(i == 0){return StdDraw.CYAN; }
        if(i == 1){return StdDraw.GREEN; }
        if(i == 2){return StdDraw.MAGENTA; }
        if(i == 3){return StdDraw.ORANGE; }
        if(i == 4){return StdDraw.PINK; }
        if(i == 5){return StdDraw.BOOK_BLUE; }
        if(i == 6){return StdDraw.BOOK_LIGHT_BLUE; }
        if(i == 7){return StdDraw.BOOK_RED; }
        else {return StdDraw.PRINCETON_ORANGE; }
    }
}
