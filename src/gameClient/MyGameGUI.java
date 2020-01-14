package gameClient;

import Server.Game_Server;
import Server.game_service;
import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import org.json.JSONException;
import org.json.JSONObject;
import oop_utils.OOP_Point3D;
import utils.StdDraw;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MyGameGUI {
    /**
     * Main function is used for testing purposes
     * @param args
     */
    public static void main(String[] args){
        MyGameGUI mg = new MyGameGUI();
        boolean auto = true;
        mg.start(auto);
    }
    // initialize game
    public MyGameGUI() {
        // get level from user
        int level = -1;
        while(level > 23 || level < 0) {
            try {
                JFrame f = new JFrame("Input");
                level = Integer.parseInt(JOptionPane.showInputDialog(f,
                        "Choose level: 0-23"));

            } catch (Exception exception) {
                System.out.println(exception);
                JFrame f = new JFrame();
                JOptionPane.showMessageDialog(f, "Bad input, please try again.",
                        "Alert", JOptionPane.WARNING_MESSAGE);
            }
        }
        game = Game_Server.getServer(level);
        // initialize game graph from server
        String g = game.getGraph();
        gg = new OOP_DGraph();
        gg.init(g);
        // grt parameters for window size
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
        yMax = yMax + yMargin;
        yMin = yMin - yMargin;
        xLen = xMax - xMin;
        yLen = yMax - yMin;
        // set useful parameters
        XYRatio = xLen/yLen;
        eps = xLen*0.01;
        textPos = new OOP_Point3D((xMax+xMin)/2, yMax - 2*eps);

    }

    public void start(boolean auto) {
        String g;
        // enable to draw off screen
        StdDraw.enableDoubleBuffering();
        StdDraw.setCanvasSize((int)(500 * XYRatio), 500);
        StdDraw.setXscale(xMin, xMax);
        StdDraw.setYscale(yMin, yMax);

        if(auto){
            addRobotsAuto();
            playAuto();
        }
        else{
            addRobotsManual();
            playManual();
        }


    }


    // add robots close to fruit
    private void addRobotsAuto(){
        while(!game.isRunning()) {
            List<String> fruit = game.getFruits();
            Iterator<String> itr = fruit.iterator();
            while (itr.hasNext()) {
                oop_edge_data e = Robot_Algs.findFruitEdge(itr.next(), gg);
                if (e != null) {
                    boolean added = game.addRobot(e.getSrc());
                    game.startGame();
                    if (added) {
                        System.out.println("Robot added on node " + e.getSrc());
                    } else
                        break;
                }

            }
        }

        // in case there are no fruit we add the robots manually
        while(!game.isRunning()){
            for (int i = 0; i < gg.getV().size(); i++) {
                boolean added = game.addRobot(i);
                game.startGame();
                if (added) {
                    System.out.println("Robot added on node " + i);
                } else
                    break;
            }
        }


        System.out.println("Added all robots.");

    }
    //Go over robots, for every robot that is not moving get next move using function
    private void playAuto(){
        while (game.isRunning()) {
            List<String> log = game.move();
            // get robots and fruit from game
            List<String> robots_json = game.getRobots();
            List<String> fruits_json = game.getFruits();
            // go over robots
            for (int i = 0; i < robots_json.size(); i++) {
                try {
                    // get robot info
                    JSONObject r = new JSONObject(robots_json.get(i));
                    int id = r.getJSONObject("Robot").getInt("id");
                    int dest = r.getJSONObject("Robot").getInt("dest");
                    // check that robot is not moving
                    if(dest == -1){
                        // get path for this robot
                        List<oop_node_data> path = Robot_Algs.pathToNearestFruit(robots_json.get(i),
                                fruits_json, game.getGraph());
                        // move
                        long move = game.chooseNextEdge(id, path.get(0).getKey());
                        // check that robot moved
                        if(move != -1)
                            System.out.println("Next node for " + id + " is " + path.get(0).getKey());
                        else
                            System.out.println("Robot " +id + " tries to go to node "
                                    + path.get(0).getKey() + ", but can't!");
                    }

                } catch (JSONException jsonException) {
                    System.out.println(jsonException);
                }
            }
            // draw
            StdDraw.clear();
            draw(gg, game, log, eps, textPos);
            StdDraw.show();
            StdDraw.pause(20);
        }
    }


    private void addRobotsManual(){
        while (!game.isRunning()) {
            // when mouse is pressed
            if (StdDraw.isMousePressed()) {
                OOP_Point3D p1, p2;
                // Get coordinates.
                p1 = new OOP_Point3D(StdDraw.mouseX(), StdDraw.mouseY());
                p2 = p1;
                while (StdDraw.isMousePressed()) {
                    // Get coordinates.
                    p2 = new OOP_Point3D(StdDraw.mouseX(), StdDraw.mouseY());
                }
                // check click, not drag
                if (p1.close2equals(p2)) {
                    StdDraw.point(p1.x(), p1.y());
                    // check node was clicked
                    int key = closeToNode(gg, p1, eps);
                    if (key != -1) {
                        game.addRobot(key);
                        game.startGame();
                        System.out.println("Robot added on node " + key);
                    }
                }
            }

            StdDraw.clear();
            draw(gg, game, null, eps, textPos);
            StdDraw.show();
            StdDraw.pause(20);
        }

        System.out.println("All robots have been added!");
    }



    private void playManual(){
        while (game.isRunning()) {
            OOP_Point3D p1, p2;
            List<String> log = game.move();
            if (log != null) {
                //System.out.println(log);
                // When clicking get coordinates of mouse.
                if (StdDraw.isMousePressed()) {
                    // Get coordinates.
                    p1 = new OOP_Point3D(StdDraw.mouseX(), StdDraw.mouseY());
                    p2 = new OOP_Point3D(p1.x(), p1.y());
                    while (StdDraw.isMousePressed()) {
                        // Get coordinates.
                        p2 = new OOP_Point3D(StdDraw.mouseX(), StdDraw.mouseY());
                    }
                    // check click, not drag
                    if (p1.close2equals(p2)) {
                        // check that a robot was clicked
                        int id = Robot_Algs.closeToRobot(log, p1, eps * 1.5);
                        if (id != -1) {
                            // get robot source node
                            int key = -1;
                            try {
                                JSONObject robot = new JSONObject(game.getRobots().get(id));
                                key = robot.getJSONObject("Robot").getInt("src");
                            } catch (JSONException exc) {
                                System.out.println(exc);
                            }
                            if (key != -1) {
                                // get keys of adjacent nodes in a string
                                Collection<oop_edge_data> adjNodes = gg.getE(key);
                                Iterator<oop_edge_data> adjItr = adjNodes.iterator();
                                String adj = "";
                                while (adjItr.hasNext()) {
                                    adj += adjItr.next().getDest() + ", ";
                                }
                                // cosmetics
                                adj = adj.substring(0, adj.length() - 2);
                                try {
                                    JFrame f = new JFrame("Choose next node");
                                    int choice = Integer.parseInt(JOptionPane.showInputDialog(f,
                                            "Options: " + adj));
                                    // check that chosen node is valid
                                    long i = game.chooseNextEdge(id, choice);
                                    if (i != -1)
                                        System.out.println("Robot " + id + " moves  to " + choice);
                                    else
                                        System.out.println("Robot cannot move to " + choice);

                                } catch (Exception exception) {
                                    System.out.println(exception);
                                    JFrame f = new JFrame();
                                    JOptionPane.showMessageDialog(f, "Bad input.",
                                            "Alert", JOptionPane.WARNING_MESSAGE);
                                }

                            }
                            long m = game.chooseNextEdge(id, key);
                            if (m != -1) {
                                System.out.println("Robot " + id + " moves  to " + key);
                            }

                        }
                    }
                }

            }

            // draw
            StdDraw.clear();
            draw(gg, game, log, eps, textPos);
            StdDraw.show();
            StdDraw.pause(20);
        }
    }



    private static int closeToNode(OOP_DGraph g, OOP_Point3D p, double eps){
        Iterator<oop_node_data> itr = g.getV().iterator();
        while(itr.hasNext()){
            oop_node_data n = itr.next();
            if(p.distance3D(n.getLocation()) < eps) {
                return n.getKey();
            }
        }
        return -1;
    }



    private static void draw(OOP_DGraph gg, game_service game, List<String> robots_json, double eps, OOP_Point3D topMid) {
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
        // draw fruit
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
                }
                else
                    StdDraw.picture(p1.x(), p1.y(), "apple.png", 0.0004, 0.0004);

            } catch (JSONException jsonException) {
                System.out.println(jsonException);
            }

        }
        // draw robots
        if(robots_json != null) {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.text(topMid.x(), topMid.y() , "Scores:");
            for (int i = 0; i < robots_json.size(); i++) {
                try {
                    JSONObject r = new JSONObject(robots_json.get(i));
                    //System.out.println(r);
                    String pos = r.getJSONObject("Robot").getString("pos");
                    int score = r.getJSONObject("Robot").getInt("value");
                    int id = r.getJSONObject("Robot").getInt("id");
                    String[] xAndY = pos.split(",");
                    p1 = new OOP_Point3D(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
                    StdDraw.setPenColor(intToColor(i));
                    StdDraw.setPenRadius(0.004);
                    StdDraw.circle(p1.x(), p1.y(), 0.00015);
                    StdDraw.setFont(new Font("Arial", Font.BOLD, 10));
                    StdDraw.text(topMid.x(), topMid.y() - eps*(id+1), "Robot "+id+": "+score+" points");

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

    private double xMax;
    private double xMin;
    private double yMax;
    private double yMin;
    // x/y
    private double XYRatio;
    // hundredth of x length
    private double eps;
    // position for text on screen
    private OOP_Point3D textPos;
    private OOP_DGraph gg;
    private transient int mc;
    private game_service game;
}