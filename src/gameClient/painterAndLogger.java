package gameClient;

import Server.Game_Server;
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
    public painterAndLogger(OOP_DGraph gg, int level, game_service game){
        this.gg = gg;
        this.game = game;
        setWindowParams(this.gg);
        this.kml_log = new KML_Logger(String.valueOf(level), gg);
        stats = MyDB.getInfo();
    }

    public void drawAndLog(){
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
        List<String> robots_json = game.getRobots();
        // draw robots and log in kml
        if(robots_json != null) {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.text(textPos.x(), textPos.y() , "Scores:");
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
                    StdDraw.text(textPos.x(), textPos.y() - eps*(i+1), "Robot "+id+": "+score+" points");
                    // log
                    kml_log.addMovingPlacemark(p1.x(), p1.y(), "Robot "+ String.valueOf(id));

                } catch (JSONException jsonException) {
                    System.out.println(jsonException);
                }

            }
        }
        // draw game info and stats from server
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 10));
        StdDraw.text(textPos.x(), textPos.y() + eps, "Time left: " + String.valueOf(game.timeToEnd()/1000));

        if(stats != null) {
            int m = 0;
            for (String line : stats.split("\n")) {
                StdDraw.text(statsPos.x(), statsPos.y() - m * eps, line);
                m++;
            }
        }

    }


    public void drawEndGame(){
        int grade = -1;
        int moves = -1;
        try {
            JSONObject results = new JSONObject(game.toString());
            moves = results.getJSONObject("GameServer").getInt("moves");
            grade = results.getJSONObject("GameServer").getInt("grade");

        }catch(Exception e){
            System.out.println(e);
        }

        int[] score_moves = new int[2];
        score_moves[0] = grade;
        score_moves[1] = moves;

        String gameOver = "Game over!\nScore is: " + score_moves[0] + "\nNumber of moves is: " + score_moves[1];
        int m = 0;
        for (String line : gameOver.split("\n")) {
            StdDraw.text(statsPos.x(), statsPos.y() - m * eps, line);
            m++;
        }
        // update stats
        this.stats = MyDB.getInfo();

        if(stats != null) {
            m = 0;
            for (String line : stats.split("\n")) {
                StdDraw.text(statsPos.x(), statsPos.y() -( m + 5) * eps, line);
                m++;
            }
        }
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

    // gets coordinate range, adds margin, sets position of text and defines epsilon
    private void setWindowParams(OOP_DGraph gg){
        double xMax = 0;
        double xMin = 0;
        double yMax = 0;
        double yMin = 0;
        Iterator<oop_node_data> itr = gg.getV().iterator();
        OOP_Point3D l;
        if(itr.hasNext()){
            l = itr.next().getLocation();
            xMax = l.x();
            xMin = l.x();
            yMax = l.y();
            yMin = l.y();
        }
        while (itr.hasNext()){
            l = itr.next().getLocation();
            if(l.x() > xMax){
                xMax = l.x();
            }
            else if(l.x() < xMin){
                xMin = l.x();
            }
            if(l.y() > yMax){
                yMax = l.y();
            }
            else if(l.y() < yMin){
                yMin = l.y();
            }
        }
        // get lengths of coordinate axises
        double xLen = xMax - xMin;
        double yLen = yMax - yMin;
        // add margin
        double xMargin = 0.05*xLen;
        double yMargin = 0.05*yLen;
        xMax = xMax + xMargin;
        xMin = xMin - xMargin;
        yMax = yMax + 5* yMargin;
        yMin = yMin - yMargin;
        xLen = xMax - xMin;
        yLen = yMax - yMin;
        // set useful parameters
        double XYRatio = xLen/yLen;
        eps = xLen*0.01;
        textPos = new OOP_Point3D((xMax+xMin)/2 - 15* eps, yMax - 2*eps);
        statsPos = new OOP_Point3D((xMax+xMin)/2 , yMax - 2*eps);
        // set parameters in StdDraw
        StdDraw.enableDoubleBuffering();
        StdDraw.setCanvasSize((int)(500 * XYRatio), 500);
        StdDraw.setXscale(xMin, xMax);
        StdDraw.setYscale(yMin, yMax);
    }

    public String saveAndGetKML(){
        String kml_str = null;
        try{
            kml_str = kml_log.saveKML();
        }catch (Exception e){
            e.printStackTrace();
        }
        return kml_str;
    }
    KML_Logger kml_log;
    OOP_DGraph gg;
    game_service game;
    // private data
    // hundredth of x length
    private double eps;
    // position for text on screen
    private OOP_Point3D textPos;
    private OOP_Point3D statsPos;
    private String stats;
}
