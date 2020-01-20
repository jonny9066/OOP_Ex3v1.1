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
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

public class MyGameGUI {
    /**
     * Main function is used for testing purposes
     * @param args
     */
    public static void main(String[] args){
        MyGameGUI mg = new MyGameGUI();
        mg.startAuto();
    }
    // initialize game
    private void MyGameGUI(int level) {
        // get level from user
        game = Game_Server.getServer(level);
        // initialize game graph from server
        String g = game.getGraph();
        gg = new OOP_DGraph();
        gg.init(g);
        // initialize kml logger
        kml_log = new KML_Logger(String.valueOf(level), gg);
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
    private void initializeWithChoice(){
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
        MyGameGUI(level);
    }
    // run levels 0, 1, 3, ..., 23, compare scores and save to DB
    private void runCompetitionLevels(){
        // int[x][y], x is for case, y is for {stage, grade and moves}
        int[][] passTable = {{0, 145, 290}, {1, 450, 580}, {3, 720, 580}, {5, 570, 500}, {9, 510, 580},
                {11, 1050, 580}, {13, 310, 580}, {16, 235, 290}, {19, 250, 580}, {20, 200, 290}, {23, 1000, 1140}};
        for (int i = 0; i <passTable.length ; i++) {
            MyGameGUI(passTable[i][0]);
        }
    }



    public void startAuto(){
        start(true);
    }
    public void startManual(){
        start(false);
    }

    private void start(boolean auto) {
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
        try {
            kml_log.saveKML();
        }catch (FileNotFoundException exc){
            System.out.println(exc);
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
            // get robots from game
            List<String> robots_json = game.getRobots();
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
                        // move
                        oop_node_data nn = Robot_Algs.nextNode(robots_json.get(i), game.getFruits(), game.getGraph());
                        long move = game.chooseNextEdge(id, nn.getKey());
                        // check that robot moved
                        if(move != -1) {
                            //System.out.println("Next node for " + id + " is " + nn.getKey());
                        }
                        else
                            System.out.println("Robot " +id + " tries to go to node "
                                    + nn.getKey() + ", but can't!");
                    }

                } catch (JSONException jsonException) {
                    System.out.println(jsonException);
                }
            }
            // draw
            StdDraw.clear();
            painterAndLogger.drawAndLog(gg, game, log, eps, kml_log, textPos);
            StdDraw.show();
            StdDraw.pause(20);
        }
        //log results
        int totalScore = 0;
        List<String> robots_json = game.getRobots();
        // go over robots
        for (int i = 0; i < robots_json.size(); i++) {
            try {
                // get robot info
                JSONObject r = new JSONObject(robots_json.get(i));
                int score = r.getJSONObject("Robot").getInt("value");
                totalScore+= score;
            } catch (JSONException jsonException) {
                System.out.println(jsonException);
            }
        }
        System.out.println("Final score is: " + totalScore);

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
                    int key = Robot_Algs.closeToNode(gg, p1, eps);
                    if (key != -1) {
                        game.addRobot(key);
                        game.startGame();
                        System.out.println("Robot added on node " + key);
                    }
                }
            }

            StdDraw.clear();
            painterAndLogger.drawAndLog(gg, game, null, eps, kml_log, textPos);
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
                                    if (i != -1) {
                                        //System.out.println("Robot " + id + " moves  to " + choice);
                                    }
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
            painterAndLogger.drawAndLog(gg, game, log, eps, kml_log, textPos);
            StdDraw.show();
            StdDraw.pause(20);
        }
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
    private game_service game;
    private KML_Logger kml_log;
}